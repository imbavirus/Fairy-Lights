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
        return Objects.requireNonNull(this.getMinecraft().level);
    }

    public Player getPlayer() {
        // TODO: Get player from new PayloadContext
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }
}
