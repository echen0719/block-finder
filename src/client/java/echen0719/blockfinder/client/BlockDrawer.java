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

        // calculate block postion relative to the camera
        matrices.pushPose();
        matrices.translate(
            position.getX() - cameraPosition.x, 
            position.getY() - cameraPosition.y, 
            position.getZ() - cameraPosition.z
        );

        context.submitNodeCollector().submitCustomGeometry(matrices, RenderTypes.lines(), (renderer, vertex) -> {
            Matrix4f matrix = matrices.last().pose(); // don't know what this does

            float offset = 0.002f;
            float x1 = 0.0f - offset, x2 = 1.0f + offset;
            float y1 = 0.0f - offset, y2 = 1.0f + offset;
            float z1 = 0.0f - offset, z2 = 1.0f + offset;

            float r = 1.0f;
            float g = 0.0f; 
            float b = 0.0f;
            float a = 0.5f; // using wildbeast42's values

            drawEdge(vertex, matrix, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, b, a); // bottom
            drawEdge(vertex, matrix, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, r, g, b, a); // top
            drawEdge(vertex, matrix, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, r, g, b, a); // north
            drawEdge(vertex, matrix, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r, g, b, a); // south
            drawEdge(vertex, matrix, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r, g, b, a); // west
            drawEdge(vertex, matrix, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, r ,g, b, a); // east
        });

        matrices.popPose();
    }

    public static void drawEdge(VertexConsumer vertex, Matrix4f matrix, 
    float x1, float y1, float z1, float x2, float y2, float z2, 
    float x3, float y3, float z3, float x4, float y4, float z4, 
    float r, float g, float b, float a) { // draws from vertices
        vertex.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
		vertex.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
		vertex.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
		vertex.addVertex(matrix, x4, y4, z4).setColor(r, g, b, a);
    }
}