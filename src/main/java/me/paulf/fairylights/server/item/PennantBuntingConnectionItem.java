package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.connection.ConnectionTypes;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class PennantBuntingConnectionItem extends ConnectionItem {
    public PennantBuntingConnectionItem(final Item.Properties properties) {
        super(properties, ConnectionTypes.PENNANT_BUNTING);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip,
            final TooltipFlag flag) {
        final CompoundTag tag = stack.get(me.paulf.fairylights.server.item.FLDataComponents.CONNECTION_LOGIC);
        if (tag != null && tag.contains("pattern", Tag.TAG_LIST)) {
            final ListTag tagList = tag.getList("pattern", Tag.TAG_COMPOUND);
            final int tagCount = tagList.size();
            if (tagCount > 0) {
                tooltip.add(Component.empty());
            }
            for (int i = 0; i < tagCount; i++) {
                final ItemStack item = ItemStack.parse(context.registries(), tagList.getCompound(i))
                        .orElse(ItemStack.EMPTY);
                tooltip.add(item.getHoverName());
            }
        } else if (tag != null && tag.contains("text", Tag.TAG_COMPOUND)) {
            final StyledString s = StyledString.deserialize(tag.getCompound("text"));
            if (s.length() > 0) {
                tooltip.add(Component.translatable("format.fairylights.text", s.toTextComponent())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
