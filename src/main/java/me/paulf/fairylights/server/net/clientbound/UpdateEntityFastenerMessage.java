package me.paulf.fairylights.server.net.clientbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.net.ClientMessageContext;
import me.paulf.fairylights.server.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.BiConsumer;

public final class UpdateEntityFastenerMessage implements CustomPacketPayload, Message {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "update_entity_fastener");
    public static final Type<UpdateEntityFastenerMessage> TYPE = new Type<>(ID);
    
    public static final StreamCodec<FriendlyByteBuf, UpdateEntityFastenerMessage> STREAM_CODEC = StreamCodec.of(
        (buf, msg) -> {
            buf.writeVarInt(msg.getEntityId());
            buf.writeNbt(msg.getCompound());
        },
        buf -> new UpdateEntityFastenerMessage(buf.readVarInt(), buf.readNbt())
    );
    
    private final int entityId;
    private final CompoundTag compound;

    public UpdateEntityFastenerMessage(final int entityId, final CompoundTag compound) {
        this.entityId = entityId;
        this.compound = compound;
    }
    
    public UpdateEntityFastenerMessage(final Entity entity, final CompoundTag compound) {
        this(entity.getId(), compound);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void encode(final FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeNbt(this.compound);
    }

    @Override
    public void decode(final FriendlyByteBuf buf) {
        // Not used with CustomPacketPayload - use constructor instead
        throw new UnsupportedOperationException("Use StreamCodec instead");
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public CompoundTag getCompound() {
        return this.compound;
    }

    public static void handle(final UpdateEntityFastenerMessage message, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            final var player = context.player();
            if (player != null) {
                final var level = player.level();
                if (level != null && level.isClientSide()) {
                    final Entity entity = level.getEntity(message.getEntityId());
                    if (entity != null && message.getCompound() != null) {
                        CapabilityHandler.getFastenerCapability(entity).ifPresent(f -> {
                            // Call deserializeNBT directly - it exists in AbstractFastener
                            if (f instanceof me.paulf.fairylights.server.fastener.AbstractFastener) {
                                ((me.paulf.fairylights.server.fastener.AbstractFastener) f).deserializeNBT(message.getCompound());
                            }
                        });
                    }
                }
            }
        });
    }
    
    public static final class Handler implements BiConsumer<UpdateEntityFastenerMessage, ClientMessageContext> {
        @Override
        public void accept(final UpdateEntityFastenerMessage message, final ClientMessageContext context) {
            // Handle on client thread
            net.minecraft.client.Minecraft.getInstance().execute(() -> {
                final Entity entity = context.getWorld().getEntity(message.getEntityId());
                if (entity != null && message.getCompound() != null) {
                    CapabilityHandler.getFastenerCapability(entity).ifPresent(f -> {
                        // Call deserializeNBT directly - it exists in AbstractFastener
                        if (f instanceof me.paulf.fairylights.server.fastener.AbstractFastener) {
                            ((me.paulf.fairylights.server.fastener.AbstractFastener) f).deserializeNBT(message.getCompound());
                        }
                    });
                }
            });
        }
    }
}
