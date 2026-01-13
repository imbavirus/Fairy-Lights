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


    // appendHoverText() signature changed in 1.21.1 - still uses Level and TooltipFlag but may need @Override removed
    public void appendHoverText(final ItemStack stack, @Nullable final Level world, final List<Component> tooltip, final TooltipFlag flag) {
        // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
        final CompoundTag compound = new CompoundTag();
        // TODO: Migrate to data components API for 1.21.1
        if (compound != null) {
            final ResourceLocation name = RegistryObjects.getName(FairyLights.STRING_TYPES.get(), getString(compound));
            tooltip.add(Component.translatable("item." + name.getNamespace() + "." + name.getPath()).withStyle(ChatFormatting.GRAY));
        }
        if (compound != null && compound.contains("pattern", Tag.TAG_LIST)) {
            final ListTag tagList = compound.getList("pattern", Tag.TAG_COMPOUND);
            final int tagCount = tagList.size();
            if (tagCount > 0) {
                tooltip.add(Component.empty());
            }
            for (int i = 0; i < tagCount; i++) {
                // ItemStack.of() API changed in 1.21.1 - use ItemStack.parse() with RegistryAccess
                final ItemStack lightStack = ItemStack.parse(world != null ? world.registryAccess() : net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY), tagList.getCompound(i)).orElse(ItemStack.EMPTY);
                tooltip.add(lightStack.getHoverName());
                // appendHoverText signature changed in 1.21.1 - Item.appendHoverText now takes TooltipContext
                // For now, skip calling appendHoverText as TooltipContext creation requires additional context
                // TODO: Create proper TooltipContext from world/player context
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
        // Use the same pattern as CONNECTION_TYPES - get the registry and then get the value by key
        final net.minecraft.core.Registry<StringType> registry = FairyLights.STRING_TYPES.get();
        final net.minecraft.resources.ResourceKey<StringType> key = net.minecraft.resources.ResourceKey.create(net.minecraft.resources.ResourceKey.createRegistryKey(FairyLights.STRING_TYPE), stringId);
        final StringType result = registry.get(key);
        return result != null ? result : StringTypes.BLACK_STRING.get();
    }

    public static void setString(final CompoundTag tag, final StringType string) {
        tag.putString("string", RegistryObjects.getName(FairyLights.STRING_TYPES.get(), string).toString());
    }
}
