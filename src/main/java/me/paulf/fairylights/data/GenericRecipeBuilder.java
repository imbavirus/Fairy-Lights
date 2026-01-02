package me.paulf.fairylights.data;

import com.google.gson.JsonObject;
import me.paulf.fairylights.FairyLights;
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
        // Check if criteria is not empty - try to access criteria map size
        try {
            // Try to check if we have any criteria by attempting to build
            final ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "recipes/" + FairyLights.ID + "/" + id.getPath());
            // Create advancement builder with parent and recipe unlock trigger
            final Advancement.Builder builder = this.advancementBuilder.parent(ResourceLocation.parse("minecraft:recipes/root"))
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(net.minecraft.advancements.AdvancementRequirements.Strategy.OR);
            advancement = builder.build(advancementId);
        } catch (Exception e) {
            // If building fails, advancement stays null
        }
        // Create a simple recipe for now - the actual recipe creation will need to be handled by the serializer
        consumer.accept(id, new Result(this.serializer, id), advancement);
    }

    public static GenericRecipeBuilder customRecipe(final RecipeSerializer<?> serializer) {
        return new GenericRecipeBuilder(serializer);
    }

    static class Result implements net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.CraftingInput> {
        final RecipeSerializer<?> serializer;
        final ResourceLocation id;

        public Result(final RecipeSerializer<?> serializer, final ResourceLocation id) {
            this.serializer = serializer;
            this.id = id;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return this.serializer;
        }

        // Recipe interface methods
        @Override
        public net.minecraft.world.item.crafting.RecipeType<?> getType() {
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
        public boolean canCraftInDimensions(int width, int height) {
            return true;
        }

        @Override
        public net.minecraft.world.item.ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider provider) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
    }
}

