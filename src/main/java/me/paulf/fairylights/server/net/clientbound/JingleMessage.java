package me.paulf.fairylights.server.net.clientbound;

import me.paulf.fairylights.server.connection.HangingLightsConnection;
import me.paulf.fairylights.server.jingle.Jingle;
import me.paulf.fairylights.server.net.ClientMessageContext;
import me.paulf.fairylights.server.net.ConnectionMessage;
import me.paulf.fairylights.server.net.ConnectionMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;

public final class JingleMessage extends ConnectionMessage {
    private int lightOffset;

    public Jingle jingle;

    public JingleMessage() {}

    public JingleMessage(final HangingLightsConnection connection, final int lightOffset, final Jingle jingle) {
        super(connection);
        this.lightOffset = lightOffset;
        this.jingle = jingle;
    }

    public int getLightOffset() {
        return this.lightOffset;
    }

    @Override
    public void encode(final FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeVarInt(this.lightOffset);
        this.jingle.write(buf);
    }

    @Override
    public void decode(final FriendlyByteBuf buf) {
        super.decode(buf);
        this.lightOffset = buf.readVarInt();
        this.jingle = Jingle.read(buf);
    }
}
