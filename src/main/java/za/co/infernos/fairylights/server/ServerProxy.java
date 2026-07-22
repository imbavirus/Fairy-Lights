package za.co.infernos.fairylights.server;

import za.co.infernos.fairylights.FairyLights;
import za.co.infernos.fairylights.server.capability.CapabilityHandler;
import za.co.infernos.fairylights.server.fastener.BlockView;
import za.co.infernos.fairylights.server.fastener.CreateBlockViewEvent;
import za.co.infernos.fairylights.server.fastener.RegularBlockView;
import za.co.infernos.fairylights.server.jingle.JingleManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
// PacketDistributor removed in NeoForge 1.21.1 - using PayloadRegistrar instead

public class ServerProxy {
    public void init(final IEventBus modBus) {
        // ModLoadingContext.registerConfig() changed in NeoForge 1.21.1
        // TODO: Update to use new config registration API
        // ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FLConfig.GENERAL_SPEC);
        NeoForge.EVENT_BUS.<AddReloadListenerEvent>addListener(e -> {
            e.addListener(JingleManager.INSTANCE);
        });

        modBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityHandler.register();
    }

    public static void sendToPlayersWatchingChunk(final Object message, final Level world, final BlockPos pos) {
        // TODO: Rewrite to use PayloadRegistrar API for NeoForge 1.21.1
        // FairyLights.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), message);
    }

    public static void sendToPlayersWatchingEntity(final Object message, final Entity entity) {
        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        if (!(message instanceof net.minecraft.network.protocol.common.custom.CustomPacketPayload payload)) {
            return;
        }

        // Must send a real ClientboundCustomPayloadPacket. NetBuilder.sendToClient only invokes
        // handlers in-process and never reaches the client (fence tether rope stayed invisible).
        for (net.minecraft.server.level.ServerPlayer player : serverLevel.getPlayers(p ->
                p.distanceToSqr(entity) < 64 * 64 || p.equals(entity))) {
            try {
                player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(payload));
            } catch (Exception ignored) {
            }
        }
    }

    public static BlockView buildBlockView() {
        final CreateBlockViewEvent evt = new CreateBlockViewEvent(new RegularBlockView());
        NeoForge.EVENT_BUS.post(evt);
        return evt.getView();
    }

    public void initIntegration() {
		/*if (Loader.isModLoaded(ValkyrienWarfareMod.MODID)) {
			final Class<?> vw;
			try {
				vw = Class.forName("ValkyrienWarfare");
			} catch (final ClassNotFoundException e) {
				throw new AssertionError(e);
			}
			NeoForge.EVENT_BUS.register(vw);
		}*/
    }
}
