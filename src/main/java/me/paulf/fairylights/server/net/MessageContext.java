package me.paulf.fairylights.server.net;

import net.neoforged.fml.LogicalSide;
// NetworkEvent.Context removed in NeoForge 1.21.1 - TODO: Update to new API

public abstract class MessageContext {
    protected final Object context; // Placeholder - TODO: Use new PayloadContext type

    public MessageContext(final Object context) {
        this.context = context;
    }

    public abstract LogicalSide getSide();
}
