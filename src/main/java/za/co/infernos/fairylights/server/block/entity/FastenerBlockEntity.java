package za.co.infernos.fairylights.server.block.entity;

import za.co.infernos.fairylights.server.block.FLBlocks;
import za.co.infernos.fairylights.server.block.FastenerBlock;
import za.co.infernos.fairylights.server.fastener.Fastener;
import za.co.infernos.fairylights.server.fastener.BlockFastener;
import za.co.infernos.fairylights.server.ServerProxy;
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
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class FastenerBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
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

    // Kept for callers; NeoForge 1.21 culls via BlockEntityRenderer#getRenderBoundingBox.
    public AABB getRenderBoundingBox() {
        return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
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
        this.getFastener().ifPresent(f -> {
            CompoundTag fastenerTag = f.serializeNBT();
            tag.put("fastener", fastenerTag);
        });
        return tag;
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("fastener", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: FastenerBlockEntity.handleUpdateTag (CLIENT) - received update tag");
            this.getFastener().ifPresent(f -> {
                if (f instanceof za.co.infernos.fairylights.server.fastener.AbstractFastener<?> af) {
                    CompoundTag fastenerTag = tag.getCompound("fastener");
                    // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: FastenerBlockEntity.handleUpdateTag - calling deserializeNBT");
                    af.deserializeNBT(fastenerTag, provider);
                }
            });
        }
    }

    @Override
    public void setLevel(final Level world) {
        super.setLevel(world);
        this.getFastener().ifPresent(f -> f.setWorld(world));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FastenerBlockEntity be) {
        be.getFastener().ifPresent(fastener -> {
            if (!level.isClientSide() && fastener.hasNoConnections()) {
                level.removeBlock(pos, false);
            } else if (!level.isClientSide() && fastener.update()) {
                // Only sync on meaningful dirty (place/toggle/edit), not every dynamic catenary tick
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        });
    }

    public static void tickClient(Level level, BlockPos pos, BlockState state, FastenerBlockEntity be) {
        be.getFastener().ifPresent(f -> f.update());
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void setRemoved() {
        // Soft teardown on chunk unload / BE clear: never dest.get peers (07:22 Saving worlds hang).
        // Intentional breaks call dropItems + remove() from FastenerBlock.onRemove first.
        this.getFastener().ifPresent(f -> {
            for (final za.co.infernos.fairylights.server.connection.Connection c : f.getOwnConnections()) {
                c.noDrop();
            }
            f.detachLocal();
        });
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        this.getFastener().ifPresent(f -> {
             CompoundTag fastenerTag = f.serializeNBT();
             tag.put("fastener", fastenerTag);
        });
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("fastener", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            this.getFastener().ifPresent(f -> {
                if (f instanceof za.co.infernos.fairylights.server.fastener.AbstractFastener<?> af) {
                    af.deserializeNBT(tag.getCompound("fastener"), provider);
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
