package za.co.infernos.fairylights.server.item;

import za.co.infernos.fairylights.server.block.LightBlock;
import za.co.infernos.fairylights.server.feature.light.ColorChangingBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ColorLightItem extends LightItem {
    public ColorLightItem(final LightBlock light, final Item.Properties properties) {
        super(light, properties);
    }

    @Override
    public Component getName(final ItemStack stack) {
        if (ColorChangingBehavior.exists(stack)) {
            return Component.translatable("format.fairylights.color_changing", super.getName(stack));
        }
        return DyeableItem.getDisplayName(stack, super.getName(stack));
    }
}
