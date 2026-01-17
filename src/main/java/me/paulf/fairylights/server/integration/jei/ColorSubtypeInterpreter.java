package me.paulf.fairylights.server.integration.jei;

import me.paulf.fairylights.server.item.FLDataComponents;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;

public final class ColorSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
    @Override
    public String apply(final ItemStack stack, final UidContext context) {
        if (stack.has(FLDataComponents.COLOR)) {
            return String.format("%06x", stack.get(FLDataComponents.COLOR));
        }
        return IIngredientSubtypeInterpreter.NONE;
    }
}
