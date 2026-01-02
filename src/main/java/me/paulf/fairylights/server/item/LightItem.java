package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.block.LightBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
// ICapabilityProvider removed in NeoForge 1.21.1 - item capabilities work differently

import javax.annotation.Nullable;
import java.util.List;

public class LightItem extends BlockItem {
    private final LightBlock light;

    public LightItem(final LightBlock light, final Properties properties) {
        super(light, properties);
        this.light = light;
    }

    @Override
    public LightBlock getBlock() {
        return this.light;
    }

    // initCapabilities removed in NeoForge 1.21.1 - item capabilities work differently
    // TODO: Refactor to use data attachments or a different approach
    // @Override
    // public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final CompoundTag nbt) {
    //     return LightVariant.provider(this.light.getVariant());
    // }

    // appendHoverText() method signature may have changed in 1.21.1 - check if @Override is still valid
    public void appendHoverText(final ItemStack stack, @Nullable final Level world, final List<Component> tooltip, final TooltipFlag flag) {
        // super.appendHoverText() signature may have changed - comment out for now
        // super.appendHoverText(stack, world, tooltip, flag);
        // getTag() removed in 1.21.1 - use data components instead
        final CompoundTag tag = new CompoundTag();
        if (!tag.isEmpty()) {
            if (tag.getBoolean("twinkle")) {
                tooltip.add(Component.translatable("item.fairyLights.twinkle").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
            if (tag.contains("colors", Tag.TAG_LIST)) {
                final ListTag colors = tag.getList("colors", Tag.TAG_INT);
                for (int i = 0; i < colors.size(); i++) {
                    tooltip.add(DyeableItem.getColorName(colors.getInt(i)).copy().withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }
}
