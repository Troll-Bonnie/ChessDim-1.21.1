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
    public static float thickness = 1.0f;
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
        float a = alpha;

        boolean hasN  = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x,     pos.z - 1));
        boolean hasS  = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x,     pos.z + 1));
        boolean hasW  = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x - 1, pos.z    ));
        boolean hasE  = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x + 1, pos.z    ));
        boolean hasNW = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x - 1, pos.z - 1));
        boolean hasNE = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x + 1, pos.z - 1));
        boolean hasSW = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x - 1, pos.z + 1));
        boolean hasSE = CellBrushClientHandler.clientSelection.contains(new ChunkPos(pos.x + 1, pos.z + 1));

        // walls
        if (!hasN){
            hQuad(buffer, matrix, minX, minZ, minX + t, minZ + t, maxX - t, minZ + t, maxX, minZ, yBot, r, g, b, a);
            hQuad(buffer, matrix, minX, minZ, minX + t, minZ + t, maxX - t, minZ + t, maxX, minZ, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX, minZ, maxX, minZ, yBot, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX + t, minZ + t, maxX - t, minZ + t, yBot, yTop, r, g, b, a);
        }
        if (!hasS){
            hQuad(buffer, matrix, minX, maxZ, minX + t, maxZ - t, maxX - t, maxZ - t, maxX, maxZ, yBot, r, g, b, a);
            hQuad(buffer, matrix, minX, maxZ, minX + t, maxZ - t, maxX - t, maxZ - t, maxX, maxZ, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX, maxZ, maxX, maxZ, yBot, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX + t, maxZ - t, maxX - t, maxZ - t, yBot, yTop, r, g, b, a);
        }
        if (!hasW){
            hQuad(buffer, matrix, minX, minZ, minX + t, minZ + t, minX + t, maxZ - t, minX, maxZ, yBot, r, g, b, a);
            hQuad(buffer, matrix, minX, minZ, minX + t, minZ + t, minX + t, maxZ - t, minX, maxZ, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX, minZ, minX, maxZ, yBot, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX + t, minZ + t, minX + t, maxZ - t, yBot, yTop, r, g, b, a);

        }
        if (!hasE) {
            hQuad(buffer, matrix, maxX, minZ, maxX - t, minZ + t, maxX - t, maxZ - t, maxX, maxZ, yBot, r, g, b, a);
            hQuad(buffer, matrix, maxX, minZ, maxX - t, minZ + t, maxX - t, maxZ - t, maxX, maxZ, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX, minZ, maxX, maxZ, yBot, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX - t, minZ + t, maxX - t, maxZ - t, yBot, yTop, r, g, b, a);
        }

        //corners
        if (hasN && hasW && !hasNW ){
            hQuad(buffer, matrix, minX - t, minZ + t, minX + t, minZ + t, minX + t, minZ + t, minX + t, minZ - t, yBot, r, g, b, a);
            hQuad(buffer, matrix, minX - t, minZ + t, minX + t, minZ + t, minX + t, minZ + t, minX + t, minZ - t, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX - t, minZ + t, minX + t, minZ + t, yBot, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX + t, minZ - t, minX + t, minZ + t, yBot, yTop, r, g, b, a);
        }
        if (hasN && hasE && !hasNE ){
            hQuad(buffer, matrix, maxX + t, minZ + t, maxX - t, minZ + t, maxX - t, minZ + t, maxX - t, minZ - t, yBot, r, g, b, a);
            hQuad(buffer, matrix, maxX + t, minZ + t, maxX - t, minZ + t, maxX - t, minZ + t, maxX - t, minZ - t, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX - t, minZ - t, maxX - t, minZ + t, yBot, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX + t, minZ + t, maxX - t, minZ + t, yBot, yTop, r, g, b, a);
        }
        if (hasS && hasE && !hasSE ){
            hQuad(buffer, matrix, maxX + t, maxZ - t, maxX - t, maxZ - t, maxX - t, maxZ - t, maxX - t, maxZ + t, yBot, r, g, b, a);
            hQuad(buffer, matrix, maxX + t, maxZ - t, maxX - t, maxZ - t, maxX - t, maxZ - t, maxX - t, maxZ + t, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX - t, maxZ + t, maxX - t, maxZ - t, yBot, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX + t, maxZ - t, maxX - t, maxZ - t, yBot, yTop, r, g, b, a);
        }
        if (hasS && hasW && !hasSW ){
            hQuad(buffer, matrix,minX - t, maxZ - t, minX + t, maxZ - t, minX + t, maxZ - t, minX + t, maxZ + t, yBot, r, g, b, a);
            hQuad(buffer, matrix, minX - t, maxZ - t, minX + t, maxZ - t, minX + t, maxZ - t, minX + t, maxZ + t, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX + t, maxZ + t, minX + t, maxZ - t, yBot, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX - t, maxZ - t, minX + t, maxZ - t, yBot, yTop, r, g, b, a);
        }

        //straight connectors
        if(hasN && !hasW && !hasNW ){
            hQuad(buffer, matrix, minX, minZ, minX + t, minZ, minX + t, minZ, minX + t, minZ + t, yBot, r, g, b, a);
            hQuad(buffer, matrix, minX, minZ, minX + t, minZ, minX + t, minZ, minX + t, minZ + t, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX + t, minZ + t, minX + t, minZ, yBot, yTop, r, g, b, a);
        }
        if(hasN && !hasE && !hasNE ){
            hQuad(buffer, matrix, maxX, minZ, maxX - t, minZ, maxX - t, minZ, maxX - t, minZ + t, yBot, r, g, b, a);
            hQuad(buffer, matrix, maxX, minZ, maxX - t, minZ, maxX - t, minZ, maxX - t, minZ + t, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX - t, minZ + t, maxX - t, minZ, yBot, yTop, r, g, b, a);
        }
        if(hasE && !hasN && !hasNE ){
            hQuad(buffer, matrix, maxX, minZ, maxX, minZ + t, maxX, minZ + t, maxX - t, minZ + t, yBot, r, g, b, a);
            hQuad(buffer, matrix, maxX, minZ, maxX, minZ + t, maxX, minZ + t, maxX - t, minZ + t, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX - t, minZ + t, maxX, minZ + t, yBot, yTop, r, g, b, a);
        }
        if(hasE && !hasS && !hasSE ){
            hQuad(buffer, matrix, maxX, maxZ, maxX, maxZ - t, maxX, maxZ - t, maxX - t, maxZ - t, yBot, r, g, b, a);
            hQuad(buffer, matrix, maxX, maxZ, maxX, maxZ - t, maxX, maxZ - t, maxX - t, maxZ - t, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX - t, maxZ - t, maxX, maxZ - t, yBot, yTop, r, g, b, a);
        }
        if(hasS && !hasE && !hasSE ){
            hQuad(buffer, matrix, maxX - t, maxZ - t, maxX - t, maxZ, maxX - t, maxZ, maxX, maxZ, yBot, r, g, b, a);
            hQuad(buffer, matrix, maxX - t, maxZ - t, maxX - t, maxZ, maxX - t, maxZ, maxX, maxZ, yTop, r, g, b, a);
            vQuad(buffer, matrix, maxX - t, maxZ - t, maxX - t, maxZ, yBot, yTop, r, g, b, a);
        }
        if(hasS && !hasW && !hasSW ){
            hQuad(buffer, matrix, minX, maxZ, minX + t, maxZ, minX + t, maxZ, minX + t, maxZ - t, yBot, r, g, b, a);
            hQuad(buffer, matrix, minX, maxZ, minX + t, maxZ, minX + t, maxZ, minX + t, maxZ - t, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX + t, maxZ - t, minX + t, maxZ, yBot, yTop, r, g, b, a);
        }
        if(hasW && !hasS && !hasSW ){
            hQuad(buffer, matrix, minX, maxZ, minX, maxZ - t, minX, maxZ - t, minX + t, maxZ - t, yBot, r, g, b, a);
            hQuad(buffer, matrix, minX, maxZ, minX, maxZ - t, minX, maxZ - t, minX + t, maxZ - t, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX + t, maxZ - t, minX, maxZ - t, yBot, yTop, r, g, b, a);
        }
        if(hasW && !hasN && !hasNW ){
            hQuad(buffer, matrix, minX, minZ, minX, minZ + t, minX, minZ + t, minX + t, minZ + t, yBot, r, g, b, a);
            hQuad(buffer, matrix, minX, minZ, minX, minZ + t, minX, minZ + t, minX + t, minZ + t, yTop, r, g, b, a);
            vQuad(buffer, matrix, minX + t, minZ + t, minX, minZ + t, yBot, yTop, r, g, b, a);
        }


    }

    private static void hQuad(BufferBuilder buffer, Matrix4f matrix,
                              double x1, double z1, double x2, double z2, double x3, double z3, double x4, double z4,
                              double y, float r, float g, float b, float a) {
        buffer.addVertex(matrix, (float)x1, (float)y, (float)z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x2, (float)y, (float)z2).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x3, (float)y, (float)z3).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float)x4, (float)y, (float)z4).setColor(r, g, b, a);
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