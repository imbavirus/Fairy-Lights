package me.paulf.fairylights.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class RegistryObjects {
    private RegistryObjects() {}

    public static <T> ResourceLocation getName(@NotNull Registry<T> registry, final T type) {
        final ResourceLocation name = registry.getKey(type);
        if (name == null) {
            throw new NullPointerException("Missing registry name: " + type);
        }
        return name;
    }
}
