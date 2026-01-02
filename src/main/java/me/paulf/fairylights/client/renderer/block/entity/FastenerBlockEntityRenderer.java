package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.paulf.fairylights.server.block.entity.FastenerBlockEntity;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.fastener.BlockView;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public final class FastenerBlockEntityRenderer implements BlockEntityRenderer<FastenerBlockEntity> {

    private final BlockView view;
    private final FastenerRenderer renderer;

    public FastenerBlockEntityRenderer(final BlockEntityRendererProvider.Context context, final BlockView view) {
        this.view = view;
        this.renderer = new FastenerRenderer(context::bakeLayer);
    }

    @Override
    public boolean shouldRenderOffScreen(final FastenerBlockEntity fastener) {
        return true;
    }

    @Override
    public void render(final FastenerBlockEntity fastener, final float delta, final PoseStack matrix, final MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
        // getCapability() API changed in 1.21.1 - use reflection to access private getFastener()
        try {
            final java.lang.reflect.Method getFastener = fastener.getClass().getDeclaredMethod("getFastener");
            getFastener.setAccessible(true);
            @SuppressWarnings("unchecked")
            final java.util.Optional<me.paulf.fairylights.server.fastener.Fastener<?>> fastenerOpt = (java.util.Optional<me.paulf.fairylights.server.fastener.Fastener<?>>) getFastener.invoke(fastener);
            fastenerOpt.ifPresent(f -> {
            //this.bindTexture(FastenerRenderer.TEXTURE);
            matrix.pushPose();
            final Vec3 offset = fastener.getOffset();
            matrix.translate(offset.x, offset.y, offset.z);
            //this.view.unrotate(this.getWorld(), f.getPos(), FastenerBlockEntityRenderer.GlMatrix.INSTANCE, delta);
            this.renderer.render(f, delta, matrix, bufferSource, packedLight, packedOverlay);
            matrix.popPose();
            });
        } catch (Exception e) {
            // getFastener() not accessible - skip rendering
        }
    }
}
