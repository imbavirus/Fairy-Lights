package me.paulf.fairylights.util.crafting.ingredient;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// Ingredient is final in Minecraft 1.21.1, so we use composition instead of inheritance
public class LazyTagIngredient {
    private final Ingredient ingredient;
    private final TagKey<Item> tag;

    private LazyTagIngredient(final TagKey<Item> tag) {
        this.tag = tag;
        // Create an Ingredient from the tag
        this.ingredient = Ingredient.of(StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false)
            .map(ItemStack::new));
    }

    public ItemStack[] getItems() {
        return this.ingredient.getItems();
    }

    public boolean test(@Nullable final ItemStack stack) {
        return this.ingredient.test(stack);
    }

    public IntList getStackingIds() {
        return this.ingredient.getStackingIds();
    }

    public boolean isEmpty() {
        return this.ingredient.isEmpty();
    }

    // Get the underlying Ingredient for use in recipes
    public Ingredient asIngredient() {
        return this.ingredient;
    }

    public static LazyTagIngredient of(final TagKey<Item> tag) {
        return new LazyTagIngredient(tag);
    }
}
