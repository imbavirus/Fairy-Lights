package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.connection.ConnectionTypes;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class LetterBuntingConnectionItem extends ConnectionItem {
    public LetterBuntingConnectionItem(final Item.Properties properties) {
        super(properties, ConnectionTypes.LETTER_BUNTING);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip,
            final TooltipFlag flag) {
        final net.minecraft.nbt.CompoundTag tag = stack
                .get(me.paulf.fairylights.server.item.FLDataComponents.STYLED_STRING.get());
        if (tag != null) {
            final me.paulf.fairylights.util.styledstring.StyledString s = me.paulf.fairylights.util.styledstring.StyledString
                    .deserialize(tag);
            if (s.length() > 0) {
                tooltip.add(Component.translatable("format.fairylights.text", s.toTextComponent())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
