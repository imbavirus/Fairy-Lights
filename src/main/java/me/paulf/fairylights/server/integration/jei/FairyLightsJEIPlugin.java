package me.paulf.fairylights.server.integration.jei;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.util.crafting.GenericRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JeiPlugin
public final class FairyLightsJEIPlugin implements IModPlugin {
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "plugin");
    }

    @Override
    public void registerVanillaCategoryExtensions(final IVanillaCategoryExtensionRegistration registration) {
        LOGGER.info("FairyLightsJEIPlugin: Registering GenericRecipeWrapper extension");
        registration.getCraftingCategory().addExtension(GenericRecipe.class, new GenericRecipeWrapper());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        LOGGER.info("FairyLightsJEIPlugin: Registering recipes");
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null) {
            LOGGER.error("FairyLightsJEIPlugin: ClientLevel is null during recipe registration!");
            return;
        }

        final RecipeManager recipeManager = world.getRecipeManager();
        List<RecipeHolder<?>> allRecipes = new ArrayList<>(recipeManager.getRecipes());
        LOGGER.info("FairyLightsJEIPlugin: RecipeManager contains " + allRecipes.size() + " total recipes.");
        
        Map<String, Long> recipesByNamespace = allRecipes.stream()
                .collect(Collectors.groupingBy(h -> h.id().getNamespace(), Collectors.counting()));
        
        LOGGER.info("FairyLightsJEIPlugin: Recipe counts by namespace:");
        recipesByNamespace.forEach((ns, count) -> LOGGER.info(" - " + ns + ": " + count));

        List<RecipeHolder<CraftingRecipe>> fairylightsRecipes = allRecipes.stream()
                .filter(holder -> holder.id().getNamespace().equals(FairyLights.ID))
                .filter(holder -> holder.value() instanceof GenericRecipe)
                .map(holder -> (RecipeHolder<CraftingRecipe>) holder)
                .collect(Collectors.toList());
        
        LOGGER.info("FairyLightsJEIPlugin: Found " + fairylightsRecipes.size() + " GenericRecipes to register.");
        fairylightsRecipes.forEach(holder -> LOGGER.info(" - Registering GenericRecipe: " + holder.id()));

        registration.addRecipes(RecipeTypes.CRAFTING, fairylightsRecipes);
    }

    @Override
    public void registerItemSubtypes(final ISubtypeRegistration registry) {
        LOGGER.info("FairyLightsJEIPlugin: Registering item subtypes");
        // Connection items with color variants
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.HANGING_LIGHTS.get(), new ColorSubtypeInterpreter());
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.PENNANT_BUNTING.get(), new ColorSubtypeInterpreter());
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.TINSEL.get(), new ColorSubtypeInterpreter());
        // Pennant items
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.TRIANGLE_PENNANT.get(), new ColorSubtypeInterpreter());
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.SPEARHEAD_PENNANT.get(), new ColorSubtypeInterpreter());
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.SWALLOWTAIL_PENNANT.get(), new ColorSubtypeInterpreter());
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.SQUARE_PENNANT.get(), new ColorSubtypeInterpreter());
        // All light items
        FLItems.lights().forEach(i -> registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, i, new ColorSubtypeInterpreter()));
    }
}
