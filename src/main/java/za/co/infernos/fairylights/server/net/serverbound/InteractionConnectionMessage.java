package za.co.infernos.fairylights.server.net.serverbound;

import za.co.infernos.fairylights.FairyLights;
import za.co.infernos.fairylights.server.collision.Intersection;
import za.co.infernos.fairylights.server.connection.Connection;
import za.co.infernos.fairylights.server.connection.PlayerAction;
import za.co.infernos.fairylights.server.feature.FeatureType;
import za.co.infernos.fairylights.server.net.ConnectionMessage;
import za.co.infernos.fairylights.server.net.ServerMessageContext;
import za.co.infernos.fairylights.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.BiConsumer;

public final class InteractionConnectionMessage extends ConnectionMessage implements CustomPacketPayload {
    private static final float RANGE = (Connection.MAX_LENGTH + 1) * (Connection.MAX_LENGTH + 1);

    private static final float REACH = 6 * 6;

    public static final CustomPacketPayload.Type<InteractionConnectionMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "interaction_connection"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InteractionConnectionMessage> STREAM_CODEC = StreamCodec
            .of(
                    (buf, msg) -> msg.encode(buf),
                    InteractionConnectionMessage::new);

    private PlayerAction type;

    private Vec3 hit;

    private FeatureType featureType;

    private int featureId;

    public InteractionConnectionMessage() {}

    public InteractionConnectionMessage(final Connection connection, final PlayerAction type, final Intersection intersection) {
        super(connection);
        this.type = type;
        this.hit = intersection.getResult();
        this.featureType = intersection.getFeatureType();
        this.featureId = intersection.getFeature().getId();
    }

    // Constructor for decoding
    private InteractionConnectionMessage(final RegistryFriendlyByteBuf buf) {
        this.decode(buf);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(final FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeByte(this.type.ordinal());
        buf.writeDouble(this.hit.x);
        buf.writeDouble(this.hit.y);
        buf.writeDouble(this.hit.z);
        buf.writeVarInt(this.featureType.getId());
        buf.writeVarInt(this.featureId);
    }

    @Override
    public void decode(final FriendlyByteBuf buf) {
        super.decode(buf);
        this.type = Utils.getEnumValue(PlayerAction.class, buf.readUnsignedByte());
        this.hit = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.featureType = FeatureType.fromId(buf.readVarInt());
        this.featureId = buf.readVarInt();
    }

    public static void handle(final InteractionConnectionMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                ConnectionMessage.getConnection(message, c -> true, serverPlayer.level()).ifPresent(connection -> {
                    if (connection.isModifiable(serverPlayer) &&
                        serverPlayer.distanceToSqr(Vec3.atLowerCornerOf(connection.getFastener().getPos())) < RANGE &&
                        serverPlayer.distanceToSqr(message.hit.x, message.hit.y, message.hit.z) < REACH
                    ) {
                        if (message.type == PlayerAction.ATTACK) {
                            connection.disconnect(serverPlayer, message.hit);
                        } else {
                            interact(message, serverPlayer, connection, message.hit);
                        }
                    }
                });
            }
        });
    }

    private static void interact(final InteractionConnectionMessage message, final Player player, final Connection connection, final Vec3 hit) {
        for (final InteractionHand hand : InteractionHand.values()) {
            final ItemStack stack = player.getItemInHand(hand);
            final ItemStack oldStack = stack.copy();
            if (connection.interact(player, hit, message.featureType, message.featureId, stack, hand)) {
                updateItem(player, oldStack, stack, hand);
                break;
            }
        }
    }

    private static void updateItem(final Player player, final ItemStack oldStack, final ItemStack stack, final InteractionHand hand) {
        if (stack.getCount() <= 0 && !player.getAbilities().instabuild) {
            EventHooks.onPlayerDestroyItem(player, stack, hand);
            player.setItemInHand(hand, ItemStack.EMPTY);
        } else if (stack.getCount() < oldStack.getCount() && player.getAbilities().instabuild) {
            stack.setCount(oldStack.getCount());
        }
    }

    public static final class Handler implements BiConsumer<InteractionConnectionMessage, ServerMessageContext> {
        @Override
        public void accept(final InteractionConnectionMessage message, final ServerMessageContext context) {
            final ServerPlayer player = context.getPlayer();
            getConnection(message, c -> true, player.level()).ifPresent(connection -> {
                if (connection.isModifiable(player) &&
                    player.distanceToSqr(Vec3.atLowerCornerOf(connection.getFastener().getPos())) < RANGE &&
                    player.distanceToSqr(message.hit.x, message.hit.y, message.hit.z) < REACH
                ) {
                    if (message.type == PlayerAction.ATTACK) {
                        connection.disconnect(player, message.hit);
                    } else {
                        interact(message, player, connection, message.hit);
                    }
                }
            });
        }
    }
}
