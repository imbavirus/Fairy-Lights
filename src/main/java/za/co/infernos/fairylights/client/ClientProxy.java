package za.co.infernos.fairylights.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import za.co.infernos.fairylights.FairyLights;
import za.co.infernos.fairylights.client.renderer.item.tint.FairyLightTintSource;
import za.co.infernos.fairylights.client.command.JinglerCommand;
import za.co.infernos.fairylights.client.model.light.BowModel;
import za.co.infernos.fairylights.client.model.light.CandleLanternModel;
import za.co.infernos.fairylights.client.model.light.ColorCandleLanternModel;
import za.co.infernos.fairylights.client.model.light.ColorOilLanternModel;
import za.co.infernos.fairylights.client.model.light.FairyLightModel;
import za.co.infernos.fairylights.client.model.light.FlowerLightModel;
import za.co.infernos.fairylights.client.model.light.GhostLightModel;
import za.co.infernos.fairylights.client.model.light.HeartLightModel;
import za.co.infernos.fairylights.client.model.light.IcicleLightsModel;
import za.co.infernos.fairylights.client.model.light.IncandescentLightModel;
import za.co.infernos.fairylights.client.model.light.JackOLanternLightModel;
import za.co.infernos.fairylights.client.model.light.MeteorLightModel;
import za.co.infernos.fairylights.client.model.light.MoonLightModel;
import za.co.infernos.fairylights.client.model.light.OilLanternModel;
import za.co.infernos.fairylights.client.model.light.OrbLanternModel;
import za.co.infernos.fairylights.client.model.light.PaperLanternModel;
import za.co.infernos.fairylights.client.model.light.SkullLightModel;
import za.co.infernos.fairylights.client.model.light.SnowflakeLightModel;
import za.co.infernos.fairylights.client.model.light.SpiderLightModel;
import za.co.infernos.fairylights.client.model.light.StarLightModel;
import za.co.infernos.fairylights.client.model.light.WitchLightModel;
import za.co.infernos.fairylights.client.renderer.block.entity.FastenerBlockEntityRenderer;
import za.co.infernos.fairylights.client.renderer.block.entity.GarlandTinselRenderer;
import za.co.infernos.fairylights.client.renderer.block.entity.GarlandVineRenderer;
import za.co.infernos.fairylights.client.renderer.block.entity.HangingLightsRenderer;
import za.co.infernos.fairylights.client.renderer.block.entity.LetterBuntingRenderer;
import za.co.infernos.fairylights.client.renderer.block.entity.LightBlockEntityRenderer;
import za.co.infernos.fairylights.client.renderer.block.entity.PennantBuntingRenderer;
import za.co.infernos.fairylights.client.renderer.entity.FenceFastenerRenderer;
import za.co.infernos.fairylights.client.tutorial.ClippyController;
import za.co.infernos.fairylights.server.ServerProxy;
import za.co.infernos.fairylights.server.block.entity.FLBlockEntities;
import za.co.infernos.fairylights.server.entity.FLEntities;
import za.co.infernos.fairylights.server.feature.light.ColorChangingBehavior;
import za.co.infernos.fairylights.server.item.DyeableItem;
import za.co.infernos.fairylights.server.item.FLItems;
import za.co.infernos.fairylights.server.item.HangingLightsConnectionItem;
import za.co.infernos.fairylights.server.string.StringTypes;
import za.co.infernos.fairylights.util.styledstring.StyledString;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientProxy extends ServerProxy {
    @SuppressWarnings("deprecation")
    public static final Material SOLID_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "entity/connections"));

    @SuppressWarnings("deprecation")
    public static final Material TRANSLUCENT_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "entity/connections"));

    private final ImmutableList<ResourceLocation> entityModels = new ImmutableList.Builder<ResourceLocation>()
            .addAll(PennantBuntingRenderer.MODELS)
            .addAll(LetterBuntingRenderer.MODELS.values())
            .build();

    @Override
    public void init(final IEventBus modBus) {
        super.init(modBus);
        new ClippyController().init(modBus);
        // ModLoadingContext.registerConfig() changed in NeoForge 1.21.1
        // TODO: Update to use new config registration API
        // ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
        // FLClientConfig.SPEC);
        // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: ClientProxy.init() called - registering client event handlers");
        ClientEventHandler clientEventHandler = new ClientEventHandler();
        NeoForge.EVENT_BUS.register(clientEventHandler);
        // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: ClientEventHandler registered");
        // Hook updateHitConnection to run after the game's pick logic each frame
        // Use EntityTickEvent.Post for LocalPlayer (similar to ClippyController)
        NeoForge.EVENT_BUS.addListener((final net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) -> {
            if (event.getEntity() instanceof net.minecraft.client.player.LocalPlayer) {
                ClientEventHandler.updateHitConnection();
            }
        });
        // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: EntityTickEvent.Post listener registered for LocalPlayer");
        modBus.<RegisterGuiLayersEvent>addListener(e -> {
            // RegisterGuiLayersEvent.registerBelowAll() - comment out for now, needs proper
            // interface
            // TODO: Fix RegisterGuiLayersEvent API compatibility
            // e.registerBelowAll(ResourceLocation.fromNamespaceAndPath(FairyLights.ID,
            // "overlay"),
            // clientEventHandler::renderOverlay);
        });
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent e) -> JinglerCommand.register(e.getDispatcher()));
        // Removed duplicate JinglerCommand.register(NeoForge.EVENT_BUS) call

        modBus.addListener(this::setup);
        modBus.addListener(this::setupLayerDefinitions);
        modBus.addListener(this::setupColors);
        modBus.addListener(this::setupModels);
    }




    private void recomputeUv(final int stride, final int finalUvOffset, final BakedModel model) {
        final TextureAtlasSprite sprite = model.getParticleIcon(ModelData.EMPTY);
        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();
        float uSize = uMax - uMin;
        float vSize = vMax - vMin;

        final int w = (int) (uSize / (sprite.getU1() - sprite.getU0()));
        final int h = (int) (vSize / (sprite.getV1() - sprite.getV0()));
        for (final BakedQuad quad : model.getQuads(null, null, RandomSource.create(42L), ModelData.EMPTY,
                RenderType.cutoutMipped())) {
            final int[] data = quad.getVertices();
            for (int n = 0; n < 4; n++) {
                int iu = n * stride + finalUvOffset;
                int iv = n * stride + finalUvOffset + 1;
                data[iu] = Float.floatToIntBits((float) Math.round(Float.intBitsToFloat(data[iu]) * w) / w);
                data[iv] = Float.floatToIntBits((float) Math.round(Float.intBitsToFloat(data[iv]) * h) / h);
            }
        }
    }

    private void setup(final FMLClientSetupEvent event) {
        BlockEntityRenderers.register(FLBlockEntities.FASTENER.get(),
                context -> new FastenerBlockEntityRenderer(context, ServerProxy.buildBlockView()));
        BlockEntityRenderers.register(FLBlockEntities.LIGHT.get(), LightBlockEntityRenderer::new);
        EntityRenderers.register(FLEntities.FASTENER.get(), FenceFastenerRenderer::new);
        /*
         * final LightRenderer r = new LightRenderer();
         * final StringBuilder bob = new StringBuilder();
         * FLItems.lights().forEach(l -> {
         * final LightModel<?> model = r.getModel(l.getBlock().getVariant(), -1);
         * final AxisAlignedBB bb = model.getBounds();
         * bob.append(String.
         * format("%n%s new AxisAlignedBB(%.3fD, %.3fD, %.3fD, %.3fD, %.3fD, %.3fD), %.3fD"
         * , l.getRegistryName(), bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ,
         * model.getFloorOffset()));
         * });
         * LogManager.getLogger().debug("waldo {}", bob);
         */
    }

    private void setupLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FLModelLayers.BOW, BowModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.GARLAND_RINGS, GarlandVineRenderer.RingsModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.TINSEL_STRIP, GarlandTinselRenderer.StripModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.FAIRY_LIGHT, FairyLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.PAPER_LANTERN, PaperLanternModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.ORB_LANTERN, OrbLanternModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.FLOWER_LIGHT, FlowerLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.CANDLE_LANTERN_LIGHT, ColorCandleLanternModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.OIL_LANTERN_LIGHT, ColorOilLanternModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.JACK_O_LANTERN, JackOLanternLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.SKULL_LIGHT, SkullLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.GHOST_LIGHT, GhostLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.SPIDER_LIGHT, SpiderLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.WITCH_LIGHT, WitchLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.SNOWFLAKE_LIGHT, SnowflakeLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.HEART_LIGHT, HeartLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.MOON_LIGHT, MoonLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.STAR_LIGHT, StarLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.ICICLE_LIGHTS_1, () -> IcicleLightsModel.createLayer(1));
        event.registerLayerDefinition(FLModelLayers.ICICLE_LIGHTS_2, () -> IcicleLightsModel.createLayer(2));
        event.registerLayerDefinition(FLModelLayers.ICICLE_LIGHTS_3, () -> IcicleLightsModel.createLayer(3));
        event.registerLayerDefinition(FLModelLayers.ICICLE_LIGHTS_4, () -> IcicleLightsModel.createLayer(4));
        event.registerLayerDefinition(FLModelLayers.METEOR_LIGHT, MeteorLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.OIL_LANTERN, OilLanternModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.CANDLE_LANTERN, CandleLanternModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.INCANDESCENT_LIGHT, IncandescentLightModel::createLayer);
        event.registerLayerDefinition(FLModelLayers.LETTER_WIRE, LetterBuntingRenderer::wireLayer);
        event.registerLayerDefinition(FLModelLayers.PENNANT_WIRE, PennantBuntingRenderer::wireLayer);
        event.registerLayerDefinition(FLModelLayers.TINSEL_WIRE, GarlandTinselRenderer::wireLayer);
        event.registerLayerDefinition(FLModelLayers.VINE_WIRE, GarlandVineRenderer::wireLayer);
        event.registerLayerDefinition(FLModelLayers.LIGHTS_WIRE, HangingLightsRenderer::wireLayer);
    }

    private void setupModels(final ModelEvent.RegisterAdditional event) {
        // NeoForge 1.21.4 registration for side-loaded models
        event.register(FenceFastenerRenderer.MODEL);
        this.entityModels.forEach(event::register);
    }

    private void setupColors(final net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "light_color"), FairyLightTintSource.CODEC);
        /*
        // Index-based ItemColor registration is gone in 1.21.4
        // Logic should be moved into specific ItemTintSource implementations
        // and models updated to point to them.
        */
    }

    private static int secondLayerColor(final ItemStack stack, final int index) {
        return index == 0 ? 0xFFFFFF : DyeableItem.getColor(stack);
    }
}
