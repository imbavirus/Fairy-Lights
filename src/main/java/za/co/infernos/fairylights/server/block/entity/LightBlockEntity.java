package za.co.infernos.fairylights.server.block.entity;

import za.co.infernos.fairylights.server.block.LightBlock;
import za.co.infernos.fairylights.server.feature.light.Light;
import za.co.infernos.fairylights.server.item.LightVariant;
import za.co.infernos.fairylights.server.item.SimpleLightVariant;
import za.co.infernos.fairylights.server.sound.FLSounds;
import za.co.infernos.fairylights.util.FLMth;
import za.co.infernos.fairylights.util.matrix.MatrixStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LightBlockEntity extends BlockEntity {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private Light<?> light;

    private boolean on = true;

    public LightBlockEntity(BlockPos pos, BlockState state) {
        super(FLBlockEntities.LIGHT.get(), pos, state);
        // Get the variant from the block state
        final LightVariant<?> variant;
        if (state.getBlock() instanceof LightBlock lightBlock) {
            variant = lightBlock.getVariant();
        } else {
            variant = SimpleLightVariant.FAIRY_LIGHT; // Fallback
        }
        this.light = new Light<>(0, Vec3.ZERO, 0.0F, 0.0F, ItemStack.EMPTY, variant, 0.0F);
    }

    public Light<?> getLight() {
        return this.light;
    }

    public void setItemStack(final ItemStack stack) {
        // Get the variant from the block state, since the block knows what variant it is
        final BlockState state = this.getBlockState();
        final LightVariant<?> variant;
        if (state.getBlock() instanceof LightBlock lightBlock) {
            variant = lightBlock.getVariant();
        } else {
            variant = SimpleLightVariant.FAIRY_LIGHT; // Fallback
        }
        this.light = new Light<>(0, Vec3.ZERO, 0.0F, 0.0F, stack, variant, 0.0F);
        this.setChanged();
    }

    private void setOn(final boolean on) {
        this.on = on;
        this.light.power(on, true);
        this.setChanged();
    }

    public void interact(final Level world, final BlockPos pos, final BlockState state, final Player player, final InteractionHand hand, final BlockHitResult hit) {
        this.setOn(!this.on);
        final BlockState newState = state.setValue(LightBlock.LIT, this.on);
        world.setBlock(pos, newState, 3);
        if (!world.isClientSide()) {
            world.sendBlockUpdated(pos, state, newState, 3);
        }
        final SoundEvent lightSnd;
        final float pitch;
        if (this.on) {
            lightSnd = FLSounds.FEATURE_LIGHT_TURNON.get();
            pitch = 0.6F;
        } else {
            lightSnd = FLSounds.FEATURE_LIGHT_TURNOFF.get();
            pitch = 0.5F;
        }
        this.level.playSound(null, pos, lightSnd, SoundSource.BLOCKS, 1.0F, pitch);
    }

    public void animateTick() {
        final BlockState state = this.getBlockState();
        final AttachFace face = state.getValue(LightBlock.FACE);
        final float rotation = state.getValue(LightBlock.FACING).toYRot();
        final MatrixStack matrix = new MatrixStack();
        matrix.translate(0.5F, 0.5F, 0.5F);
        matrix.rotate((float) Math.toRadians(180.0F - rotation), 0.0F, 1.0F, 0.0F);
        if (this.light.getVariant().isOrientable()) {
            if (face == AttachFace.WALL) {
                matrix.rotate(FLMth.HALF_PI, 1.0F, 0.0F, 0.0F);
            } else if (face == AttachFace.FLOOR) {
                matrix.rotate(-FLMth.PI, 1.0F, 0.0F, 0.0F);
            }
            matrix.translate(0.0F, 0.5F, 0.0F);
        } else {
            if (face == AttachFace.CEILING) {
                matrix.translate(0.0F, 0.25F, 0.0F);
            } else if (face == AttachFace.WALL) {
                matrix.translate(0.0F, 3.0F / 16.0F, 0.125F);
            } else {
                matrix.translate(0.0F, -(float) this.light.getVariant().getBounds().minY - 0.5F, 0.0F);
            }
        }
        this.light.getBehavior().animateTick(this.level, Vec3.atLowerCornerOf(this.worldPosition).add(matrix.transform(Vec3.ZERO)), this.light);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider provider) {
        final CompoundTag tag = super.getUpdateTag(provider);
        this.saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        this.loadAdditional(tag, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(compound, provider);
        // ItemStack.save() API changed in 1.21.1 - needs RegistryAccess or Provider
        // Minecraft 1.21.1 throws IllegalStateException when trying to save empty ItemStack
        final ItemStack item = this.light.getItem();
        if (!item.isEmpty()) {
            compound.put("item", item.save(provider));
        }
        compound.putBoolean("on", this.on);
    }

    @Override
    protected void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(compound, provider);
        // ItemStack.of() API changed in 1.21.1 - use ItemStack.parse() with RegistryAccess/Provider
        if (compound.contains("item")) {
            final ItemStack item = ItemStack.parse(provider, compound.getCompound("item")).orElse(ItemStack.EMPTY);
            if (item != null && !item.isEmpty()) {
                this.setItemStack(item);
            }
        }
        this.setOn(compound.getBoolean("on"));
    }
}
