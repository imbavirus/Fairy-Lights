package me.paulf.fairylights.server.net;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.LogicalSide;
// NetworkEvent.Context removed in NeoForge 1.21.1 - TODO: Update to new API

import java.util.Objects;

public class ServerMessageContext extends MessageContext {
    public ServerMessageContext(final Object context) {
        super(context);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    public MinecraftServer getServer() {
        return this.getPlayer().server;
    }

    public ServerLevel getWorld() {
        return this.getPlayer().serverLevel();
    }

    public ServerPlayer getPlayer() {
        // TODO: Get player from new PayloadContext
        // This needs to be updated to use the new PayloadRegistrar API
        throw new UnsupportedOperationException("TODO: Implement with PayloadRegistrar");
    }
}
