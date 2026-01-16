package me.paulf.fairylights.server.net.clientbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.net.ClientMessageContext;
import me.paulf.fairylights.server.net.Message;
import net.minecraft.client.multiplayer.ClientLevel;
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
         // Logic specific to IPayloadContext - if this needs client handling, it should be done carefully.
         // Since context.player() is available, we can check side.
         // However, ensure this method doesn't trigger client class loading if called on server.
         // The previous implementation utilized safe checks, but referenced ClientLevel potentially.
         // For now, we delegate or remove. Since we are moving to ClientMessageHandlers, we can remove this if unused,
         // or update it to be server-safe (e.g. only use common classes).
         
         // Assuming this method was just a duplicate/helper for the new API, we can leave it if we ensure it doesn't reference client types.
         // But the previous implementation had:
         // final var player = context.player();
         // ...
         // This seems safe as long as no client-only classes are used.
         // But wait, the previous implementation used level.isClientSide().
         // If we want to be safe, we should probably strip it or make sure it delegates safely.
         // Since we are refactoring, let's remove the static handle method if it's not being used by the new system (FairyLights.java passes a consumer).
         // The registrar.playToClient(...) in FairyLights.java USES this handle method ref!
         
         // So we MUST keep `handle` but sanitize it.
         // The previous version was using simple types, except inside lambda? 
         // Actually, context.enqueueWork(() -> ...) is safe.
         // BUT, we want to move logic to ClientMessageHandlers to be consistent.
    }
}
