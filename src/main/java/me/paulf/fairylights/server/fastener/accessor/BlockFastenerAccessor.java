package me.paulf.fairylights.server.fastener.accessor;

import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.fastener.BlockFastener;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.FastenerType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
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
        // In 1.21+, Level#isLoaded semantics are different and can return false even for nearby loaded positions,
        // which breaks the "placing" flow (player can't resolve the first fastener to reconnect on 2nd click).
        // Using getBlockEntity() is sufficient: it will be null if the chunk isn't loaded.
        final BlockEntity entity = world.getBlockEntity(this.pos);
        return entity != null ? CapabilityHandler.getFastenerCapability(entity) : Optional.empty();
    }

    @Override
    public boolean isGone(final Level world) {
        if (world.isClientSide()) return false;
        final BlockEntity entity = world.getBlockEntity(this.pos);
        return entity == null || CapabilityHandler.getFastenerCapability(entity).isEmpty();
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
        // 1.21+: we serialize as tag.put("pos", NbtUtils.writeBlockPos(pos))
        // NbtUtils.readBlockPos(tag, "pos") reads a BlockPos from a *field* named "pos".
        this.pos = NbtUtils.readBlockPos(nbt, "pos").orElseGet(() -> {
            // Fallback: if someone stored the raw compound directly, read X/Y/Z manually.
            // (writeBlockPos uses X/Y/Z keys in modern MC)
            if (nbt.contains("pos", Tag.TAG_COMPOUND)) {
                final CompoundTag posTag = nbt.getCompound("pos");
                if (posTag.contains("X", Tag.TAG_ANY_NUMERIC) && posTag.contains("Y", Tag.TAG_ANY_NUMERIC) && posTag.contains("Z", Tag.TAG_ANY_NUMERIC)) {
                    return new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
                }
            }
            return BlockPos.ZERO;
        });
    }
}
