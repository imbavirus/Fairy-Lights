package me.paulf.fairylights.server.block.entity;

import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.block.FastenerBlock;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.BlockFastener;
import me.paulf.fairylights.server.ServerProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class FastenerBlockEntity extends BlockEntity {
    /**
     * NeoForge 1.21.x removed the old Forge capability attachment flow used by the original mod.
     * The previous port tried to access a "getCapability(ResourceLocation)" method via reflection,
     * which doesn't exist for BlockEntities anymore, so this was always empty.
     *
     * Store the fastener directly on the BE instead so connections can be created and persisted.
     */
    private BlockFastener fastener;

    public FastenerBlockEntity(final BlockPos pos, final BlockState state) {
        super(FLBlockEntities.FASTENER.get(), pos ,state);
    }

    // getRenderBoundingBox() may not be an override in 1.21.1
    public AABB getRenderBoundingBox() {
        return this.getFastener().map(fastener -> fastener.getBounds().inflate(1)).orElseGet(() -> {
            // super.getRenderBoundingBox() may not exist in 1.21.1 - return default AABB
            final BlockPos pos = this.getBlockPos();
            return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        });
    }

    public Vec3 getOffset() {
        return FLBlocks.FASTENER.get().getOffset(this.getFacing(), 0.125F);
    }

    public Direction getFacing() {
        final BlockState state = this.level.getBlockState(this.worldPosition);
        if (state.getBlock() != FLBlocks.FASTENER.get()) {
            return Direction.UP;
        }
        return state.getValue(FastenerBlock.FACING);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * 1.21.x: update packets use the HolderLookup.Provider-based tag methods.
     * If we don't override these exact signatures, the client will never receive connection NBT,
     * and ropes will be invisible even though the server-side connection exists (player gets tethered).
     */
    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider provider) {
        // Start with the vanilla tag so position/id/etc are correct, then add our fastener state.
        final CompoundTag tag = super.getUpdateTag(provider);
        this.getFastener().ifPresent(f -> tag.put("fastener", f.serializeNBT()));
        return tag;
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("fastener", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            this.getFastener().ifPresent(f -> {
                if (f instanceof me.paulf.fairylights.server.fastener.AbstractFastener<?> af) {
                    af.deserializeNBT(tag.getCompound("fastener"));
                }
            });
        }
    }

    // setLevel() may not be an override in 1.21.1
    public void setLevel(final Level world) {
        super.setLevel(world);
        this.getFastener().ifPresent(f -> f.setWorld(world));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FastenerBlockEntity be) {
        be.getFastener().ifPresent(fastener -> {
            if (!level.isClientSide() && fastener.hasNoConnections()) {
                level.removeBlock(pos, false);
            } else if (!level.isClientSide() && fastener.update()) {
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        });
    }

    public static void tickClient(Level level, BlockPos pos, BlockState state, FastenerBlockEntity be) {
        be.getFastener().ifPresent(f -> f.update());
    }

    @Override
    public void setRemoved() {
        this.getFastener().ifPresent(Fastener::remove);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        this.getFastener().ifPresent(f -> tag.put("fastener", f.serializeNBT()));
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("fastener", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            this.getFastener().ifPresent(f -> {
                if (f instanceof me.paulf.fairylights.server.fastener.AbstractFastener<?> af) {
                    af.deserializeNBT(tag.getCompound("fastener"));
                }
            });
        }
    }

    public Optional<Fastener<?>> getFastener() {
        if (this.fastener == null) {
            this.fastener = new BlockFastener(this, ServerProxy.buildBlockView());
            if (this.level != null) {
                this.fastener.setWorld(this.level);
            }
        }
        return Optional.of(this.fastener);
    }
}
