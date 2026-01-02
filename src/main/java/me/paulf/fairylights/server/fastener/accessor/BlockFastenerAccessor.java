package me.paulf.fairylights.server.fastener.accessor;

import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.fastener.BlockFastener;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.FastenerType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.Optional;

import javax.annotation.Nullable;

public final class BlockFastenerAccessor implements FastenerAccessor {
    private BlockPos pos = BlockPos.ZERO;

    public BlockFastenerAccessor() {}

    public BlockFastenerAccessor(final BlockFastener fastener) {
        this(fastener.getPos());
    }

    public BlockFastenerAccessor(final BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public Optional<Fastener<?>> get(final Level world, final boolean load) {
        if (load || world.isLoaded(this.pos)) {
            final BlockEntity entity = world.getBlockEntity(this.pos);
            if (entity != null) {
                return CapabilityHandler.getFastenerCapability(entity);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isGone(final Level world) {
        if (world.isClientSide() || !world.isLoaded(this.pos)) return false;
        final BlockEntity entity = world.getBlockEntity(this.pos);
        return entity == null || !CapabilityHandler.getFastenerCapability(entity).isPresent();
    }

    @Override
    public FastenerType getType() {
        return FastenerType.BLOCK;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BlockFastenerAccessor) {
            return this.pos.equals(((BlockFastenerAccessor) obj).pos);
        }
        return false;
    }

    @Override
    public CompoundTag serialize() {
        // NbtUtils.writeBlockPos() returns Tag, need to wrap in CompoundTag
        final CompoundTag tag = new CompoundTag();
        tag.put("pos", NbtUtils.writeBlockPos(this.pos));
        return tag;
    }

    @Override
    public void deserialize(final CompoundTag nbt) {
        // NbtUtils.readBlockPos() now returns Optional<BlockPos>
        if (nbt.contains("pos")) {
            this.pos = NbtUtils.readBlockPos(nbt.getCompound("pos"), "pos").orElse(BlockPos.ZERO);
        } else {
            // Fallback for old format
            this.pos = NbtUtils.readBlockPos(nbt, "pos").orElse(BlockPos.ZERO);
        }
    }
}
