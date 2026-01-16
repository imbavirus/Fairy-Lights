package me.paulf.fairylights.server.net.clientbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.net.ClientMessageContext;
import me.paulf.fairylights.server.net.ConnectionMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.Lettered;
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

    // Removed handle method and Handler class to avoid client-side dependencies.
    // Logic moved to ClientMessageHandlers.
}
