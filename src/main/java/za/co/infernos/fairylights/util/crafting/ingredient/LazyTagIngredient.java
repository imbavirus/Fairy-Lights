package za.co.infernos.fairylights.util.crafting.ingredient;

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

// In 1.21.2, Ingredient.of(TagKey) creates a lazy tag ingredient natively.
// We keep this class just as a factory to minimize changes elsewhere.
public class LazyTagIngredient {
    private final Ingredient ingredient;

    private LazyTagIngredient(final TagKey<Item> tag) {
        this.ingredient = Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(tag).map(h -> (net.minecraft.core.HolderSet<Item>)h).orElse(net.minecraft.core.HolderSet.empty()));
    }

    public Ingredient asIngredient() {
        return this.ingredient;
    }

    public static LazyTagIngredient of(final TagKey<Item> tag) {
        return new LazyTagIngredient(tag);
    }
}
