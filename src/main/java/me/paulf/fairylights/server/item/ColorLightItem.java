package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.block.LightBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ColorLightItem extends LightItem {
    public ColorLightItem(final LightBlock light, final Item.Properties properties) {
        super(light, properties);
    }

    @Override
    public Component getName(final ItemStack stack) {
        // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
        final CompoundTag tag = new CompoundTag();
        // TODO: Migrate to data components API for 1.21.1
        if (tag != null && tag.contains("colors", Tag.TAG_LIST)) {
            return Component.translatable("format.fairylights.color_changing", super.getName(stack));
        }
        return DyeableItem.getDisplayName(stack, super.getName(stack));
    }
}
