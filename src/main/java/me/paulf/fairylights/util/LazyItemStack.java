package me.paulf.fairylights.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Function;

public final class LazyItemStack {
    private final DeferredHolder<? extends Item, ? extends Item> object;

    private final Function<? super Item, ItemStack> factory;

    private ItemStack stack;

    public LazyItemStack(final DeferredHolder<? extends Item, ? extends Item> object, final Function<? super Item, ItemStack> factory) {
        this.object = object;
        this.factory = factory;
        this.stack = ItemStack.EMPTY;
    }

    public ItemStack get() {
        if (this.stack.isEmpty()) {
            // DeferredHolder.map() doesn't exist in 1.21.1 - use value() directly
            if (this.object.value() != null) {
                this.stack = this.factory.apply(this.object.value());
            }
        }
        return this.stack;
    }
}
