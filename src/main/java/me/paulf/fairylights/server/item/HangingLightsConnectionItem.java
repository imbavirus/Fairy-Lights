package me.paulf.fairylights.server.item;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.connection.ConnectionTypes;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import me.paulf.fairylights.server.string.StringType;
import me.paulf.fairylights.server.string.StringTypes;
import me.paulf.fairylights.util.RegistryObjects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public final class HangingLightsConnectionItem extends ConnectionItem {
    public HangingLightsConnectionItem(final Properties properties) {
        super(properties, ConnectionTypes.HANGING_LIGHTS);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip,
            final TooltipFlag flag) {
        // Use Data Components to get logic tag
        final CompoundTag compound = stack.get(FLDataComponents.CONNECTION_LOGIC);
        if (compound != null) {
            final ResourceLocation name = RegistryObjects.getName(FairyLights.STRING_TYPES.get(), getString(compound));
            tooltip.add(Component.translatable("item." + name.getNamespace() + "." + name.getPath())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (compound != null && compound.contains("pattern", Tag.TAG_LIST)) {
            final ListTag tagList = compound.getList("pattern", Tag.TAG_COMPOUND);
            final int tagCount = tagList.size();
            if (tagCount > 0) {
                tooltip.add(Component.empty());
            }
            for (int i = 0; i < tagCount; i++) {
                // ItemStack.parse() now uses context.registries()
                final ItemStack lightStack = ItemStack.parse(context.registries(), tagList.getCompound(i))
                        .orElse(ItemStack.EMPTY);
                tooltip.add(lightStack.getHoverName());
            }
        }
    }

    public static StringType getString(final CompoundTag tag) {
        if (!tag.contains("string", net.minecraft.nbt.Tag.TAG_STRING)) {
            // Default to black string if not specified
            return StringTypes.BLACK_STRING.get();
        }
        final ResourceLocation stringId = ResourceLocation.tryParse(tag.getString("string"));
        if (stringId == null) {
            return StringTypes.BLACK_STRING.get();
        }
        // Use the same pattern as CONNECTION_TYPES - get the registry and then get the
        // value by key
        final net.minecraft.core.Registry<StringType> registry = FairyLights.STRING_TYPES.get();
        final net.minecraft.resources.ResourceKey<StringType> key = net.minecraft.resources.ResourceKey
                .create(net.minecraft.resources.ResourceKey.createRegistryKey(FairyLights.STRING_TYPE), stringId);
        final StringType result = registry.get(key);
        return result != null ? result : StringTypes.BLACK_STRING.get();
    }

    public static void setString(final CompoundTag tag, final StringType string) {
        tag.putString("string", RegistryObjects.getName(FairyLights.STRING_TYPES.get(), string).toString());
    }
}
