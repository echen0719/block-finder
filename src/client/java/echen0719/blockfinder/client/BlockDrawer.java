package echen0719.blockfinder.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.core.BlockPos;

// https://modrinth.com/mod/block-hightlightfx saved the day!!

public class BlockDrawer {
    public static void drawOutline(LevelRenderContext context, BlockPos position) {
        // get camera
        // do stuff
        // get stuff

        drawEdge(vertex, matrix, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, b, a); // bottom
		drawEdge(vertex, matrix, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, r, g, b, a); // top
		drawEdge(vertex, matrix, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, r, g, b, a); // north
		drawEdge(vertex, matrix, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r, g, b, a); // south
		drawEdge(vertex, matrix, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r, g, b, a); // west
		drawEdge(vertex, matrix, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, r ,g, b, a); // east
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