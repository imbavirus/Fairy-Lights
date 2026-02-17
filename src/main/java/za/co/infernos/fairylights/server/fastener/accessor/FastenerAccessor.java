package za.co.infernos.fairylights.server.fastener.accessor;

import za.co.infernos.fairylights.server.fastener.Fastener;
import za.co.infernos.fairylights.server.fastener.FastenerType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.Optional;

public interface FastenerAccessor {
    default Optional<Fastener<?>> get(final Level world) {
        return this.get(world, true);
    }

    Optional<Fastener<?>> get(final Level world, final boolean load);

    boolean isGone(final Level world);

    FastenerType getType();

    CompoundTag serialize();

    void deserialize(CompoundTag compound);
}
