package za.co.infernos.fairylights.data;

import com.google.gson.JsonObject;
import za.co.infernos.fairylights.FairyLights;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericRecipeBuilder {
    private final RecipeSerializer<?> serializer;

    private final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();

    public GenericRecipeBuilder(final RecipeSerializer<?> serializer) {
        this.serializer = Objects.requireNonNull(serializer, "serializer");
    }

    public GenericRecipeBuilder unlockedBy(final String name, final net.minecraft.advancements.Criterion<?> criterion) {
        this.advancementBuilder.addCriterion(name, criterion);
        return this;
    }

    public void build(final RecipeOutput consumer, final ResourceLocation id) {
        AdvancementHolder advancement = null;
        net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipeKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id);
        try {
            final ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "recipes/" + FairyLights.ID + "/" + id.getPath());
            final Advancement.Builder builder = this.advancementBuilder.parent(ResourceLocation.parse("minecraft:recipes/root"))
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeKey))
                .rewards(AdvancementRewards.Builder.recipe(recipeKey))
                .requirements(net.minecraft.advancements.AdvancementRequirements.Strategy.OR);
            advancement = builder.build(advancementId);
        } catch (Exception e) {
        }
        consumer.accept(recipeKey, new Result(this.serializer, id), advancement);
    }

    public static GenericRecipeBuilder customRecipe(final RecipeSerializer<?> serializer) {
        return new GenericRecipeBuilder(serializer);
    }

    static class Result implements net.minecraft.world.item.crafting.CraftingRecipe {
        final RecipeSerializer<?> serializer;
        final ResourceLocation id;

        public Result(final RecipeSerializer<?> serializer, final ResourceLocation id) {
            this.serializer = serializer;
            this.id = id;
        }

        @Override
        public RecipeSerializer<? extends net.minecraft.world.item.crafting.CraftingRecipe> getSerializer() {
            return (RecipeSerializer<? extends net.minecraft.world.item.crafting.CraftingRecipe>) this.serializer;
        }

        @Override
        public net.minecraft.world.item.crafting.CraftingBookCategory category() {
            return net.minecraft.world.item.crafting.CraftingBookCategory.MISC;
        }

        // Recipe interface methods
        @Override
        public net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.CraftingRecipe> getType() {
            return net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        }

        @Override
        public boolean matches(net.minecraft.world.item.crafting.CraftingInput input, net.minecraft.world.level.Level level) {
            return false; // This is a placeholder - actual matching is handled by the serializer
        }

        @Override
        public net.minecraft.world.item.ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput input, net.minecraft.core.HolderLookup.Provider provider) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }



        @Override
        public net.minecraft.world.item.crafting.PlacementInfo placementInfo() {
            return net.minecraft.world.item.crafting.PlacementInfo.NOT_PLACEABLE;
        }

        @Override
        public net.minecraft.world.item.crafting.RecipeBookCategory recipeBookCategory() {
            return net.minecraft.world.item.crafting.RecipeBookCategories.CRAFTING_MISC;
        }
    }
}

