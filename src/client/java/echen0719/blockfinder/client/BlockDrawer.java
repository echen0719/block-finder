package echen0719.blockfinder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;

import org.joml.Matrix4f;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

// https://modrinth.com/mod/block-hightlightfx saved the day!!

public class BlockDrawer {
    private static Minecraft client = Minecraft.getInstance();

    private static final RenderPipeline seeThroughLines = RenderPipelines.register(RenderPipeline.builder(
        RenderPipelines.LINES_SNIPPET).
        withLocation(Identifier.fromNamespaceAndPath("blockfinder", "pipeline/see_through_lines")).
        withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH).
        withCull(false).withPrimitiveTopology(PrimitiveTopology.LINES).
        withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)
    ).build());

    public static void drawOutline(LevelRenderContext context, BlockPos position) {
        if (client.level == null) return;

        Vec3 cameraPosition = client.gameRenderer.mainCamera().position(); // get camera position

        PoseStack matrices = context.poseStack(); // i assume this is close to CFrames in Roblox
        if (matrices == null) return;

        // calculate block postion relative to the camera
        float dx = (float)(position.getX() - cameraPosition.x);
        float dy = (float)(position.getY() - cameraPosition.y);
        float dz = (float)(position.getZ() - cameraPosition.z);

        // https://github.com/AdvancedXRay/XRay-Mod/blob/main/common/src/main/java/pro/mikey/xray/core/OutlineRender.java
        ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(seeThroughLines.getVertexFormatBinding(0).getVertexSize() * 1024);
        BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, seeThroughLines.getPrimitiveTopology(), seeThroughLines.getVertexFormatBinding(0));
        
        Matrix4f matrix = matrices.last().pose(); // don't know what this does

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

        float lineWidth = 2.0f;

        // cuz ya know, a cube is 12 edges and 6 faces...3rd grade stuff
        drawEdge(bufferBuilder, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a, lineWidth); // Bottom-North
        drawEdge(bufferBuilder, matrix, x1, y1, z2, x2, y1, z2, r, g, b, a, lineWidth); // Bottom-South
        drawEdge(bufferBuilder, matrix, x1, y1, z1, x1, y1, z2, r, g, b, a, lineWidth); // Bottom-West
        drawEdge(bufferBuilder, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a, lineWidth); // Bottom-East

        drawEdge(bufferBuilder, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a, lineWidth); // Top-North
        drawEdge(bufferBuilder, matrix, x1, y2, z2, x2, y2, z2, r, g, b, a, lineWidth); // Top-South
        drawEdge(bufferBuilder, matrix, x1, y2, z1, x1, y2, z2, r, g, b, a, lineWidth); // Top-West
        drawEdge(bufferBuilder, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a, lineWidth); // Top-East

        drawEdge(bufferBuilder, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a, lineWidth); // Side-North
        drawEdge(bufferBuilder, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a, lineWidth); // Side-East
        drawEdge(bufferBuilder, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a, lineWidth); // Side-West
        drawEdge(bufferBuilder, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a, lineWidth); // Side-East

        try (MeshData meshData = bufferBuilder.buildOrThrow()) {
            // do something here to actually render it
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawEdge(BufferBuilder buffer, Matrix4f matrix, 
    float x1, float y1, float z1, float x2, float y2, float z2,
    float r, float g, float b, float a, float lineWidth) {
        
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;

        // start & end
        buffer.addVertex(x1, y1, z1).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(lineWidth);
        buffer.addVertex(x2, y2, z2).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(lineWidth);
    }
}