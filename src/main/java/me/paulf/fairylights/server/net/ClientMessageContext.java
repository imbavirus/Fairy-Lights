package me.paulf.fairylights.server.net;

import net.neoforged.fml.LogicalSide;

public class ClientMessageContext extends MessageContext {
    public ClientMessageContext(final Object context) {
        super(context);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }
}
