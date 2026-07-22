package za.co.infernos.fairylights.server.item;

import za.co.infernos.fairylights.server.block.LightBlock;
import za.co.infernos.fairylights.server.feature.light.ColorChangingBehavior;
import za.co.infernos.fairylights.server.feature.light.TwinkleBehavior;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

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

    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (TwinkleBehavior.exists(stack)) {
            tooltip.add(Component.translatable("item.fairyLights.twinkle").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        if (ColorChangingBehavior.exists(stack)) {
            final java.util.List<Integer> colors = stack.get(FLDataComponents.COLORS.get());
            if (colors != null && !colors.isEmpty()) {
                for (final int color : colors) {
                    tooltip.add(DyeableItem.getColorName(color).copy().withStyle(ChatFormatting.GRAY));
                }
            } else {
                final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData != null) {
                    final CompoundTag tag = customData.getUnsafe();
                    if (tag.contains("colors", Tag.TAG_LIST)) {
                        final ListTag colorList = tag.getList("colors", Tag.TAG_INT);
                        for (int i = 0; i < colorList.size(); i++) {
                            tooltip.add(DyeableItem.getColorName(colorList.getInt(i)).copy().withStyle(ChatFormatting.GRAY));
                        }
                    }
                }
            }
        }
    }
}
