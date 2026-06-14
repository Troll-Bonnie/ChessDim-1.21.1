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
        double t = 0.2; // толщина

        boolean hasN = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x, pos.z - 1));
        boolean hasS = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x, pos.z + 1));
        boolean hasW = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x - 1, pos.z));
        boolean hasE = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x + 1, pos.z));

        // Углы — столбы там где две стены встречаются
        boolean cornerNW = !hasN && !hasW;
        boolean cornerNE = !hasN && !hasE;
        boolean cornerSW = !hasS && !hasW;
        boolean cornerSE = !hasS && !hasE;

        // Север — смещение внутрь по +Z
        if (!hasN)
            solidWall(buffer, matrix, minX, minZ, maxX, minZ + t, yBot, yTop, r, g, b);
        // Юг — смещение внутрь по -Z
        if (!hasS)
            solidWall(buffer, matrix, minX, maxZ - t, maxX, maxZ, yBot, yTop, r, g, b);
        // Запад — смещение внутрь по +X
        if (!hasW)
            solidWall(buffer, matrix, minX, minZ, minX + t, maxZ, yBot, yTop, r, g, b);
        // Восток — смещение внутрь по -X
        if (!hasE)
            solidWall(buffer, matrix, maxX - t, minZ, maxX, maxZ, yBot, yTop, r, g, b);
        /*
        // Столбы на углах
        if (cornerNW)
            solidWall(buffer, matrix, minX, minZ, minX + t, minZ + t, yBot, yTop, r, g, b);
        if (cornerNE)
            solidWall(buffer, matrix, maxX - t, minZ, maxX, minZ + t, yBot, yTop, r, g, b);
        if (cornerSW)
            solidWall(buffer, matrix, minX, maxZ - t, minX + t, maxZ, yBot, yTop, r, g, b);
        if (cornerSE)
            solidWall(buffer, matrix, maxX - t, maxZ - t, maxX, maxZ, yBot, yTop, r, g, b);

         */
    }

    // Рисует сплошной куб (6 граней) от x1,z1 до x2,z2 по высоте yBot..yTop
    private static void solidWall(BufferBuilder buffer, Matrix4f matrix,
                                  double x1, double z1, double x2, double z2,
                                  double yBot, double yTop,
                                  float r, float g, float b) {
        float a = 0.65f;
        // Низ
        hQuad(buffer, matrix, x1, z1, x2, z2, yBot, r, g, b, a);
        // Верх
        hQuad(buffer, matrix, x1, z1, x2, z2, yTop, r, g, b, a);
        // Север (z1)
        vQuad(buffer, matrix, x1, z1, x2, z1, yBot, yTop, r, g, b, a);
        // Юг (z2)
        vQuad(buffer, matrix, x1, z2, x2, z2, yBot, yTop, r, g, b, a);
        // Запад (x1)
        vQuad(buffer, matrix, x1, z1, x1, z2, yBot, yTop, r, g, b, a);
        // Восток (x2)
        vQuad(buffer, matrix, x2, z1, x2, z2, yBot, yTop, r, g, b, a);
    }

    // Горизонтальный quad
    private static void hQuad(BufferBuilder buffer, Matrix4f matrix,
                              double x1, double z1, double x2, double z2,
                              double y, float r, float g, float b, float a) {
        buffer.addVertex(matrix, (float)x1, (float)y, (float)z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x2, (float)y, (float)z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x2, (float)y, (float)z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x1, (float)y, (float)z2).setColor(r, g, b, a);
    }

    // Вертикальный quad
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