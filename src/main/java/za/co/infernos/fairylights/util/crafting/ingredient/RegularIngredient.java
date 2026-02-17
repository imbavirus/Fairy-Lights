package za.co.infernos.fairylights.util.crafting.ingredient;

import za.co.infernos.fairylights.util.crafting.GenericRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface RegularIngredient extends GenericIngredient<RegularIngredient, GenericRecipe.MatchResultRegular> {
    default void matched(final ItemStack ingredient, final CompoundTag nbt) {}
}
