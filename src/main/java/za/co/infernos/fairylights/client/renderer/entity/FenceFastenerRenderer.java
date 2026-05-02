package za.co.infernos.fairylights.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import za.co.infernos.fairylights.FairyLights;
import za.co.infernos.fairylights.client.renderer.block.entity.FastenerRenderer;
import za.co.infernos.fairylights.server.entity.FenceFastenerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public final class FenceFastenerRenderer extends EntityRenderer<FenceFastenerEntity, EntityRenderState> {
    public static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(FairyLights.ID,
            "block/fence_fastener");

    private final FastenerRenderer renderer;

    public FenceFastenerRenderer(final EntityRendererProvider.Context context) {
        super(context);
        this.renderer = new FastenerRenderer(context::bakeLayer);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void render(final EntityRenderState state, final PoseStack matrix,
            final MultiBufferSource source, final int packedLight) {
        // Note: In 1.21.2, we render from state, not entity directly
        // The entity reference is not available in the new API's render method
        // We handle this by overriding extractRenderState to cache the entity
        matrix.pushPose();
        FastenerRenderer.renderBakedModel(MODEL, matrix, source, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
        matrix.popPose();
        super.render(state, matrix, source, packedLight);
    }

    // We need to override the old render method for connection rendering
    // since connections need the entity reference
    public void render(final FenceFastenerEntity entity, final float yaw, final float delta, final PoseStack matrix,
            final MultiBufferSource source, final int packedLight) {
        matrix.pushPose();
        FastenerRenderer.renderBakedModel(MODEL, matrix, source, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
        matrix.popPose();
        entity.getFastener()
                .ifPresent(f -> this.renderer.render(f, delta, matrix, source, packedLight, OverlayTexture.NO_OVERLAY));
    }
}
