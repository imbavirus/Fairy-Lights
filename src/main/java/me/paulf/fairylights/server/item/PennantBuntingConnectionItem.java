package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.connection.ConnectionTypes;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PennantBuntingConnectionItem extends ConnectionItem {
    public PennantBuntingConnectionItem(final Item.Properties properties) {
        super(properties, ConnectionTypes.PENNANT_BUNTING);
    }

    // @Override removed - appendHoverText signature changed in 1.21.1
    public void appendHoverText(final ItemStack stack, final Level world, final List<Component> tooltip, final TooltipFlag flag) {
        // stack.getTag() removed in 1.21.1 - using placeholder for now
        final CompoundTag compound = new CompoundTag();
        if (compound == null) {
            return;
        }
        if (compound.contains("text", Tag.TAG_COMPOUND)) {
            final CompoundTag text = compound.getCompound("text");
            final StyledString s = StyledString.deserialize(text);
            if (s.length() > 0) {
                tooltip.add(Component.translatable("format.fairylights.text", s.toTextComponent()).withStyle(ChatFormatting.GRAY));
            }
        }
        if (compound.contains("pattern", Tag.TAG_LIST)) {
            final ListTag tagList = compound.getList("pattern", Tag.TAG_COMPOUND);
            final int tagCount = tagList.size();
            if (tagCount > 0) {
                tooltip.add(Component.empty());
            }
            for (int i = 0; i < tagCount; i++) {
                final ItemStack item = ItemStack.parse(world != null ? world.registryAccess() : net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY), tagList.getCompound(i)).orElse(ItemStack.EMPTY);
                tooltip.add(item.getHoverName());
            }
        }
    }
}
