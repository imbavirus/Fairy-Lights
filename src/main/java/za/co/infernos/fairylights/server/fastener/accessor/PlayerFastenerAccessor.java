package za.co.infernos.fairylights.server.fastener.accessor;

import za.co.infernos.fairylights.server.fastener.FastenerType;
import za.co.infernos.fairylights.server.fastener.PlayerFastener;
import net.minecraft.world.entity.player.Player;

public final class PlayerFastenerAccessor extends EntityFastenerAccessor<Player> {
    public PlayerFastenerAccessor() {
        super(Player.class);
    }

    public PlayerFastenerAccessor(final PlayerFastener fastener) {
        super(Player.class, fastener);
    }

    @Override
    public FastenerType getType() {
        return FastenerType.PLAYER;
    }
}
