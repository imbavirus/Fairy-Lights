package za.co.infernos.fairylights.server.item;

import za.co.infernos.fairylights.server.feature.light.LightBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

public interface LightVariant<T extends LightBehavior> {
    // Capabilities API removed in NeoForge 1.21.1 - item capabilities work differently
    // This will need to be refactored to use data attachments or a different approach

    boolean parallelsCord();

    float getSpacing();

    AABB getBounds();

    double getFloorOffset();

    T createBehavior(final ItemStack stack);

    boolean isOrientable();

    // Get the LightVariant from an ItemStack
    // If the item is a LightItem, get the variant from its LightBlock
    static Optional<LightVariant<?>> get(final ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        final var item = stack.getItem();
        if (item instanceof LightItem lightItem) {
            final var block = lightItem.getBlock();
            if (block instanceof za.co.infernos.fairylights.server.block.LightBlock lightBlock) {
                return Optional.of(lightBlock.getVariant());
            }
        }
        return Optional.empty();
    }
}
