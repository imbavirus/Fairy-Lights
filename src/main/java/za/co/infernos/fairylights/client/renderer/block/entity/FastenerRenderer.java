package za.co.infernos.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import za.co.infernos.fairylights.client.ClientProxy;
import net.minecraft.client.renderer.Sheets;
import za.co.infernos.fairylights.client.FLModelLayers;
import za.co.infernos.fairylights.client.model.light.BowModel;
import za.co.infernos.fairylights.server.connection.Connection;
import za.co.infernos.fairylights.server.connection.GarlandTinselConnection;
import za.co.infernos.fairylights.server.connection.GarlandVineConnection;
import za.co.infernos.fairylights.server.connection.HangingLightsConnection;
import za.co.infernos.fairylights.server.connection.LetterBuntingConnection;
import za.co.infernos.fairylights.server.connection.PennantBuntingConnection;
import za.co.infernos.fairylights.server.fastener.Fastener;
import za.co.infernos.fairylights.server.fastener.FenceFastener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.Tags;

import java.util.function.Function;

public class FastenerRenderer {
    private final HangingLightsRenderer hangingLights;
    private final GarlandVineRenderer garland;
    private final GarlandTinselRenderer tinsel;
    private final PennantBuntingRenderer pennants;
    private final LetterBuntingRenderer letters;
    private final BowModel bow;

    public FastenerRenderer(final Function<ModelLayerLocation, ModelPart> baker) {
        this.hangingLights = new HangingLightsRenderer(baker);
        this.garland = new GarlandVineRenderer(baker);
        this.tinsel = new GarlandTinselRenderer(baker);
        this.pennants = new PennantBuntingRenderer(baker);
        this.letters = new LetterBuntingRenderer(baker);
        this.bow = new BowModel(baker.apply(FLModelLayers.BOW));
    }

    public void render(final Fastener<?> fastener, final float delta, final PoseStack matrix, final MultiBufferSource source, final int packedLight, final int packedOverlay) {
        boolean renderBow = true;
        for (final Connection conn : fastener.getAllConnections()) {
            final java.util.UUID uuid = conn.getUUID();
            final boolean isOrigin = conn.getFastener() == fastener;
            if (za.co.infernos.fairylights.client.ClientEventHandler.RENDERED_CONNECTIONS.add(uuid)) {
                matrix.pushPose();
                if (!isOrigin) {
                    // We are at the destination end - translate to origin to render correctly
                    final net.minecraft.world.phys.Vec3 origin = conn.getFastener().getConnectionPoint();
                    final net.minecraft.world.phys.Vec3 dest = fastener.getConnectionPoint();
                    matrix.translate(origin.x - dest.x, origin.y - dest.y, origin.z - dest.z);
                }
                this.renderConnection(delta, matrix, source, packedLight, packedOverlay, conn);
                matrix.popPose();
            }
            if (renderBow && conn instanceof za.co.infernos.fairylights.server.connection.GarlandVineConnection &&
                    this.renderBow(fastener, matrix, source, packedLight, packedOverlay)) {
                renderBow = false;
            }
        }
    }

    private boolean renderBow(Fastener<?> fastener, PoseStack matrix, MultiBufferSource source, int packedLight, int packedOverlay) {
        if (fastener instanceof FenceFastener) {
            final Level world = fastener.getWorld();
            if (world == null) {
                return false;
            }
            final BlockState state = world.getBlockState(fastener.getPos());
            if (!state.is(Tags.Blocks.FENCES)) {
                return false;
            }
            final VertexConsumer buf = ClientProxy.SOLID_TEXTURE.buffer(source, RenderType::entityCutout);
            final float offset = -1.5F / 16.0F;
            final boolean north = state.getValue(FenceBlock.NORTH);
            final boolean east = state.getValue(FenceBlock.EAST);
            final boolean south = state.getValue(FenceBlock.SOUTH);
            final boolean west = state.getValue(FenceBlock.WEST);
            boolean tryDirX = true;
            boolean bow = false;
            if (!north && (east || west)) {
                this.bow(matrix, Direction.NORTH, offset, buf, packedLight, packedOverlay);
                tryDirX = false;
                bow = true;
            }
            if (!south && (east || west)) {
                this.bow(matrix, Direction.SOUTH, offset, buf, packedLight, packedOverlay);
                tryDirX = false;
                bow = true;
            }
            if (tryDirX) {
                if (!east && (north || south)) {
                    this.bow(matrix, Direction.EAST, offset, buf, packedLight, packedOverlay);
                    bow = true;
                }
                if (!west && (north || south)) {
                    this.bow(matrix, Direction.WEST, offset, buf, packedLight, packedOverlay);
                    bow = true;
                }
            }
            return bow;
        } else if (fastener.getFacing().getAxis() != Direction.Axis.Y) {
            final VertexConsumer buf = ClientProxy.SOLID_TEXTURE.buffer(source, RenderType::entityCutout);
            this.bow(matrix, fastener.getFacing(), 0.0F, buf, packedLight, packedOverlay);
            return true;
        }
        return false;
    }

    private void bow(PoseStack matrix, Direction dir, float offset, VertexConsumer buf, int packedLight, int packedOverlay) {
        matrix.pushPose();
        matrix.mulPose(Axis.YP.rotationDegrees(180.0F - dir.toYRot()));
        if (offset != 0.0F) {
            matrix.translate(0.0D, 0.0D, offset);
        }
        this.bow.renderToBuffer(matrix, buf, packedLight, packedOverlay, 0xFFFFFFFF); // White color
        matrix.popPose();
    }

    private void renderConnection(final float delta, final PoseStack matrix, final MultiBufferSource source, final int packedLight, final int packedOverlay, final Connection conn) {
        if (conn instanceof HangingLightsConnection) {
            this.hangingLights.render((HangingLightsConnection) conn, delta, matrix, source, packedLight, packedOverlay);
        } else if (conn instanceof GarlandVineConnection) {
            this.garland.render((GarlandVineConnection) conn, delta, matrix, source, packedLight, packedOverlay);
        } else if (conn instanceof GarlandTinselConnection) {
            this.tinsel.render((GarlandTinselConnection) conn, delta, matrix, source, packedLight, packedOverlay);
        } else if (conn instanceof PennantBuntingConnection) {
            this.pennants.render((PennantBuntingConnection) conn, delta, matrix, source, packedLight, packedOverlay);
        } else if (conn instanceof LetterBuntingConnection) {
            this.letters.render((LetterBuntingConnection) conn, delta, matrix, source, packedLight, packedOverlay);
        }
    }

    public static void renderBakedModel(final ResourceLocation path, final PoseStack matrix, final MultiBufferSource source, final float r, final float g, final float b, final int packedLight, final int packedOverlay) {
        final net.minecraft.client.resources.model.ModelResourceLocation modelLoc = new net.minecraft.client.resources.model.ModelResourceLocation(path, "standalone");
        final BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLoc);
        if (model == null || model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            return;
        }
        // BakedQuad UVs reference the block texture atlas, so we must use a RenderType that uses LOCATION_BLOCKS
        final VertexConsumer buf = source.getBuffer(Sheets.cutoutBlockSheet());
        renderBakedModel(model, matrix, buf, r, g, b, packedLight, packedOverlay);
    }

    public static void renderBakedModel(final BakedModel model, final PoseStack matrix, final VertexConsumer buf, final float r, final float g, final float b, final int packedLight, final int packedOverlay) {
        renderBakedModel(model, ItemDisplayContext.FIXED, matrix, buf, r, g, b, packedLight, packedOverlay);
    }

    @SuppressWarnings("deprecation")
    public static void renderBakedModel(final BakedModel model, final ItemDisplayContext type, final PoseStack matrix, final VertexConsumer buf, final float r, final float g, final float b, final int packedLight, final int packedOverlay) {
        // Apply display transform (fence_fastener FIXED is -8,-8,-8 = center on entity).
        // Without this the grey cube renders a half-block off and looks like a floating Fastener.
        matrix.pushPose();
        model.getTransforms().getTransform(type).apply(false, matrix);

        final PoseStack.Pose pose = matrix.last();
        final RandomSource randSource = RandomSource.create(42L);

        // Render quads for each direction
        for (final Direction side : Direction.values()) {
            randSource.setSeed(42L);
            for (final BakedQuad quad : model.getQuads(null, side, randSource, ModelData.EMPTY, null)) {
                buf.putBulkData(pose, quad, r, g, b, 1.0F, packedLight, packedOverlay);
            }
        }

        // Render general quads (no specific side)
        randSource.setSeed(42L);
        for (final BakedQuad quad : model.getQuads(null, null, randSource, ModelData.EMPTY, null)) {
            buf.putBulkData(pose, quad, r, g, b, 1.0F, packedLight, packedOverlay);
        }
        matrix.popPose();
    }
}
