package net.baphy.chessdim.client;

import net.baphy.chessdim.item.CellBrushItem;
import net.baphy.chessdim.registry.ModDimensions;
import net.baphy.chessdim.world.CellType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;


public class ChunkSelectionRenderer {

    public static float alpha = 0.3f;
    public static float thickness = 0.8f;
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.CHESS_WORLD_KEY)) return;
        if (!(mc.player.getMainHandItem().getItem()
                instanceof CellBrushItem)) return;

        if (CellBrushClientHandler.clientSelection.isEmpty()) return;

        CellType type = CellBrushClientHandler.clientType;
        int color = type.getParticleColor();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8)  & 0xFF) / 255f;
        float b = (color         & 0xFF) / 255f;

        var camera = event.getCamera();
        double cx = camera.getPosition().x;
        double cy = camera.getPosition().y;
        double cz = camera.getPosition().z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        RenderSystem.applyModelViewMatrix();
        Matrix4f modelView = event.getModelViewMatrix();

        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR);

        for (ChunkPos pos : CellBrushClientHandler.clientSelection) {
            drawChunkOutline(buffer, modelView, pos, cx, cy, cz, r, g, b);
        }

        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private static void drawChunkOutline(BufferBuilder buffer, Matrix4f matrix,
                                         ChunkPos pos,
                                         double cx, double cy, double cz,
                                         float r, float g, float b) {
        double minX = pos.getMinBlockX() - cx;
        double maxX = pos.getMaxBlockX() + 1 - cx;
        double minZ = pos.getMinBlockZ() - cz;
        double maxZ = pos.getMaxBlockZ() + 1 - cz;
        double yBot = 1.0 - cy;
        double yTop = 2.0 - cy;
        float t = thickness;
        double h = t / 2.0;

        boolean hasN  = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x,     pos.z - 1));
        boolean hasS  = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x,     pos.z + 1));
        boolean hasW  = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x - 1, pos.z    ));
        boolean hasE  = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x + 1, pos.z    ));
        boolean hasNW = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x - 1, pos.z - 1));
        boolean hasNE = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x + 1, pos.z - 1));
        boolean hasSW = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x - 1, pos.z + 1));
        boolean hasSE = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x + 1, pos.z + 1));

        // --- Стены (центрированы по грани, укорочены на h с каждого конца) ---
        if (!hasN) wall(buffer, matrix, minX + 2*h, minZ - h, maxX - 2*h, minZ + h, yBot, yTop, r, g, b);
        if (!hasS) wall(buffer, matrix, minX + 2*h, maxZ - h, maxX - 2*h, maxZ + h, yBot, yTop, r, g, b);
        if (!hasW) wall(buffer, matrix, minX - h, minZ + 2*h, minX + h, maxZ - 2*h, yBot, yTop, r, g, b);
        if (!hasE) wall(buffer, matrix, maxX - h, minZ + 2*h, maxX + h, maxZ - 2*h, yBot, yTop, r, g, b);

        // --- Углы NW ---
        if (!hasN && !hasW) {
            // Внешний — нет соседей
            outerCorner(buffer, matrix, minX - h, minZ - h, minX + h, minZ + h, yBot, yTop, r, g, b);
        } else if (hasN && hasW && !hasNW) {
            // Внутренний — два соседа но нет диагонали
            innerCorner(buffer, matrix, minX - h, minZ - h, minX + h, minZ + h, yBot, yTop,
                    true, true, r, g, b); // грани: внутренние (E и S)
        } else if (!hasN || !hasW) {
            // Прямой — один сосед
            straightCorner(buffer, matrix, minX - h, minZ - h, minX + h, minZ + h, yBot, yTop,
                    hasN, hasW, false, false, r, g, b);
        }

        // --- Углы NE ---
        if (!hasN && !hasE) {
            outerCorner(buffer, matrix, maxX - h, minZ - h, maxX + h, minZ + h, yBot, yTop, r, g, b);
        } else if (hasN && hasE && !hasNE) {
            innerCorner(buffer, matrix, maxX - h, minZ - h, maxX + h, minZ + h, yBot, yTop,
                    true, false, r, g, b);
        } else if (!hasN || !hasE) {
            straightCorner(buffer, matrix, maxX - h, minZ - h, maxX + h, minZ + h, yBot, yTop,
                    hasN, false, false, hasE, r, g, b);
        }

        // --- Углы SW ---
        if (!hasS && !hasW) {
            outerCorner(buffer, matrix, minX - h, maxZ - h, minX + h, maxZ + h, yBot, yTop, r, g, b);
        } else if (hasS && hasW && !hasSW) {
            innerCorner(buffer, matrix, minX - h, maxZ - h, minX + h, maxZ + h, yBot, yTop,
                    false, true, r, g, b);
        } else if (!hasS || !hasW) {
            straightCorner(buffer, matrix, minX - h, maxZ - h, minX + h, maxZ + h, yBot, yTop,
                    false, hasW, hasS, false, r, g, b);
        }

        // --- Углы SE ---
        if (!hasS && !hasE) {
            outerCorner(buffer, matrix, maxX - h, maxZ - h, maxX + h, maxZ + h, yBot, yTop, r, g, b);
        } else if (hasS && hasE && !hasSE) {
            innerCorner(buffer, matrix, maxX - h, maxZ - h, maxX + h, maxZ + h, yBot, yTop,
                    false, false, r, g, b);
        } else if (!hasS || !hasE) {
            straightCorner(buffer, matrix, maxX - h, maxZ - h, maxX + h, maxZ + h, yBot, yTop,
                    false, false, hasS, hasE, r, g, b);
        }
    }

    // --- Стена: верх, низ, две вертикальные грани ---
    private static void wall(BufferBuilder buffer, Matrix4f matrix,
                             double x1, double z1, double x2, double z2,
                             double yBot, double yTop,
                             float r, float g, float b) {
        float a = alpha;
        hQuad(buffer, matrix, x1, z1, x2, z2, yBot, r, g, b, a);
        hQuad(buffer, matrix, x1, z1, x2, z2, yTop, r, g, b, a);
        // Две длинные грани
        vQuad(buffer, matrix, x1, z1, x2, z1, yBot, yTop, r, g, b, a);
        vQuad(buffer, matrix, x1, z2, x2, z2, yBot, yTop, r, g, b, a);
        // Две короткие торцевые грани
        vQuad(buffer, matrix, x1, z1, x1, z2, yBot, yTop, r, g, b, a);
        vQuad(buffer, matrix, x2, z1, x2, z2, yBot, yTop, r, g, b, a);
    }

    // --- Внешний угол: полный куб ---
    private static void outerCorner(BufferBuilder buffer, Matrix4f matrix,
                                    double x1, double z1, double x2, double z2,
                                    double yBot, double yTop,
                                    float r, float g, float b) {
        wall(buffer, matrix, x1, z1, x2, z2, yBot, yTop, r, g, b);
    }

    // --- Внутренний угол: верх, низ, две внутренние грани ---
// hasN/hasW — какие соседи есть (определяет какие грани рисовать)
    private static void innerCorner(BufferBuilder buffer, Matrix4f matrix,
                                    double x1, double z1, double x2, double z2,
                                    double yBot, double yTop,
                                    boolean innerE, boolean innerS,
                                    float r, float g, float b) {
        float a = 0.85f;
        hQuad(buffer, matrix, x1, z1, x2, z2, yBot, r, g, b, a);
        hQuad(buffer, matrix, x1, z1, x2, z2, yTop, r, g, b, a);
        if (innerE) vQuad(buffer, matrix, x2, z1, x2, z2, yBot, yTop, r, g, b, a);
        if (innerS) vQuad(buffer, matrix, x1, z2, x2, z2, yBot, yTop, r, g, b, a);
        if (!innerE) vQuad(buffer, matrix, x1, z1, x1, z2, yBot, yTop, r, g, b, a);
        if (!innerS) vQuad(buffer, matrix, x1, z1, x2, z1, yBot, yTop, r, g, b, a);
    }

    // --- Прямой угол: верх, низ, две внешние грани ---
// hasN/hasS/hasW/hasE — какие соседи есть
    private static void straightCorner(BufferBuilder buffer, Matrix4f matrix,
                                       double x1, double z1, double x2, double z2,
                                       double yBot, double yTop,
                                       boolean hasN, boolean hasW,
                                       boolean hasS, boolean hasE,
                                       float r, float g, float b) {
        float a = 0.85f;
        hQuad(buffer, matrix, x1, z1, x2, z2, yBot, r, g, b, a);
        hQuad(buffer, matrix, x1, z1, x2, z2, yTop, r, g, b, a);
        // Рисуем только внешние грани (где нет соседа)
        if (!hasN) vQuad(buffer, matrix, x1, z1, x2, z1, yBot, yTop, r, g, b, a);
        if (!hasS) vQuad(buffer, matrix, x1, z2, x2, z2, yBot, yTop, r, g, b, a);
        if (!hasW) vQuad(buffer, matrix, x1, z1, x1, z2, yBot, yTop, r, g, b, a);
        if (!hasE) vQuad(buffer, matrix, x2, z1, x2, z2, yBot, yTop, r, g, b, a);
    }

    private static void hQuad(BufferBuilder buffer, Matrix4f matrix,
                              double x1, double z1, double x2, double z2,
                              double y, float r, float g, float b, float a) {
        buffer.addVertex(matrix, (float)x1, (float)y, (float)z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x2, (float)y, (float)z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x2, (float)y, (float)z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x1, (float)y, (float)z2).setColor(r, g, b, a);
    }

    private static void vQuad(BufferBuilder buffer, Matrix4f matrix,
                              double x1, double z1, double x2, double z2,
                              double yBot, double yTop,
                              float r, float g, float b, float a) {
        buffer.addVertex(matrix, (float)x1, (float)yBot, (float)z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x2, (float)yBot, (float)z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x2, (float)yTop, (float)z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x1, (float)yTop, (float)z1).setColor(r, g, b, a);
    }
}