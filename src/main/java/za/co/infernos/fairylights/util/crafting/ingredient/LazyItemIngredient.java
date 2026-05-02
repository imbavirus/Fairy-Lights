package za.co.infernos.fairylights.util.crafting.ingredient;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.function.Supplier;

public class LazyItemIngredient implements Supplier<Ingredient> {
    private final Supplier<? extends Item> itemSupplier;
    private Ingredient ingredient;

    public LazyItemIngredient(Supplier<? extends Item> itemSupplier) {
        this.itemSupplier = itemSupplier;
    }

    @Override
    public Ingredient get() {
        if (this.ingredient == null) {
            this.ingredient = Ingredient.of(this.itemSupplier.get());
        }
        return this.ingredient;
    }
}
