package echen0719.blockfinder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
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
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.OptionalDouble;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

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

    private static int indexCount = 0;
    private static GpuBuffer vertexBuffer = null;

    private static final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.LINES);

    private static float r = 1.0f;
    private static float g = 0f;
    private static float b = 0f;
    private static float a = 0.5f;

    public static void setColor(Object[] color) {
        r = ((Integer) color[0]).floatValue() / 255;
        g = ((Integer) color[1]).floatValue() / 255;
        b = ((Integer) color[2]).floatValue() / 255;
        a = (Float) color[3];

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }

    public static void clear() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        indexCount = 0;
    }

    private static void initBuffer() {
        if (vertexBuffer != null) return;

        // https://github.com/AdvancedXRay/XRay-Mod/blob/main/common/src/main/java/pro/mikey/xray/core/OutlineRender.java
        ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(seeThroughLines.getVertexFormatBinding(0).getVertexSize() * 1024);
        BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, seeThroughLines.getPrimitiveTopology(), seeThroughLines.getVertexFormatBinding(0));

        Matrix4f matrix = new Matrix4f(); 

        float offset = 0.002f;
        float x1 = 0 - offset;
        float x2 = 1 + offset;

        float y1 = 0 - offset;
        float y2 = 1 + offset;

        float z1 = 0 - offset;
        float z2 = 1 + offset;

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
            indexCount = meshData.drawState().indexCount();
            vertexBuffer = RenderSystem.getDevice().createBuffer(() -> 
                "blockfinder buffer", GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer()
            );
        }
    }

    public static void drawOutline(LevelRenderContext context, BlockPos position) {
        if (client.level == null) return;

        initBuffer();

        PoseStack matrices = context.poseStack(); // i assume this is close to CFrames in Roblox
        if (matrices == null) return;

        Vec3 cameraPosition = client.gameRenderer.mainCamera().position();

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();

        matrices.pushPose();
        modelViewStack.translate(
            (float)(position.getX() - cameraPosition.x), 
            (float)(position.getY() - cameraPosition.y), 
            (float)(position.getZ() - cameraPosition.z)
        );
        
        Matrix4f matrix = new Matrix4f(modelViewStack); // don't know what this does
        modelViewStack.popMatrix();

        GpuBufferSlice[] gpubufferslice = RenderSystem.getDynamicUniforms().writeTransforms(
            new DynamicUniforms.Transform(
                matrix,
                new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), 
                new Vector3f(), 
                new Matrix4f()
            )
        );

        var colorTextureView = client.gameRenderer.mainRenderTarget().getColorTextureView();
        var depthTextureView = client.gameRenderer.mainRenderTarget().getDepthTextureView();

        try (RenderPass renderPass = RenderSystem.getDevice().
            createCommandEncoder().createRenderPass(() -> 
            "blockfinder_outline", colorTextureView, 
            Optional.empty(), depthTextureView, OptionalDouble.empty())) {
            
            RenderSystem.bindDefaultUniforms(renderPass);

            renderPass.setVertexBuffer(0, vertexBuffer.slice());
            renderPass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
            renderPass.setUniform("DynamicTransforms", gpubufferslice[0]);
            renderPass.setPipeline(seeThroughLines);

            renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void drawEdge(BufferBuilder buffer, Matrix4f matrix, 
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