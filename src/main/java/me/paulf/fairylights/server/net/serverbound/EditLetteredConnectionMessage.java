package me.paulf.fairylights.server.net.serverbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.Lettered;
import me.paulf.fairylights.server.net.ConnectionMessage;
import me.paulf.fairylights.server.net.ServerMessageContext;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public class EditLetteredConnectionMessage<C extends Connection & Lettered> extends ConnectionMessage
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EditLetteredConnectionMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "edit_lettered_connection"));

    @SuppressWarnings("rawtypes")
    public static final StreamCodec<RegistryFriendlyByteBuf, EditLetteredConnectionMessage> STREAM_CODEC = StreamCodec
            .of(
                    (buf, msg) -> msg.encode(buf),
                    EditLetteredConnectionMessage::new);

    private StyledString text;

    public EditLetteredConnectionMessage() {
    }

    public EditLetteredConnectionMessage(final C connection, final StyledString text) {
        super(connection);
        this.text = text;
    }

    // Constructor for decoding
    private EditLetteredConnectionMessage(final RegistryFriendlyByteBuf buf) {
        this.decode(buf);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final EditLetteredConnectionMessage message,
            final net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            // Context has no player directly? Usually it does.
            // IPayloadContext.player() exists.
            net.minecraft.world.entity.player.Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                ConnectionMessage.getConnection(message, c -> c instanceof Lettered, serverPlayer.level())
                        .ifPresent(connection -> {
                            if (connection.isModifiable(serverPlayer)
                                    && ((Lettered) connection).isSupportedText(message.text)) { // Cast needed or
                                                                                                // predicate
                                ((Lettered) connection).setText(message.text);
                            }
                        });
            }
        });
    }

    @Override
    public void encode(final FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeNbt(StyledString.serialize(this.text));
    }

    @Override
    public void decode(final FriendlyByteBuf buf) {
        super.decode(buf);
        this.text = StyledString.deserialize(buf.readNbt());
    }

    public static final class Handler implements BiConsumer<EditLetteredConnectionMessage<?>, ServerMessageContext> {
        @Override
        public void accept(final EditLetteredConnectionMessage<?> message, final ServerMessageContext context) {
            final ServerPlayer player = context.getPlayer();
            this.accept(message, player);
        }

        private <C extends Connection & Lettered> void accept(final EditLetteredConnectionMessage<C> message,
                final ServerPlayer player) {
            if (player != null) {
                ConnectionMessage.<C>getConnection(message, c -> c instanceof Lettered, player.level())
                        .ifPresent(connection -> {
                            if (connection.isModifiable(player) && connection.isSupportedText(message.text)) {
                                connection.setText(message.text);
                            }
                        });
            }
        }
    }
}
