package za.co.infernos.fairylights.server.connection;

import za.co.infernos.fairylights.client.gui.EditLetteredConnectionScreen;
import za.co.infernos.fairylights.server.collision.Intersection;
import za.co.infernos.fairylights.server.fastener.Fastener;
import za.co.infernos.fairylights.server.feature.FeatureType;
import za.co.infernos.fairylights.server.feature.Pennant;
import za.co.infernos.fairylights.server.item.DyeableItem;
import za.co.infernos.fairylights.server.sound.FLSounds;
import za.co.infernos.fairylights.server.item.crafting.FLCraftingRecipes;
import za.co.infernos.fairylights.util.OreDictUtils;
import za.co.infernos.fairylights.util.styledstring.StyledString;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PennantBuntingConnection extends HangingFeatureConnection<Pennant> implements Lettered {
    private List<ItemStack> pattern;

    private StyledString text;

    public PennantBuntingConnection(final ConnectionType<? extends PennantBuntingConnection> type, final Level world, final Fastener<?> fastener, final UUID uuid) {
        super(type, world, fastener, uuid);
        this.pattern = new ArrayList<>();
        this.text = new StyledString();
    }

    @Override
    public float getRadius() {
        return 0.045F;
    }

    @Override
    public void processClientAction(final Player player, final PlayerAction action, final Intersection intersection) {
        if (this.openTextGui(player, action, intersection)) {
            super.processClientAction(player, action, intersection);
        }
    }

    @Override
    public boolean interact(final Player player, final Vec3 hit, final FeatureType featureType, final int feature, final ItemStack heldStack, final InteractionHand hand) {
        if (featureType == FEATURE && (OreDictUtils.isDye(heldStack) || heldStack.is(FLCraftingRecipes.PENNANTS))) {
            final int index = feature % this.pattern.size();
            final ItemStack pennant = this.pattern.get(index);
            if (!ItemStack.matches(pennant, heldStack)) {
                final ItemStack placed = heldStack.split(1);
                this.pattern.set(index, placed);
                ItemHandlerHelper.giveItemToPlayer(player, pennant);
                this.computeCatenary();
                this.world.playSound(null, hit.x, hit.y, hit.z, FLSounds.FEATURE_COLOR_CHANGE.get(), SoundSource.BLOCKS, 1, 1);
                return true;
            }
        }
        return super.interact(player, hit, featureType, feature, heldStack, hand);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        for (final Pennant light : this.features) {
            light.tick(this.world);
        }
    }

    @Override
    protected Pennant[] createFeatures(final int length) {
        return new Pennant[length];
    }

    @Override
    protected Pennant createFeature(final int index, final Vec3 point, final float yaw, final float pitch) {
        final ItemStack data = this.pattern.isEmpty() ? ItemStack.EMPTY : this.pattern.get(index % this.pattern.size());
        return new Pennant(index, point, yaw, pitch, DyeableItem.getColor(data), data.getItem());
    }

    @Override
    protected float getFeatureSpacing() {
        return 0.6875F;
    }

    @Override
    public boolean isSupportedText(final StyledString text) {
        return text.length() <= this.features.length && Lettered.super.isSupportedText(text);
    }

    @Override
    public void setText(final StyledString text) {
        this.text = text;
        this.computeCatenary();
    }

    @Override
    public StyledString getText() {
        return this.text;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen createTextGUI() {
        return new EditLetteredConnectionScreen<>(this);
    }

    @Override
    public CompoundTag serializeLogic() {
        final CompoundTag compound = super.serializeLogic();
        final ListTag patternList = new ListTag();
        for (final ItemStack entry : this.pattern) {
            Tag savedTag = entry.save(this.world.registryAccess());
            if (savedTag instanceof CompoundTag compoundTag) {
                compoundTag.putInt("fl_backup_color", DyeableItem.getColor(entry));
            }
            patternList.add(savedTag);
        }
        compound.put("pattern", patternList);
        compound.put("text", StyledString.serialize(this.text));
        return compound;
    }

    @Override
    public void deserializeLogic(final CompoundTag compound, final net.minecraft.core.HolderLookup.Provider provider) {
        super.deserializeLogic(compound, provider);
        this.pattern = new ArrayList<>();
        final ListTag patternList = compound.getList("pattern", Tag.TAG_COMPOUND);
        for (int i = 0; i < patternList.size(); i++) {
            final CompoundTag pennantCompound = patternList.getCompound(i);
            final ItemStack stack = ItemStack.parse(provider, pennantCompound).orElse(ItemStack.EMPTY);
            if (pennantCompound.contains("fl_backup_color", Tag.TAG_ANY_NUMERIC)) {
                DyeableItem.setColor(stack, pennantCompound.getInt("fl_backup_color"));
            }
            this.pattern.add(stack);
        }
        this.text = StyledString.deserialize(compound.getCompound("text"));
    }
}
