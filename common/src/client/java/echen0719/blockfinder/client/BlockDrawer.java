package echen0719.blockfinder.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BlockDrawer {
    private static Minecraft client = Minecraft.getInstance();

    // caching instead of rebuilding every update
    private static Map<Integer, VertexBuffer> vertexBufferCache = new HashMap<>();
    private static Map<Integer, VertexBuffer> tracerVertexBufferCache = new HashMap<>();

    public static int getColor(Object[] color) {
        int r = (Integer) color[0];
        int g = (Integer) color[1];
        int b = (Integer) color[2];
        int a = (int) (((Float) color[3]) * 255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void clear() {
        for (VertexBuffer buffer : vertexBufferCache.values()) {
            buffer.close();
        }
        vertexBufferCache.clear();

        for (VertexBuffer buffer : tracerVertexBufferCache.values()) {
            buffer.close();
        }
        tracerVertexBufferCache.clear();
    }

    private static void initBuffer(Object[] color) {
        int colorKey = getColor(color);
        if (vertexBufferCache.containsKey(colorKey)) return; // if color is already cached

        vertexBufferCache.put(colorKey, new VertexBuffer(VertexBuffer.Usage.DYNAMIC));
        tracerVertexBufferCache.put(colorKey, new VertexBuffer(VertexBuffer.Usage.DYNAMIC));
    }

    private static BufferBuilder initBuilder() {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        return builder;
    }

    public static void drawOutline(PoseStack matrices, List<BlockPos> positions, Object[] color) {
        if (client.level == null || positions == null || positions.isEmpty()) return;

        int colorKey = getColor(color);
        if (!vertexBufferCache.containsKey(colorKey)) {
            initBuffer(color);
        }
        VertexBuffer vertexBuffer = vertexBufferCache.get(colorKey);

        float r = ((Integer) color[0]) / 255.0f; 
        float g = ((Integer) color[1]) / 255.0f; 
        float b = ((Integer) color[2]) / 255.0f; 
        float a = (Float) color[3];
        
        Vec3 cameraPosition = client.gameRenderer.getMainCamera().getPosition();
        BufferBuilder builder = initBuilder();

        for (BlockPos position : positions) {
            float offset = 0.002f;
            float x1 = (float) (position.getX() - cameraPosition.x) - offset;
            float x2 = (float) (position.getX() - cameraPosition.x) + 1 + offset;

            float y1 = (float) (position.getY() - cameraPosition.y) - offset;
            float y2 = (float) (position.getY() - cameraPosition.y) + 1 + offset;

            float z1 = (float) (position.getZ() - cameraPosition.z) - offset;
            float z2 = (float) (position.getZ() - cameraPosition.z) + 1 + offset;

            // cuz ya know, a cube is 12 edges and 6 faces...3rd grade stuff
            drawEdge(builder, x1, y1, z1, x2, y1, z1, r, g, b, a); // Bottom-North
            drawEdge(builder, x1, y1, z2, x2, y1, z2, r, g, b, a); // Bottom-South
            drawEdge(builder, x1, y1, z1, x1, y1, z2, r, g, b, a); // Bottom-West
            drawEdge(builder, x2, y1, z1, x2, y1, z2, r, g, b, a); // Bottom-East

            drawEdge(builder, x1, y2, z1, x2, y2, z1, r, g, b, a); // Top-North
            drawEdge(builder, x1, y2, z2, x2, y2, z2, r, g, b, a); // Top-South
            drawEdge(builder, x1, y2, z1, x1, y2, z2, r, g, b, a); // Top-West
            drawEdge(builder, x2, y2, z1, x2, y2, z2, r, g, b, a); // Top-East

            drawEdge(builder, x1, y1, z1, x1, y2, z1, r, g, b, a); // Side-North
            drawEdge(builder, x2, y1, z1, x2, y2, z1, r, g, b, a); // Side-East
            drawEdge(builder, x1, y1, z2, x1, y2, z2, r, g, b, a); // Side-West
            drawEdge(builder, x2, y1, z2, x2, y2, z2, r, g, b, a); // Side-East
        }

        BufferBuilder.RenderedBuffer mesh = builder.end();
        vertexBuffer.bind(); 
        vertexBuffer.upload(mesh);

        // defined here instead of in init
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false); 

        RenderSystem.lineWidth(3.0f);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);

        vertexBuffer.drawWithShader(
            matrices.last().pose(),
            RenderSystem.getProjectionMatrix(),
            GameRenderer.getRendertypeLinesShader() 
        );
        VertexBuffer.unbind();

        RenderSystem.depthMask(true); // restore old values
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawTracerLines(PoseStack matrices, List<BlockPos> positions, Object[] color) {
        if (client.level == null || positions == null || positions.isEmpty()) return;

        int colorKey = getColor(color);
        if (!tracerVertexBufferCache.containsKey(colorKey)) {
            initBuffer(color);
        }
        VertexBuffer vertexBuffer = tracerVertexBufferCache.get(colorKey);

        float r = ((Integer) color[0]) / 255.0f; 
        float g = ((Integer) color[1]) / 255.0f; 
        float b = ((Integer) color[2]) / 255.0f; 
        float a = (Float) color[3];
        
        Vec3 cameraPosition = client.gameRenderer.getMainCamera().getPosition();
        Vec3 cameraAngles = client.player.getLookAngle(); // look angle is for camera, head angle is for 3D stuff
        Vec3 startPosition = cameraPosition.add(cameraAngles.scale(0.25)); // slightly in front

        BufferBuilder builder = initBuilder();

        for (BlockPos position : positions) {
            Vec3 targetPosition = Vec3.atCenterOf(position); // get center of block

            float dx = (float)(targetPosition.x - startPosition.x);
            float dy = (float)(targetPosition.y - startPosition.y);
            float dz = (float)(targetPosition.z - startPosition.z);

            float startX = (float) (startPosition.x - cameraPosition.x);
            float startY = (float) (startPosition.y - cameraPosition.y);
            float startZ = (float) (startPosition.z - cameraPosition.z);

            drawEdge(builder, startX, startY, startZ, dx, dy, dz, r, g, b, a);
        }

        BufferBuilder.RenderedBuffer mesh = builder.end();
        vertexBuffer.bind(); 
        vertexBuffer.upload(mesh);

        // defined here instead of in init
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.lineWidth(3.0f);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);

        vertexBuffer.drawWithShader(
            matrices.last().pose(), // for some reason this over RenderSystem.getModelViewMatrix()
            RenderSystem.getProjectionMatrix(),
            GameRenderer.getRendertypeLinesShader() 
        );
        VertexBuffer.unbind();

        RenderSystem.depthMask(true); // restore old values
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawEdge(BufferBuilder buffer, 
    float x1, float y1, float z1, float x2, float y2, float z2,
    float r, float g, float b, float a) {
        // start & end
        buffer.vertex(x1, y1, z1).color(r, g, b, a).normal(1, 1, 1).endVertex();
        buffer.vertex(x2, y2, z2).color(r, g, b, a).normal(1, 1, 1).endVertex();
    }
}