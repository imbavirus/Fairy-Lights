package me.paulf.fairylights.server.net.clientbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.Lettered;
import me.paulf.fairylights.server.net.ClientMessageContext;
import me.paulf.fairylights.server.net.ConnectionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

public class OpenEditLetteredConnectionScreenMessage<C extends Connection & Lettered> extends ConnectionMessage
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenEditLetteredConnectionScreenMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "open_edit_lettered_connection_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenEditLetteredConnectionScreenMessage> STREAM_CODEC = StreamCodec
            .of(
                    (buf, msg) -> msg.encode(buf),
                    OpenEditLetteredConnectionScreenMessage::new);

    // Constructor for decoding
    private OpenEditLetteredConnectionScreenMessage(final RegistryFriendlyByteBuf buf) {
        this.decode(buf);
    }

    public OpenEditLetteredConnectionScreenMessage() {
    }

    public OpenEditLetteredConnectionScreenMessage(final C connection) {
        super(connection);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final OpenEditLetteredConnectionScreenMessage message,
            final net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            ConnectionMessage.getConnection(message, c -> c instanceof Lettered, Minecraft.getInstance().level)
                    .ifPresent(connection -> {
                        // ConnectionMessage.getConnection returns Connection, we check instanceof
                        // Lettered but casting to C (generic) is tricky
                        // But EditLetteredConnectionScreen expects C extends Connection & Lettered
                        // We know it is Lettered from trace.
                        Minecraft.getInstance()
                                .setScreen(new EditLetteredConnectionScreen<>((Connection & Lettered) connection));
                    });
        });
    }

    // Legacy handler kept if needed by other systems (NetBuilder), but
    // PayloadHandler uses handle() static method usually
    public static final class Handler
            implements BiConsumer<OpenEditLetteredConnectionScreenMessage<?>, ClientMessageContext> {
        @Override
        public void accept(final OpenEditLetteredConnectionScreenMessage<?> message,
                final ClientMessageContext context) {
            // This legacy handler might not be needed if we switch fully to IPayloadContext
        }
    }
}
