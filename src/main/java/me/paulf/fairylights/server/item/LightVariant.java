package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.feature.light.LightBehavior;
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

    // TODO: Refactor to use NeoForge 1.21.1 data attachment system
    static Optional<LightVariant<?>> get(final ItemStack stack) {
        // For now, return empty - needs proper implementation
        return Optional.empty();
    }
}
