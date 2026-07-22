package za.co.infernos.fairylights.server.integration.jei;

import za.co.infernos.fairylights.server.item.FLDataComponents;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public final class ColorSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
    @Override
    public String apply(final ItemStack stack, final UidContext context) {
        final StringBuilder sb = new StringBuilder();

        // Twinkle / color-changing must stay distinct for recipe matching, or JEI links the
        // twinkle craft to plain lights and then drops glowstone from the focused layout.
        if (stack.getOrDefault(FLDataComponents.TWINKLE.get(), false)) {
            sb.append("T");
        }
        final List<Integer> colors = stack.get(FLDataComponents.COLORS.get());
        if (colors != null && !colors.isEmpty()) {
            sb.append("CC");
        } else {
            final CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
            if (custom != null && custom.copyTag().contains("colors", Tag.TAG_LIST)) {
                sb.append("CC");
            }
        }

        // Dye color only for the ingredient list — ignore in Recipe context so upgrades still
        // appear for any dyed light of the matching variant.
        if (context != UidContext.Recipe
                && stack.has(FLDataComponents.COLOR.get())
                && !stack.has(FLDataComponents.CONNECTION_LOGIC.get())) {
            sb.append(String.format("C:%06x", stack.get(FLDataComponents.COLOR.get())));
        }

        if (sb.length() > 0) {
            return sb.toString();
        }

        return IIngredientSubtypeInterpreter.NONE;
    }
}
