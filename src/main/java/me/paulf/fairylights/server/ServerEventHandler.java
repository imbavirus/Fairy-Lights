package me.paulf.fairylights.server;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.connection.Connection;

import me.paulf.fairylights.server.item.ConnectionItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import me.paulf.fairylights.server.connection.HangingLightsConnection;
import me.paulf.fairylights.server.jingle.Jingle;
import me.paulf.fairylights.server.jingle.JingleLibrary;
import me.paulf.fairylights.server.jingle.JingleManager;
import net.minecraft.world.level.Level;
import java.util.Optional;

@EventBusSubscriber(modid = FairyLights.ID)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        final Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        CapabilityHandler.getFastenerCapability(player).ifPresent(fastener -> {
            final Optional<Connection> connection = fastener.getFirstConnection();
            if (connection.isPresent()) {
                if (isHoldingConnection(player, connection.get())) {
                    return;
                }
                fastener.removeConnection(connection.get());
            }
        });
    }

    private static boolean isHoldingConnection(final Player player, final Connection connection) {
        return isHoldingConnection(player.getItemInHand(InteractionHand.MAIN_HAND), connection) ||
                isHoldingConnection(player.getItemInHand(InteractionHand.OFF_HAND), connection);
    }

    private static boolean isHoldingConnection(final ItemStack stack, final Connection connection) {
        return stack.getItem() instanceof ConnectionItem &&
                ((ConnectionItem) stack.getItem()).getConnectionType() == connection.getType();
    }

    public static void tryJingle(final Level world, final Connection connection) {
        if (!(connection instanceof HangingLightsConnection)) {
            return;
        }
        final HangingLightsConnection hangingLightsConnection = (HangingLightsConnection) connection;
        final JingleLibrary library = JingleManager.INSTANCE.get(JingleLibrary.CHRISTMAS);
        final Jingle jingle = library.getRandom(world.random, 100);
        if (jingle != null) {
            hangingLightsConnection.play(jingle, 0);
        }
    }
}
