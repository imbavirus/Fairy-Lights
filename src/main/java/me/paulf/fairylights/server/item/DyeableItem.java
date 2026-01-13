package me.paulf.fairylights.server.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public final class DyeableItem {
    private DyeableItem() {}

    public static Component getColorName(final int color) {
        final int r = color >> 16 & 0xFF;
        final int g = color >> 8 & 0xFF;
        final int b = color & 0xFF;
        DyeColor closest = DyeColor.WHITE;
        int closestDist = Integer.MAX_VALUE;
        for (final DyeColor dye : DyeColor.values()) {
            final int dyeColor = getColor(dye);
            if (dyeColor == color) {
                closest = dye;
                closestDist = 0;
                break;
            }
            final int dr = dyeColor >> 16 & 0xFF;
            final int dg = dyeColor >> 8 & 0xFF;
            final int db = dyeColor & 0xFF;
            final int dist = (dr - r) * (dr - r) + (dg - g) * (dg - g) + (db - b) * (db - b);
            if (dist < closestDist) {
                closest = dye;
                closestDist = dist;
            }
        }
        final Component colorName = Component.translatable("color.fairylights." + closest.getName());
        return closestDist == 0 ? colorName : Component.translatable("format.fairylights.dyed_colored", colorName);
    }

    public static Component getDisplayName(final ItemStack stack, final Component name) {
        return Component.translatable("format.fairylights.colored", getColorName(getColor(stack)), name);
    }

    public static int getColor(final DyeColor color) {
        if (color == DyeColor.BLACK) {
            return 0x323232;
        }
        if (color == DyeColor.GRAY) {
            return 0x606060;
        }
        // DyeColor.getTextureDiffuseColors() removed in 1.21.1 - use getTextureDiffuseColor() which returns int
        final int textureColor = color.getTextureDiffuseColor();
        return textureColor;
    }

    public static Optional<DyeColor> getDyeColor(final ItemStack stack) {
        final int color = getColor(stack);
        return Arrays.stream(DyeColor.values()).filter(dye -> getColor(dye) == color).findFirst();
    }

    public static ItemStack setColor(final ItemStack stack, final DyeColor dye) {
        return setColor(stack, getColor(dye));
    }

    public static ItemStack setColor(final ItemStack stack, final int color) {
        // In 1.21.1, ItemStack NBT access is deprecated, but we need it for colors
        // Try reflection first (more reliable for setting NBT)
        try {
            final java.lang.reflect.Method getTag = stack.getClass().getMethod("getTag");
            CompoundTag tag = (CompoundTag) getTag.invoke(stack);
            if (tag == null) {
                tag = new CompoundTag();
                final java.lang.reflect.Method setTag = stack.getClass().getMethod("setTag", CompoundTag.class);
                setTag.invoke(stack, tag);
            }
            setColor(tag, color);
            return stack;
        } catch (Exception e) {
            // Fallback: try save/parse approach
            try {
                final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
                final Tag savedTag = stack.save(registryAccess);
                if (savedTag instanceof CompoundTag) {
                    final CompoundTag tag = (CompoundTag) savedTag;
                    setColor(tag, color);
                    final ItemStack result = ItemStack.parse(registryAccess, tag).orElse(stack);
                    result.setCount(stack.getCount());
                    return result;
                }
            } catch (Exception e2) {
                // Both methods failed - log for debugging
                org.apache.logging.log4j.LogManager.getLogger().warn("[FairyLights] DyeableItem.setColor failed for color 0x{}: reflection={}, save/parse={}", 
                    Integer.toHexString(color), e.getMessage(), e2.getMessage());
            }
        }
        return stack;
    }

    public static CompoundTag setColor(final CompoundTag tag, final DyeColor dye) {
        return setColor(tag, getColor(dye));
    }

    public static CompoundTag setColor(final CompoundTag tag, final int color) {
        tag.putInt("color", color);
        return tag;
    }

    public static int getColor(final ItemStack stack) {
        // In 1.21.1, ItemStack NBT access is deprecated, but we need it for colors
        // Try reflection first (more reliable for reading NBT)
        try {
            final java.lang.reflect.Method getTag = stack.getClass().getMethod("getTag");
            final CompoundTag tag = (CompoundTag) getTag.invoke(stack);
            if (tag != null) {
                return getColor(tag);
            }
        } catch (Exception e) {
            // Fallback: try save/parse approach
            try {
                final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
                final Tag savedTag = stack.save(registryAccess);
                if (savedTag instanceof CompoundTag) {
                    final CompoundTag tag = (CompoundTag) savedTag;
                    return getColor(tag);
                }
            } catch (Exception e2) {
                // Both methods failed
            }
        }
        return 0xFFFFFF;
    }

    public static int getColor(final CompoundTag tag) {
        return tag.contains("color", Tag.TAG_INT) ? tag.getInt("color") : 0xFFFFFF;
    }
}
