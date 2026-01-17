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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
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
    public void registerRecipes(final IRecipeRegistration registration) {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        final RecipeManager recipeManager = world.getRecipeManager();
        List<net.minecraft.world.item.crafting.RecipeHolder<?>> allRecipes = recipeManager.getRecipes().stream().collect(Collectors.toList());
        LOGGER.info("FairyLightsJEIPlugin: RecipeManager contains " + allRecipes.size() + " total recipes.");
        
        for (int i = 0; i < Math.min(20, allRecipes.size()); i++) {
             LOGGER.info("Sample Recipe " + i + ": " + allRecipes.get(i).id());
        }

        List<net.minecraft.world.item.crafting.RecipeHolder<?>> fairylightsRecipes = allRecipes.stream()
                .filter(holder -> holder.id().getNamespace().equals("fairylights"))
                .collect(Collectors.toList());
        
        LOGGER.info("FairyLightsJEIPlugin: Found " + fairylightsRecipes.size() + " recipes with namespace 'fairylights'.");

        List<net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>> recipes = fairylightsRecipes.stream()
                .filter(holder -> holder.value() instanceof GenericRecipe)
                .map(holder -> (net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>) holder)
                .collect(Collectors.toList());
        
        LOGGER.info("FairyLightsJEIPlugin: Registering " + recipes.size() + " GenericRecipes.");
        
        registration.addRecipes(
            RecipeTypes.CRAFTING,
            recipes);
    }

    @Override
    public void registerItemSubtypes(final ISubtypeRegistration registry) {
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.TINSEL.get(), new ColorSubtypeInterpreter());
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.TRIANGLE_PENNANT.get(), new ColorSubtypeInterpreter());
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.SPEARHEAD_PENNANT.get(), new ColorSubtypeInterpreter());
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.SWALLOWTAIL_PENNANT.get(), new ColorSubtypeInterpreter());
        registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, FLItems.SQUARE_PENNANT.get(), new ColorSubtypeInterpreter());
        FLItems.lights().forEach(i -> registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, i, new ColorSubtypeInterpreter()));
    }
}


