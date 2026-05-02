package za.co.infernos.fairylights.server.entity;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import za.co.infernos.fairylights.server.ServerProxy;
import za.co.infernos.fairylights.server.block.FLBlocks;
import za.co.infernos.fairylights.server.capability.CapabilityHandler;
import za.co.infernos.fairylights.server.fastener.Fastener;
import za.co.infernos.fairylights.server.item.ConnectionItem;
import za.co.infernos.fairylights.server.net.clientbound.UpdateEntityFastenerMessage;
import za.co.infernos.fairylights.server.fastener.FenceFastener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
// NetworkHooks removed in NeoForge 1.21.1 - using entity's getAddEntityPacket() instead

import javax.annotation.Nullable;
import java.io.IOException;
import net.minecraft.server.level.ServerLevel;
import java.util.Optional;

public final class FenceFastenerEntity extends HangingEntity implements IEntityWithComplexSpawn {
    private final FenceFastener fastener;
    private int surfaceCheckTime;
    
    public FenceFastenerEntity(final EntityType<? extends FenceFastenerEntity> type, final Level world) {
        super(type, world);
        this.fastener = new FenceFastener(this);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        // Entity.defineSynchedData is abstract - don't call super
    }

    public FenceFastenerEntity(final Level world) {
        this(FLEntities.FASTENER.get(), world);
    }

    public FenceFastenerEntity(final Level world, final BlockPos pos) {
        this(world);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    // getWidth() and getHeight() may not be overrides in 1.21.1 HangingEntity
    public int getWidth() {
        return 9;
    }

    public int getHeight() {
        return 9;
    }

    /**
     * Returns the fastener capability for this entity.
     * Used by renderers to access connection data.
     */
    public Optional<Fastener<?>> getFastener() {
        return Optional.of(this.fastener);
    }

    // getEyeHeight() may not be an override in 1.21.1
    public float getEyeHeight(final Pose pose, final EntityDimensions size) {
        /*
         * Because this entity is inside of a block when
         * EntityLivingBase#canEntityBeSeen performs its
         * raytracing it will always return false during
         * NetHandlerPlayServer#processUseEntity, making
         * the player reach distance be limited at three
         * blocks as opposed to the standard six blocks.
         * EntityLivingBase#canEntityBeSeen will add the
         * value given by getEyeHeight to the y position
         * of the entity to calculate the end point from
         * which to raytrace to. Returning one lets most
         * interactions with a player succeed, typically
         * for breaking the connection or creating a new
         * connection. I hope you enjoy my line lengths.
         */
        return 1;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(final double distance) {
        // Always render at any distance - connections can be long
        return true;
    }

    public boolean ignoreExplosion() {
        return true;
    }

    public boolean survives() {
        return !this.level().isLoaded(this.pos) || ConnectionItem.isFence(this.level().getBlockState(this.pos));
    }

    @Override
    public void remove(final RemovalReason reason) {
        this.getFastener().ifPresent(Fastener::remove);
        super.remove(reason);
    }

    @Override
    public boolean hurtServer(final ServerLevel level, final DamageSource source, final float amount) {
        // isInvulnerableTo removed in 1.21.2
        if (source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) {
            return false;
        }
        if (this.isAlive()) {
            this.markHurt();
            this.dropItem(level, source.getEntity());
            this.remove(RemovalReason.KILLED);
        }
        return true;
    }

    // canChangeDimensions() may not be an override in 1.21.1
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public void dropItem(final ServerLevel level, @Nullable final Entity breaker) {
        this.getFastener().ifPresent(fastener -> fastener.dropItems(level, this.pos));
        if (breaker != null) {
            level.levelEvent(2001, this.pos, Block.getId(FLBlocks.FASTENER.get().defaultBlockState()));
        }
    }

    @Override
    public void playPlacementSound() {
        final SoundType sound = FLBlocks.FASTENER.get().getSoundType(FLBlocks.FASTENER.get().defaultBlockState(),
                this.level(), this.getPos(), null);
        this.playSound(sound.getPlaceSound(), (sound.getVolume() + 1) / 2, sound.getPitch() * 0.8F);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.BLOCKS;
    }

    @Override
    public void setPos(final double x, final double y, final double z) {
        super.setPos(Mth.floor(x) + 0.5, Mth.floor(y) + 0.5, Mth.floor(z) + 0.5);
    }

    // setDirection() may not be an override in 1.21.1
    public void setDirection(final Direction facing) {
    }

    // calculateBoundingBox is now required in 1.21.1
    @Override
    protected AABB calculateBoundingBox(final BlockPos pos, final Direction direction) {
        final double posX = pos.getX() + 0.5;
        final double posY = pos.getY() + 0.5;
        final double posZ = pos.getZ() + 0.5;
        final float w = 3 / 16F;
        final float h = 3 / 16F;
        return new AABB(posX - w, posY - h, posZ - w, posX + w, posY + h, posZ + w);
    }

    // Override removed in 1.21.2 - getBoundingBoxForCulling no longer exists
    // Connections handle their own visibility via shouldRender in the renderer

    @Override
    public void tick() {
        this.getFastener().ifPresent(fastener -> {
            if (!this.level().isClientSide() && (fastener.hasNoConnections() || this.checkSurface())) {
                if (this.level() instanceof ServerLevel sl) { this.dropItem(sl, null); }
                this.remove(RemovalReason.DISCARDED);
            } else if (fastener.update() && !this.level().isClientSide()) {
                final UpdateEntityFastenerMessage msg = new UpdateEntityFastenerMessage(this, fastener.serializeNBT());
                ServerProxy.sendToPlayersWatchingEntity(msg, this);
            }
        });
    }

    private boolean checkSurface() {
        if (this.surfaceCheckTime++ == 100) {
            this.surfaceCheckTime = 0;
            return !this.survives();
        }
        return false;
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ConnectionItem) {
            if (this.level().isClientSide()) {
                player.swing(hand);
            } else {
                this.getFastener().ifPresent(
                        fastener -> ((ConnectionItem) stack.getItem()).connect(stack, player, this.level(), fastener));
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("pos", NbtUtils.writeBlockPos(this.pos));
        compound.put("fastener", this.fastener.serializeNBT());
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound) {
        // NbtUtils.readBlockPos() signature changed in 1.21.1
        // NbtUtils.readBlockPos() now returns Optional<BlockPos> in 1.21.1
        if (compound.contains("pos", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            NbtUtils.readBlockPos(compound, "pos").ifPresent(pos -> this.pos = pos);
        }
        if (compound.contains("fastener", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            final net.minecraft.core.HolderLookup.Provider provider = this.level() != null ? this.level().registryAccess() : net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
            this.fastener.deserializeNBT(compound.getCompound("fastener"), provider);
        }
    }

    @Override
    public void writeSpawnData(final net.minecraft.network.RegistryFriendlyByteBuf buf) {
        this.getFastener().ifPresent(fastener -> {
            try {
                CompoundTag tag = fastener.serializeNBT();
                NbtIo.write(tag, new ByteBufOutputStream(buf));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void readSpawnData(final net.minecraft.network.RegistryFriendlyByteBuf buf) {
        this.getFastener().ifPresent(fastener -> {
            try {
                final NbtAccounter accounter = NbtAccounter.create(0x200000);
                CompoundTag tag = NbtIo.read(new ByteBufInputStream(buf), accounter);
                if (tag != null && fastener instanceof za.co.infernos.fairylights.server.fastener.AbstractFastener) {
                    ((za.co.infernos.fairylights.server.fastener.AbstractFastener<?>) fastener).deserializeNBT(tag, buf.registryAccess());
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static FenceFastenerEntity create(final Level world, final BlockPos fence) {
        final FenceFastenerEntity fastener = new FenceFastenerEntity(world, fence);
        // fastener.forceSpawn = true;
        world.addFreshEntity(fastener);
        fastener.playPlacementSound();
        return fastener;
    }

    @Nullable
    public static FenceFastenerEntity find(final Level world, final BlockPos pos) {
        final HangingEntity entity = findHanging(world, pos);
        if (entity instanceof FenceFastenerEntity) {
            return (FenceFastenerEntity) entity;
        }
        return null;
    }

    @Nullable
    public static HangingEntity findHanging(final Level world, final BlockPos pos) {
        for (final HangingEntity e : world.getEntitiesOfClass(HangingEntity.class, new AABB(pos).inflate(2))) {
            if (e.getPos().equals(pos)) {
                return e;
            }
        }
        return null;
    }
}
