package za.co.infernos.fairylights.server.connection;

// LogManager import removed

import za.co.infernos.fairylights.server.block.FLBlocks;
import za.co.infernos.fairylights.server.fastener.Fastener;
import za.co.infernos.fairylights.server.feature.FeatureType;
import za.co.infernos.fairylights.server.feature.light.Light;
import za.co.infernos.fairylights.server.feature.light.LightBehavior;
import za.co.infernos.fairylights.server.item.HangingLightsConnectionItem;
import za.co.infernos.fairylights.server.item.LightVariant;
import za.co.infernos.fairylights.server.item.SimpleLightVariant;
import za.co.infernos.fairylights.server.item.crafting.FLCraftingRecipes;
import za.co.infernos.fairylights.server.jingle.Jingle;
import za.co.infernos.fairylights.server.jingle.JinglePlayer;
import za.co.infernos.fairylights.server.sound.FLSounds;
import za.co.infernos.fairylights.server.string.StringType;
import za.co.infernos.fairylights.server.string.StringTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class HangingLightsConnection extends HangingFeatureConnection<Light<?>> {
    private static final int MAX_LIGHT = 15;

    private static final int LIGHT_UPDATE_WAIT = 400;

    private static final int LIGHT_UPDATE_RATE = 10;

    private StringType string;

    private List<ItemStack> pattern;

    private JinglePlayer jinglePlayer = new JinglePlayer();

    private boolean wasPlaying = false;

    private boolean isOn = true;
    
    private long lastToggleTick = -1; // Track last toggle tick to prevent double-toggles

    private final Set<BlockPos> litBlocks = new HashSet<>();

    private final Set<BlockPos> oldLitBlocks = new HashSet<>();

    private int lightUpdateTime = (int) (Math.random() * LIGHT_UPDATE_WAIT / 2);

    private int lightUpdateIndex;

    public HangingLightsConnection(final ConnectionType<? extends HangingLightsConnection> type, final Level world, final Fastener<?> fastenerOrigin, final UUID uuid) {
        super(type, world, fastenerOrigin, uuid);
        this.string = StringTypes.BLACK_STRING.get();
        this.pattern = new ArrayList<>();
    }

    public StringType getString() {
        return this.string;
    }

    @Nullable
    public Jingle getPlayingJingle() {
        return this.jinglePlayer.getJingle();
    }

    public void play(final Jingle jingle, final int lightOffset) {
        this.jinglePlayer.play(jingle, lightOffset);
    }

    @Override
    public boolean interact(final Player player, final Vec3 hit, final FeatureType featureType, final int feature, final ItemStack heldStack, final InteractionHand hand) {
        // Check if the held item is a LightItem (more robust than tag check)
        final boolean isLightItem = heldStack.getItem() instanceof za.co.infernos.fairylights.server.item.LightItem;
        // Also check the tag as a fallback for compatibility
        final boolean isInLightsTag = heldStack.is(FLCraftingRecipes.LIGHTS);
        
        if (featureType == FEATURE && (isLightItem || isInLightsTag)) {
            // Ensure pattern has enough entries for ALL features (one per light)
            // Expand pattern to match the total number of features if needed
            final int totalFeatures = this.features.length;
            while (this.pattern.size() < totalFeatures) {
                final int targetIndex = this.pattern.size(); // Index we're about to add
                ItemStack currentLight = ItemStack.EMPTY;
                
                // Try to get the actual light from the feature at this index
                if (targetIndex < this.features.length) {
                    currentLight = this.features[targetIndex].getItem();
                }
                
                // If no feature exists at this index, use empty (don't cycle - we want individual entries)
                this.pattern.add(currentLight.isEmpty() ? ItemStack.EMPTY : currentLight.copy());
            }
            
            // Also ensure we have at least one entry for the feature being clicked
            while (this.pattern.size() <= feature) {
                this.pattern.add(ItemStack.EMPTY);
            }
            
            // Use feature ID directly as index (each light has its own pattern entry)
            final int index = feature;
            final ItemStack light = this.pattern.get(index);
            
            // Only swap if the held light is different from the current light
            if (!ItemStack.matches(light, heldStack)) {
                final ItemStack placed = heldStack.split(1);
                this.pattern.set(index, placed);
                // Give the old light back to player (or empty stack if it was empty)
                if (!light.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, light);
                }
                this.computeCatenary();
                // Mark fastener dirty to sync pattern change to clients
                this.fastener.setDirty();
                this.getDestination().get(this.world, false).ifPresent(Fastener::setDirty);
                this.world.playSound(null, hit.x, hit.y, hit.z, FLSounds.FEATURE_COLOR_CHANGE.get(), SoundSource.BLOCKS, 1, 1);
                return true;
            }
        }
        if (super.interact(player, hit, featureType, feature, heldStack, hand)) {
            return true;
        }
        // Only toggle on/off state if player has nothing in hand
        if (!heldStack.isEmpty()) {
            return false; // Don't toggle if holding something
        }
        // Prevent double-toggles from the same tick (interaction might be called twice)
        final long currentTick = this.world.getGameTime();
        if (currentTick == this.lastToggleTick) {
            // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: Ignoring duplicate toggle on same tick");
            return true; // Already handled this tick
        }
        this.lastToggleTick = currentTick;
        
        // Toggle on/off state
        final boolean wasOn = this.isOn;
        this.isOn = !this.isOn;
        // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: Toggling lights - wasOn=" + wasOn + " isOn=" + this.isOn);
        final SoundEvent lightSnd;
        final float pitch;
        if (this.isOn) {
            lightSnd = FLSounds.FEATURE_LIGHT_TURNON.get();
            pitch = 0.6F;
        } else {
            lightSnd = FLSounds.FEATURE_LIGHT_TURNOFF.get();
            pitch = 0.5F;
        }
        this.world.playSound(null, hit.x, hit.y, hit.z, lightSnd, SoundSource.BLOCKS, 1, pitch);
        
        // Immediately update power state of all existing features
        final boolean on = !this.isDynamic() && this.isOn;
        // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: Setting power state - on=" + on + " isDynamic=" + this.isDynamic() + " features.length=" + this.features.length);
        for (final Light<?> light : this.features) {
            light.power(on, true); // Use 'now=true' for immediate visual update
        }
        
        // If turning off, remove all light blocks immediately
        if (!on) {
            for (final BlockPos pos : this.litBlocks) {
                this.removeLight(pos);
            }
            this.litBlocks.clear();
        }
        
        // Mark fastener dirty to sync state change to clients
        // Don't call computeCatenary() - it triggers feature updates that interfere with power state
        this.fastener.setDirty();
        this.getDestination().get(this.world, false).ifPresent(Fastener::setDirty);
        
        // Force immediate block entity update to sync state to clients
        // This ensures deserialize() is called on the client with the new isOn value
        if (!this.world.isClientSide() && this.fastener instanceof za.co.infernos.fairylights.server.fastener.BlockFastener blockFastener) {
            final BlockPos pos = blockFastener.getPos();
            final net.minecraft.world.level.block.entity.BlockEntity be = this.world.getBlockEntity(pos);
            if (be instanceof za.co.infernos.fairylights.server.block.entity.FastenerBlockEntity fastenerBE) {
                fastenerBE.setChanged();
                final net.minecraft.world.level.block.state.BlockState state = this.world.getBlockState(pos);
                this.world.sendBlockUpdated(pos, state, state, 3);
            }
        }
        // Also handle entity fasteners if needed (they sync via different mechanism)
        
        return true;
    }

    @Override
    public void onUpdate() {
        this.jinglePlayer.tick(this.world, this.fastener.getConnectionPoint(), this.features, this.world.isClientSide());
        final boolean playing = this.jinglePlayer.isPlaying();
        if (playing || this.wasPlaying) {
            this.updateNeighbors(this.fastener);
            this.getDestination().get(this.world, false).ifPresent(this::updateNeighbors);
        }
        this.wasPlaying = playing;
        final boolean on = !this.isDynamic() && this.isOn;
        for (final Light<?> light : this.features) {
            light.tick(this.world, this.fastener.getConnectionPoint());
        }
        if (on && this.features.length > 0) {
            this.lightUpdateTime++;
            if (this.lightUpdateTime > LIGHT_UPDATE_WAIT && this.lightUpdateTime % LIGHT_UPDATE_RATE == 0) {
                if (this.lightUpdateIndex >= this.features.length) {
                    this.lightUpdateIndex = 0;
                    this.lightUpdateTime = this.world.random.nextInt(LIGHT_UPDATE_WAIT / 2);
                } else {
                    this.setLight(BlockPos.containing(this.features[this.lightUpdateIndex++].getAbsolutePoint(this.fastener)));
                }
            }
        }
    }

    private void updateNeighbors(final Fastener<?> fastener) {
        this.world.updateNeighbourForOutputSignal(fastener.getPos(), FLBlocks.FASTENER.get());
    }

    @Override
    protected Light<?>[] createFeatures(final int length) {
        return new Light<?>[length];
    }

    @Override
    protected boolean canReuse(final Light<?> feature, final int index) {
        return ItemStack.matches(feature.getItem(), this.getPatternStack(index));
    }

    @Override
    protected Light<?> createFeature(final int index, final Vec3 point, final float yaw, final float pitch) {
        final ItemStack lightData = this.getPatternStack(index);
        return this.createLight(index, point, yaw, pitch, lightData, LightVariant.get(lightData).orElse(SimpleLightVariant.FAIRY_LIGHT));
    }

    private ItemStack getPatternStack(final int index) {
        if (this.pattern.isEmpty()) {
            return ItemStack.EMPTY;
        }
        // Use direct indexing if pattern is large enough, otherwise cycle
        if (index < this.pattern.size()) {
            return this.pattern.get(index);
        } else {
            // Fallback to cycling only if index is beyond pattern size
            return this.pattern.get(index % this.pattern.size());
        }
    }

    @Override
    protected void updateFeature(final Light<?> light) {
        super.updateFeature(light);
        if (!this.isDynamic() && this.isOn) {
            final BlockPos pos = BlockPos.containing(light.getAbsolutePoint(this.fastener));
            this.litBlocks.add(pos);
            this.setLight(pos);
        }
    }

    private <T extends LightBehavior> Light<T> createLight(final int index, final Vec3 point, final float yaw, final float pitch, final ItemStack stack, final LightVariant<T> variant) {
        return new Light<>(index, point, yaw, pitch, stack, variant, 0.125F);
    }

    @Override
    protected float getFeatureSpacing() {
        if (this.pattern.isEmpty()) {
            return SimpleLightVariant.FAIRY_LIGHT.getSpacing();
        }
        float spacing = 0;
        for (final ItemStack patternLightData : this.pattern) {
            final float lightSpacing = LightVariant.get(patternLightData).orElse(SimpleLightVariant.FAIRY_LIGHT).getSpacing();
            if (lightSpacing > spacing) {
                spacing = lightSpacing;
            }
        }
        return spacing;
    }

    @Override
    protected void onBeforeUpdateFeatures() {
        this.oldLitBlocks.clear();
        this.oldLitBlocks.addAll(this.litBlocks);
        this.litBlocks.clear();
    }

    @Override
    protected void onAfterUpdateFeatures() {
        // Apply on both sides. Skipping the client left Light#powered false after feature
        // rebuilds, so twinkle/emissive rendering never saw a powered light.
        final boolean on = !this.isDynamic() && this.isOn;
        for (final Light<?> light : this.features) {
            light.power(on, true);
        }
        this.oldLitBlocks.removeAll(this.litBlocks);
        final Iterator<BlockPos> oldIter = this.oldLitBlocks.iterator();
        while (oldIter.hasNext()) {
            this.removeLight(oldIter.next());
            oldIter.remove();
        }
    }

    @Override
    public void onRemove() {
        for (final BlockPos pos : this.litBlocks) {
            this.removeLight(pos);
        }
        this.litBlocks.clear();

        // Drop pattern lights only on intentional breaks (shouldDrop), never during
        // chunk unload / soft setRemoved (caller sets noDrop first).
        if (this.shouldDrop() && this.world != null && !this.world.isClientSide() &&
            this.pattern != null && !this.pattern.isEmpty()) {
            final BlockPos dropPos = this.fastener != null ? this.fastener.getPos() : null;
            // Skip drops if the fastener chunk is not loaded/ticking (unload path).
            if (dropPos != null && this.world.isLoaded(dropPos)) {
                for (final ItemStack lightStack : this.pattern) {
                    if (!lightStack.isEmpty()) {
                        final float offsetX = this.world.random.nextFloat() * 0.8F + 0.1F;
                        final float offsetY = this.world.random.nextFloat() * 0.8F + 0.1F;
                        final float offsetZ = this.world.random.nextFloat() * 0.8F + 0.1F;
                        final ItemEntity entityItem = new ItemEntity(
                            this.world,
                            dropPos.getX() + offsetX,
                            dropPos.getY() + offsetY,
                            dropPos.getZ() + offsetZ,
                            lightStack.copy()
                        );
                        final float scale = 0.05F;
                        entityItem.setDeltaMovement(
                            this.world.random.nextGaussian() * scale,
                            this.world.random.nextGaussian() * scale + 0.2F,
                            this.world.random.nextGaussian() * scale
                        );
                        this.world.addFreshEntity(entityItem);
                    }
                }
            }
        }
    }
    
    @Override
    public void disconnect(final Player player, final Vec3 hit) {
        // Drop individual light items from the pattern when connection is manually broken
        // This ensures swapped-in lights are returned to the player
        if (this.shouldDrop() && this.pattern != null && !this.pattern.isEmpty()) {
            for (final ItemStack lightStack : this.pattern) {
                if (!lightStack.isEmpty()) {
                    final float offsetX = this.world.random.nextFloat() * 0.8F + 0.1F;
                    final float offsetY = this.world.random.nextFloat() * 0.8F + 0.1F;
                    final float offsetZ = this.world.random.nextFloat() * 0.8F + 0.1F;
                    final ItemEntity entityItem = new ItemEntity(
                        this.world,
                        hit.x + offsetX,
                        hit.y + offsetY,
                        hit.z + offsetZ,
                        lightStack.copy()
                    );
                    final float scale = 0.05F;
                    entityItem.setDeltaMovement(
                        this.world.random.nextGaussian() * scale,
                        this.world.random.nextGaussian() * scale + 0.2F,
                        this.world.random.nextGaussian() * scale
                    );
                    this.world.addFreshEntity(entityItem);
                }
            }
        }
        super.disconnect(player, hit);
    }

    private void removeLight(final BlockPos pos) {
        // Must guard with isLoaded — getBlockState during chunk unload can force a blocking
        // chunk load and deadlock the server (watchdog hang).
        if (this.world != null && this.world.isLoaded(pos) && this.world.getBlockState(pos).is(Blocks.LIGHT)) {
            this.world.removeBlock(pos, false);
        }
    }

    private void setLight(final BlockPos pos) {
        if (this.world.isLoaded(pos) && this.world.isEmptyBlock(pos) && this.world.getBrightness(LightLayer.BLOCK, pos) < MAX_LIGHT) {
            this.world.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LightBlock.MAX_LEVEL), 2);
        }
    }

    public boolean canCurrentlyPlayAJingle() {
        return !this.jinglePlayer.isPlaying();
    }

    public float getJingleProgress() {
        return this.jinglePlayer.getProgress();
    }

    @Override
    public CompoundTag serialize() {
        final CompoundTag compound = super.serialize();
        compound.put("jinglePlayer", this.jinglePlayer.serialize());
        // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: serialize (" + (this.world.isClientSide() ? "CLIENT" : "SERVER") + ") - isOn=" + this.isOn);
        compound.putBoolean("isOn", this.isOn);
        final ListTag litBlocks = new ListTag();
        for (final BlockPos litBlock : this.litBlocks) {
            litBlocks.add(NbtUtils.writeBlockPos(litBlock));
        }
        compound.put("litBlocks", litBlocks);
        return compound;
    }

    @Override
    public void deserialize(final CompoundTag compound) {
        throw new UnsupportedOperationException("Use deserialize(CompoundTag, HolderLookup.Provider)");
    }
    
    @Override
    public void deserialize(final CompoundTag compound, final net.minecraft.core.HolderLookup.Provider provider) {
        // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: deserialize(CompoundTag, Provider) ENTRY - world=" + (this.world != null ? (this.world.isClientSide() ? "CLIENT" : "SERVER") : "NULL") + " isOn=" + this.isOn + " compound.hasIsOn=" + compound.contains("isOn"));
        
        // Call parent to handle destination, slack, drop, etc.
        super.deserialize(compound, provider);
        
        // Now handle HangingLightsConnection-specific data
        // The connection compound structure: {destination, logic, slack, drop, jinglePlayer, isOn, litBlocks}
        // But we're being called with the full connection compound, so we can read isOn directly
        if (this.jinglePlayer == null) {
            this.jinglePlayer = new JinglePlayer();
        }
        if (!this.jinglePlayer.isPlaying() && compound.contains("jinglePlayer")) {
            this.jinglePlayer.deserialize(compound.getCompound("jinglePlayer"));
        }
        
        // Read isOn state, defaulting to true if not present
        final boolean oldIsOn = this.isOn;
        final boolean hasIsOn = compound.contains("isOn");
        final boolean newIsOn = hasIsOn ? compound.getBoolean("isOn") : this.isOn;
        this.isOn = newIsOn;
        
        // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: deserialize (" + (this.world != null && this.world.isClientSide() ? "CLIENT" : "SERVER") + ") - hasIsOn=" + hasIsOn + " oldIsOn=" + oldIsOn + " newIsOn=" + newIsOn + " isOn=" + this.isOn + " features.length=" + (this.features != null ? this.features.length : 0));
        
        // On client side, always update power state when deserializing (state might have changed)
        if (this.world != null && this.world.isClientSide() && this.features != null) {
            final boolean on = !this.isDynamic() && this.isOn;
            // com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: deserialize (CLIENT) - updating power state - on=" + on + " isDynamic=" + this.isDynamic() + " features.length=" + this.features.length);
            for (final Light<?> light : this.features) {
                light.power(on, true); // Use 'now=true' for immediate visual update
            }
            
            // Remove all light blocks if turning off (before reading new litBlocks from NBT)
            if (!on) {
                // Remove all existing light blocks from the set
                for (final BlockPos pos : this.litBlocks) {
                    this.removeLight(pos);
                }
                this.litBlocks.clear();
            }
        }
        
        // Read litBlocks from NBT (server sends empty list when lights are off)
        this.litBlocks.clear();
        if (compound.contains("litBlocks", Tag.TAG_LIST)) {
            final ListTag litBlocks = compound.getList("litBlocks", Tag.TAG_COMPOUND);
            for (int i = 0; i < litBlocks.size(); i++) {
                // NbtUtils.readBlockPos() API changed - takes CompoundTag and key in 1.21.1
                final CompoundTag blockPosTag = litBlocks.getCompound(i);
                NbtUtils.readBlockPos(blockPosTag, "Pos").ifPresent(pos -> this.litBlocks.add(pos));
            }
        }
        
        // On client side, if lights are off, ensure no light blocks exist (double-check)
        if (this.world != null && this.world.isClientSide() && !this.isOn) {
            // Remove any light blocks that might still exist (safety check)
            for (final BlockPos pos : this.litBlocks) {
                this.removeLight(pos);
            }
            this.litBlocks.clear();
        }
    }

    @Override
    public CompoundTag serializeLogic() {
        final CompoundTag compound = super.serializeLogic();
        HangingLightsConnectionItem.setString(compound, this.string);
        final ListTag tagList = new ListTag();
        for (final ItemStack light : this.pattern) {
            // ItemStack.save() API changed in 1.21.1 - use RegistryAccess from world
            Tag savedTag = light.save(this.world.registryAccess());
            
            // Manual fallbacks: DataComponents have been unreliable across pattern sync.
            if (savedTag instanceof CompoundTag compoundTag) {
                int color = za.co.infernos.fairylights.server.item.DyeableItem.getColor(light);
                compoundTag.putInt("fl_backup_color", color);
                if (Boolean.TRUE.equals(light.get(za.co.infernos.fairylights.server.item.FLDataComponents.TWINKLE))) {
                    compoundTag.putBoolean("fl_backup_twinkle", true);
                }
                final java.util.List<Integer> colors =
                        light.get(za.co.infernos.fairylights.server.item.FLDataComponents.COLORS.get());
                if (colors != null && !colors.isEmpty()) {
                    final ListTag backupColors = new ListTag();
                    for (final int c : colors) {
                        backupColors.add(net.minecraft.nbt.IntTag.valueOf(c));
                    }
                    compoundTag.put("fl_backup_colors", backupColors);
                } else {
                    final net.minecraft.world.item.component.CustomData custom =
                            light.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                    if (custom != null) {
                        final CompoundTag customTag = custom.copyTag();
                        if (customTag.contains("colors", Tag.TAG_LIST)) {
                            compoundTag.put("fl_backup_colors", customTag.getList("colors", Tag.TAG_INT));
                        }
                    }
                }
            }
            
            tagList.add(savedTag);
        }
        compound.put("pattern", tagList);
        return compound;
    }

    @Override
    public void deserializeLogic(final CompoundTag compound, final net.minecraft.core.HolderLookup.Provider provider) {
        super.deserializeLogic(compound, provider);
        this.string = HangingLightsConnectionItem.getString(compound);
        final ListTag patternList = compound.getList("pattern", Tag.TAG_COMPOUND);
        this.pattern = new ArrayList<>();
        for (int i = 0; i < patternList.size(); i++) {
            final CompoundTag lightCompound = patternList.getCompound(i);
            // Use the passed provider for registry access during deserialization
            ItemStack stack = ItemStack.parse(provider, lightCompound).orElse(ItemStack.EMPTY);
            
            // Manual fallbacks: restore color / twinkle / color-changing list if components were dropped
            if (lightCompound.contains("fl_backup_color", Tag.TAG_ANY_NUMERIC)) {
                int color = lightCompound.getInt("fl_backup_color");
                za.co.infernos.fairylights.server.item.DyeableItem.setColor(stack, color);
            }
            if (lightCompound.getBoolean("fl_backup_twinkle")) {
                stack.set(za.co.infernos.fairylights.server.item.FLDataComponents.TWINKLE, true);
            }
            if (lightCompound.contains("fl_backup_colors", Tag.TAG_LIST)) {
                final ListTag colorsTag = lightCompound.getList("fl_backup_colors", Tag.TAG_INT);
                final java.util.List<Integer> colors = new java.util.ArrayList<>(colorsTag.size());
                for (int j = 0; j < colorsTag.size(); j++) {
                    colors.add(colorsTag.getInt(j));
                }
                if (!colors.isEmpty()) {
                    stack.set(za.co.infernos.fairylights.server.item.FLDataComponents.COLORS.get(), colors);
                    final CompoundTag custom = stack
                            .getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                                    net.minecraft.world.item.component.CustomData.EMPTY)
                            .copyTag();
                    custom.put("colors", colorsTag.copy());
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.of(custom));
                }
            }
            
            this.pattern.add(stack);
        }
        // Force refresh of features (Light objects) to use the loaded pattern (with colors)
        this.computeCatenary();
    }
}
