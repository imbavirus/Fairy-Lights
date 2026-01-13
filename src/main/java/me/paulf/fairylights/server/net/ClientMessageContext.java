package me.paulf.fairylights.server.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
// NetworkEvent.Context removed in NeoForge 1.21.1 - TODO: Update to new API

import java.util.Objects;

public class ClientMessageContext extends MessageContext {
    public ClientMessageContext(final Object context) {
        super(context);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public ClientLevel getWorld() {
        final ClientLevel level = this.getMinecraft().level;
        if (level == null) {
            // World might be null during shutdown/pause - return null instead of throwing
            return null;
        }
        return level;
    }

    public Player getPlayer() {
        // TODO: Get player from new PayloadContext
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            // Player might be null during shutdown/pause - return null instead of throwing
            return null;
        }
        return player;
    }
}
