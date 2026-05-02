package za.co.infernos.fairylights.server.item.crafting;

import za.co.infernos.fairylights.server.item.DyeableItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class CopyColorRecipe extends CustomRecipe {
    public CopyColorRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(final net.minecraft.world.item.crafting.CraftingInput input, final Level world) {
        int count = 0;
        for (int i = 0; i < input.size(); i++) {
            final ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && (!stack.is(FLCraftingRecipes.DYEABLE) || count++ >= 2)) {
                return false;
            }
        }
        return count == 2;
    }

    @Override
    public ItemStack assemble(final net.minecraft.world.item.crafting.CraftingInput input,
            final net.minecraft.core.HolderLookup.Provider provider) {
        ItemStack original = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            final ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(FLCraftingRecipes.DYEABLE)) {
                    if (original.isEmpty()) {
                        original = stack;
                    } else {
                        final ItemStack copy = stack.copy();
                        copy.setCount(1);
                        DyeableItem.setColor(copy, DyeableItem.getColor(original));
                        return copy;
                    }
                } else {
                    break;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public net.minecraft.core.NonNullList<ItemStack> getRemainingItems(
            final net.minecraft.world.item.crafting.CraftingInput input) {
        ItemStack original = ItemStack.EMPTY;
        final NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < remaining.size(); i++) {
            final ItemStack stack = input.getItem(i);
            ItemStack remainder = stack.getItem().getCraftingRemainder();
            if (!remainder.isEmpty()) {
                remaining.set(i, remainder);
            } else if (original.isEmpty() && !stack.isEmpty() && stack.is(FLCraftingRecipes.DYEABLE)) {
                final ItemStack rem = stack.copy();
                rem.setCount(1);
                remaining.set(i, rem);
                original = stack;
            }
        }
        return remaining;
    }



    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return FLCraftingRecipes.COPY_COLOR.get();
    }
}
