package za.co.infernos.fairylights.client.net;

import za.co.infernos.fairylights.client.gui.EditLetteredConnectionScreen;
import za.co.infernos.fairylights.server.connection.Connection;
import za.co.infernos.fairylights.server.connection.HangingLightsConnection;
import za.co.infernos.fairylights.server.connection.Lettered;
import za.co.infernos.fairylights.server.jingle.Jingle;
import za.co.infernos.fairylights.server.net.ClientMessageContext;
import za.co.infernos.fairylights.server.net.ConnectionMessage;
import za.co.infernos.fairylights.server.net.clientbound.JingleMessage;
import za.co.infernos.fairylights.server.net.clientbound.OpenEditLetteredConnectionScreenMessage;
import za.co.infernos.fairylights.server.net.clientbound.UpdateEntityFastenerMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class ClientMessageHandlers {
    public static void handleOpenScreen(final OpenEditLetteredConnectionScreenMessage<?> message, final ClientMessageContext context) {
        ConnectionMessage.getConnection(message, c -> c instanceof Lettered, Minecraft.getInstance().level)
                .ifPresent(connection -> {
                    Minecraft.getInstance().setScreen(new EditLetteredConnectionScreen<>((Connection & Lettered) connection));
                });
    }

    public static void handleJingle(final JingleMessage message, final ClientMessageContext context) {
        final Jingle jingle = message.jingle;
        if (jingle != null) {
            ConnectionMessage.<HangingLightsConnection>getConnection(message, c -> c instanceof HangingLightsConnection, Minecraft.getInstance().level).ifPresent(connection ->
                connection.play(jingle, message.getLightOffset()));
        }
    }

    public static void handleUpdateEntityFastener(final UpdateEntityFastenerMessage message, final ClientMessageContext context) {
        final Entity entity = Minecraft.getInstance().level.getEntity(message.getEntityId());
        if (entity != null) {
            za.co.infernos.fairylights.server.capability.CapabilityHandler.getFastenerCapability(entity).ifPresent(fastener ->
                    fastener.deserializeNBT(message.getCompound(), Minecraft.getInstance().level.registryAccess()));
        }
    }
}
