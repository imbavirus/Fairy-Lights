package me.paulf.fairylights.server;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.fastener.BlockView;
import me.paulf.fairylights.server.fastener.CreateBlockViewEvent;
import me.paulf.fairylights.server.fastener.RegularBlockView;
import me.paulf.fairylights.server.jingle.JingleManager;
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
        NeoForge.EVENT_BUS.register(new ServerEventHandler());
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
        
        // Get the network builder
        final me.paulf.fairylights.server.net.NetBuilder network = (me.paulf.fairylights.server.net.NetBuilder) FairyLights.NETWORK;
        if (network == null) {
            return;
        }
        
        // Send to all players tracking the entity (including the entity itself if it's a player)
        for (net.minecraft.server.level.ServerPlayer player : serverLevel.getPlayers(p -> {
            // Check if player is tracking the entity (within reasonable distance)
            return p.distanceToSqr(entity) < 64 * 64 || p.equals(entity);
        })) {
            try {
                // Prefer our message path first (UpdateEntityFastenerMessage implements Message + CustomPacketPayload)
                if (message instanceof me.paulf.fairylights.server.net.Message) {
                    network.sendToClient((me.paulf.fairylights.server.net.Message) message, player);
                    continue;
                }

                // Vanilla fallback: wrap payload into a packet and send
                if (message instanceof net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
                    player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(payload));
                }
            } catch (Exception e) {
                // Networking API not available - message won't be sent
                // Connection will sync on next tick via regular update mechanism
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
