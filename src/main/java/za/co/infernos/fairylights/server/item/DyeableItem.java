package za.co.infernos.fairylights.server.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import java.util.Arrays;
import java.util.Optional;

public final class DyeableItem {
    private DyeableItem() {
    }

    /** Force opaque ARGB — 1.21 ItemColor treats alpha 0 as fully transparent. */
    public static int opaque(final int rgb) {
        return (rgb & 0x00FFFFFF) | 0xFF000000;
    }

    public static Component getColorName(final int color) {
        final int rgb = color & 0xFFFFFF;
        final int r = rgb >> 16 & 0xFF;
        final int g = rgb >> 8 & 0xFF;
        final int b = rgb & 0xFF;
        DyeColor closest = DyeColor.WHITE;
        int closestDist = Integer.MAX_VALUE;
        for (final DyeColor dye : DyeColor.values()) {
            final int dyeColor = getColor(dye) & 0xFFFFFF;
            if (dyeColor == rgb) {
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
            return opaque(0x323232);
        }
        if (color == DyeColor.GRAY) {
            return opaque(0x606060);
        }
        return opaque(color.getTextureDiffuseColor());
    }

    public static Optional<DyeColor> getDyeColor(final ItemStack stack) {
        final int color = getColor(stack) & 0xFFFFFF;
        return Arrays.stream(DyeColor.values()).filter(dye -> (getColor(dye) & 0xFFFFFF) == color).findFirst();
    }

    public static ItemStack setColor(final ItemStack stack, final DyeColor dye) {
        return setColor(stack, getColor(dye));
    }

    public static ItemStack setColor(final ItemStack stack, final int color) {
        stack.set(FLDataComponents.COLOR.get(), opaque(color));
        return stack;
    }

    public static CompoundTag setColor(final CompoundTag tag, final DyeColor dye) {
        return setColor(tag, getColor(dye));
    }

    public static CompoundTag setColor(final CompoundTag tag, final int color) {
        tag.putInt("color", opaque(color));
        return tag;
    }

    public static int getColor(final ItemStack stack) {
        if (stack.has(FLDataComponents.COLOR.get())) {
            return opaque(stack.get(FLDataComponents.COLOR.get()));
        }

        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            final CompoundTag tag = customData.getUnsafe();
            if (tag.contains("color", Tag.TAG_ANY_NUMERIC)) {
                return opaque(tag.getInt("color"));
            }
            if (tag.contains("fl_backup_color", Tag.TAG_ANY_NUMERIC)) {
                return opaque(tag.getInt("fl_backup_color"));
            }
        }

        final CompoundTag logic = stack.get(FLDataComponents.CONNECTION_LOGIC.get());
        if (logic != null) {
            if (logic.contains("color", Tag.TAG_ANY_NUMERIC)) {
                return opaque(logic.getInt("color"));
            }
            if (logic.contains("fl_backup_color", Tag.TAG_ANY_NUMERIC)) {
                return opaque(logic.getInt("fl_backup_color"));
            }
        }

        return 0xFFFFFFFF;
    }

    public static int getColor(final CompoundTag tag) {
        if (tag.contains("color", Tag.TAG_ANY_NUMERIC)) {
            return opaque(tag.getInt("color"));
        }
        if (tag.contains("fl_backup_color", Tag.TAG_ANY_NUMERIC)) {
            return opaque(tag.getInt("fl_backup_color"));
        }
        return 0xFFFFFFFF;
    }
}
