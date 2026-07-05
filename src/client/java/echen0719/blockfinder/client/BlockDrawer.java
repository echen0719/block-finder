package echen0719.blockfinder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix4f;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

// https://modrinth.com/mod/block-hightlightfx saved the day!!

public class BlockDrawer {
    private static Minecraft client = Minecraft.getInstance();

    public static void drawOutline(LevelRenderContext context, BlockPos position) {
        if (client.level == null) return;

        Vec3 cameraPosition = client.gameRenderer.mainCamera().position(); // get camera position

        PoseStack matrices = context.poseStack(); // i assume this is close to CFrames in Roblox
        if (matrices == null) return;

        Matrix4f matrix = matrices.last().pose(); // don't know what this does

        // calculate block postion relative to the camera
        float dx = (float)(position.getX() - cameraPosition.x);
        float dy = (float)(position.getY() - cameraPosition.y);
        float dz = (float)(position.getZ() - cameraPosition.z);

        context.submitNodeCollector().submitCustomGeometry(matrices, RenderTypes.lines(), (renderer, vertex) -> {
            float offset = 0.002f;
            float x1 = dx - offset;
            float x2 = dx + 1.0f + offset;

            float y1 = dy - offset;
            float y2 = dy + 1.0f + offset;

            float z1 = dz - offset;
            float z2 = dz + 1.0f + offset;

            float r = 1.0f;
            float g = 0.0f; 
            float b = 0.0f;
            float a = 0.5f; // using wildbeast42's values

            // cuz ya know, a cube is 12 edges and 6 faces...3rd grade stuff
            drawEdge(vertex, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a); // Bottom-North
            drawEdge(vertex, matrix, x1, y1, z2, x2, y1, z2, r, g, b, a); // Bottom-South
            drawEdge(vertex, matrix, x1, y1, z1, x1, y1, z2, r, g, b, a); // Bottom-West
            drawEdge(vertex, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a); // Bottom-East

            drawEdge(vertex, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a); // Top-North
            drawEdge(vertex, matrix, x1, y2, z2, x2, y2, z2, r, g, b, a); // Top-South
            drawEdge(vertex, matrix, x1, y2, z1, x1, y2, z2, r, g, b, a); // Top-West
            drawEdge(vertex, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a); // Top-East

            drawEdge(vertex, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a); // Side-North
            drawEdge(vertex, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a); // Side-East
            drawEdge(vertex, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a); // Side-West
            drawEdge(vertex, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a); // Side-East
        });
    }

    public static void drawEdge(VertexConsumer vertex, Matrix4f matrix, 
    float x1, float y1, float z1, float x2, float y2, float z2,
    float r, float g, float b, float a) {
        
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        
        float lineWidth = 2.0f;

        // start & end
        vertex.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(lineWidth);
        vertex.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(lineWidth);
    }
}