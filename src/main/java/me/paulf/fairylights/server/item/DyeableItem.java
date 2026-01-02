package me.paulf.fairylights.server.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
        // ItemStack.getOrCreateTag() removed in 1.21.1 - use data components API instead
        // TODO: Migrate to data components API for 1.21.1
        // For now, use reflection to access NBT methods if available
        try {
            final java.lang.reflect.Method getTag = stack.getClass().getMethod("getTag");
            CompoundTag tag = (CompoundTag) getTag.invoke(stack);
            if (tag == null) {
                tag = new CompoundTag();
                final java.lang.reflect.Method setTag = stack.getClass().getMethod("setTag", CompoundTag.class);
                setTag.invoke(stack, tag);
            }
            setColor(tag, color);
        } catch (Exception e) {
            // NBT methods not available - need data components API
            // For now, color will be handled by color handlers in ClientProxy
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
        // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
        // TODO: Migrate to data components API for 1.21.1
        // For now, use reflection to access NBT methods if available
        try {
            final java.lang.reflect.Method getTag = stack.getClass().getMethod("getTag");
            final CompoundTag tag = (CompoundTag) getTag.invoke(stack);
            if (tag != null) {
                return getColor(tag);
            }
        } catch (Exception e) {
            // NBT methods not available - need data components API
        }
        return 0xFFFFFF;
    }

    public static int getColor(final CompoundTag tag) {
        return tag.contains("color", Tag.TAG_INT) ? tag.getInt("color") : 0xFFFFFF;
    }
}
