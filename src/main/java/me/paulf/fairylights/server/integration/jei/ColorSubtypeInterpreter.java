package me.paulf.fairylights.server.integration.jei;

import me.paulf.fairylights.server.item.FLDataComponents;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

public final class ColorSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

    @Override
    public String apply(final ItemStack stack, final UidContext context) {
        StringBuilder sb = new StringBuilder();

        // 1. Base Item Color
        // 1. Base Item Color
        // Only check color if there is no connection logic, as connection items use logic for identity
        // and the base color tag is often just for the creative tab icon.
        if (stack.has(FLDataComponents.COLOR.get()) && !stack.has(FLDataComponents.CONNECTION_LOGIC.get())) {
            sb.append(String.format("C:%06x", stack.get(FLDataComponents.COLOR.get())));
        }

        // 2. Connection Logic (String type & Pattern)
        if (stack.has(FLDataComponents.CONNECTION_LOGIC.get())) {
             net.minecraft.nbt.CompoundTag logic = stack.get(FLDataComponents.CONNECTION_LOGIC.get());
             
             // String Type
             // if (logic.contains("string", net.minecraft.nbt.Tag.TAG_STRING)) {
             //     sb.append("_S:").append(logic.getString("string"));
             // }
             
             // Pattern List
             /*
             if (logic.contains("pattern", net.minecraft.nbt.Tag.TAG_LIST)) {
                 net.minecraft.nbt.ListTag pattern = logic.getList("pattern", net.minecraft.nbt.Tag.TAG_COMPOUND);
                 sb.append("_P:[");
                 final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.client.Minecraft.getInstance().level != null ? 
                         net.minecraft.client.Minecraft.getInstance().level.registryAccess() : 
                         net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
                 
                 for (int i = 0; i < pattern.size(); i++) {
                     net.minecraft.nbt.CompoundTag itemTag = pattern.getCompound(i);
                     // Parse item to be safe/stable, or just read ID if possible. 
                     // Parsing is safest to normalize data.
                     Optional<ItemStack> itemOpt = ItemStack.parse(registryAccess, itemTag);
                     if (itemOpt.isPresent()) {
                         ItemStack item = itemOpt.get();
                         sb.append(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem()));
                         if (item.has(FLDataComponents.COLOR.get())) {
                             sb.append(String.format("@%06x", item.get(FLDataComponents.COLOR.get())));
                         }
                         sb.append(";");
                     } else {
                         sb.append("?;");
                     }
                 }
                 sb.append("]");
             }
             */
        }
        
        if (sb.length() > 0) {
            String result = sb.toString();
            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals("hanging_lights")) {
                 LOGGER.info("ColorSubtypeInterpreter: " + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()) + " -> " + result);
            }
            return result;
        }
        
        return IIngredientSubtypeInterpreter.NONE;
    }
}
