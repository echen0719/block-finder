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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

// https://modrinth.com/mod/block-hightlightfx saved the day!!

public class BlockDrawer {
    private static Minecraft client = Minecraft.getInstance();

    private static final RenderPipeline seeThroughLines = RenderPipeline.builder(
        RenderPipelines.LINES_SNIPPET).
        withLocation(Identifier.fromNamespaceAndPath("blockfinder", "pipeline/see_through_lines")).
        withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH).
        withCull(false).withPrimitiveTopology(PrimitiveTopology.LINES).
        withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)
    ).build();

    // caching instead of rebuilding every update
    private static Map<Integer, Integer> indexCountCache = new HashMap<>();
    private static Map<Integer, GpuBuffer> vertexBufferCache = new HashMap<>();

    private static Map<Integer, Integer> tracerIndexCountCache = new HashMap<>();
    private static Map<Integer, GpuBuffer> tracerVertexBufferCache = new HashMap<>();

    private static final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.LINES);

    public static int getColor(Object[] color) {
        int r = (Integer) color[0];
        int g = (Integer) color[1];
        int b = (Integer) color[2];
        int a = (int) (((Float) color[3]) * 255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void clear() {
        for (GpuBuffer buffer : vertexBufferCache.values()) {
            buffer.close();
        }
        vertexBufferCache.clear();
        indexCountCache.clear();

        for (GpuBuffer buffer : tracerVertexBufferCache.values()) {
            buffer.close();
        }
        tracerVertexBufferCache.clear();
        tracerIndexCountCache.clear();
    }

    private static void initBuffer(Object[] color) {
        int colorKey = getColor(color);
        if (vertexBufferCache.containsKey(colorKey)) return; // if color is already cached

        float r = ((Integer) color[0]).floatValue() / 255;
        float g = ((Integer) color[1]).floatValue() / 255;
        float b = ((Integer) color[2]).floatValue() / 255;
        float a = (Float) color[3];

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
            int indexCount = meshData.drawState().indexCount();
            GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(() -> 
                "blockfinder_buffer " + colorKey, GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer()
            );

            vertexBufferCache.put(colorKey, vertexBuffer);
            indexCountCache.put(colorKey, indexCount);
        }

        ByteBufferBuilder tracerByteBufferBuilder = new ByteBufferBuilder(seeThroughLines.getVertexFormatBinding(0).getVertexSize() * 1024);
        BufferBuilder tracerBufferBuilder = new BufferBuilder(tracerByteBufferBuilder, seeThroughLines.getPrimitiveTopology(), seeThroughLines.getVertexFormatBinding(0));
        
        Matrix4f tracerMatrix = new Matrix4f();
        drawEdge(tracerBufferBuilder, tracerMatrix, 0, 0, 0, 1, 1, 1, r, g, b, a, lineWidth);

        try (MeshData tracerMeshData = tracerBufferBuilder.buildOrThrow()) {
            int tracerIndexCount = tracerMeshData.drawState().indexCount();
            GpuBuffer tracerVertexBuffer = RenderSystem.getDevice().createBuffer(() -> 
                "blockfinder_tracer_buffer " + colorKey, GpuBuffer.USAGE_VERTEX, tracerMeshData.vertexBuffer()
            );

            tracerVertexBufferCache.put(colorKey, tracerVertexBuffer);
            tracerIndexCountCache.put(colorKey, tracerIndexCount);
        }
    }

    public static void drawOutline(RenderLevelStageEvent context, List<BlockPos> positions, Object[] color) {
        if (client.level == null || positions == null || positions.isEmpty()) return;
        
        int colorKey = getColor(color);

        if (!vertexBufferCache.containsKey(colorKey)) {
            initBuffer(color);
        }

        GpuBuffer vertexBuffer = vertexBufferCache.get(colorKey);
        int indexCount = indexCountCache.get(colorKey);

        PoseStack matrices = context.getPoseStack(); // i assume this is close to CFrames in Roblox
        if (matrices == null) return;

        Vec3 cameraPosition = client.gameRenderer.mainCamera().position();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();

        var colorTextureView = client.gameRenderer.mainRenderTarget().getColorTextureView();
        var depthTextureView = client.gameRenderer.mainRenderTarget().getDepthTextureView();

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> 
        "blockfinder_outline", colorTextureView, Optional.empty(), depthTextureView, OptionalDouble.empty())) { 
            RenderSystem.bindDefaultUniforms(renderPass);

            renderPass.setVertexBuffer(0, vertexBuffer.slice());
            renderPass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
            renderPass.setPipeline(seeThroughLines);

            for (BlockPos position : positions) {
                modelViewStack.pushMatrix();
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

                renderPass.setUniform("DynamicTransforms", gpubufferslice[0]);
                renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawTracerLines(RenderLevelStageEvent context, List<BlockPos> positions, Object[] color) {
        if (client.level == null || positions == null || positions.isEmpty()) return;
        
        int colorKey = getColor(color);
        
        if (!tracerVertexBufferCache.containsKey(colorKey)) {
            initBuffer(color);
        }

        GpuBuffer tracerVertexBuffer = tracerVertexBufferCache.get(colorKey);
        int tracerIndexCount = tracerIndexCountCache.get(colorKey);
        
        PoseStack matrices = context.getPoseStack();
        if (matrices == null) return;

        Vec3 cameraPosition = client.gameRenderer.mainCamera().position();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();

        var colorTextureView = client.gameRenderer.mainRenderTarget().getColorTextureView();
        var depthTextureView = client.gameRenderer.mainRenderTarget().getDepthTextureView();
        
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> 
        "blockfinder_outline", colorTextureView, Optional.empty(), depthTextureView, OptionalDouble.empty())) { 
            RenderSystem.bindDefaultUniforms(renderPass);
            
            renderPass.setVertexBuffer(0, tracerVertexBuffer.slice());
            renderPass.setIndexBuffer(indices.getBuffer(tracerIndexCount), indices.type());
            renderPass.setPipeline(seeThroughLines);
            
            for (BlockPos position : positions) {
                float dx = (float)(position.getX() + 0.5 - cameraPosition.x);
                float dy = (float)(position.getY() + 0.5 - cameraPosition.y);
                float dz = (float)(position.getZ() + 0.5 - cameraPosition.z);

                modelViewStack.pushMatrix();
                modelViewStack.scale(dx, dy, dz); // create unit line and stretch it to camera

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
                renderPass.setUniform("DynamicTransforms", gpubufferslice[0]);
                renderPass.drawIndexed(tracerIndexCount, 1, 0, 0, 0);
            }
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