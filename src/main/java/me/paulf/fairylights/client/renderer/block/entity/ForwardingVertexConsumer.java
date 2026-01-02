package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;

public abstract class ForwardingVertexConsumer implements VertexConsumer {
    protected abstract VertexConsumer delegate();

    // Implement VertexConsumer API methods - in 1.21.1, addVertex is part of the interface
    public VertexConsumer addVertex(double x, double y, double z) {
        return this.delegate().addVertex((float)x, (float)y, (float)z);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return this.delegate().addVertex(x, y, z);
    }

    // In 1.21.1, VertexConsumer interface has these methods - forward directly
    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return this.delegate().setColor(r, g, b, a);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return this.delegate().setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this.delegate().setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this.delegate().setUv2(u, v);
    }

    @Override
    public VertexConsumer setOverlay(int uv) {
        return this.delegate().setOverlay(uv);
    }

    @Override
    public VertexConsumer setLight(int uv) {
        return this.delegate().setLight(uv);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return this.delegate().setNormal(x, y, z);
    }
}
