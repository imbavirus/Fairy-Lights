package me.paulf.fairylights.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.data.recipes.RecipeOutput;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = FairyLights.ID, bus = EventBusSubscriber.Bus.MOD)
public final class DataGatherer {
    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        gen.addProvider(event.includeServer(), new RecipeGenerator(gen.getPackOutput()));
        gen.addProvider(event.includeServer(), new LootTableGenerator(gen));
    }

    static class RecipeGenerator extends RecipeProvider {
        RecipeGenerator(final PackOutput generator) {
            // RecipeProvider constructor in 1.21.1 - try with CompletableFuture
            super(generator, java.util.concurrent.CompletableFuture.completedFuture(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)));
        }

        @Override
        protected void buildRecipes(final RecipeOutput consumer) {
            final CompoundTag nbt = new CompoundTag();
            nbt.put("text", StyledString.serialize(new StyledString()));
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FLItems.LETTER_BUNTING.get())
                .pattern("I-I")
                .pattern("PBF")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('-', Items.STRING)
                .define('P', Items.PAPER)
                .define('B', Items.INK_SAC)
                .define('F', Tags.Items.FEATHERS)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_string", has(Items.STRING))
                .save(addNbt(consumer, nbt));
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS,FLItems.GARLAND.get(), 2)
                .pattern("I-I")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('-', Items.VINE)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_vine", has(Items.VINE))
                .save(consumer);
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS,FLItems.OIL_LANTERN.get(), 4)
                .pattern(" I ")
                .pattern("STS")
                .pattern("IGI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('S', Items.STICK)
                .define('T', Items.TORCH)
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_torch", has(Items.TORCH))
                .save(consumer);
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS,FLItems.CANDLE_LANTERN.get(), 4)
                .pattern(" I ")
                .pattern("GTG")
                .pattern("IGI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', Tags.Items.NUGGETS_GOLD)
                .define('T', Items.TORCH)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_torch", has(Items.TORCH))
                .save(consumer);
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS,FLItems.INCANDESCENT_LIGHT.get(), 4)
                .pattern(" I ")
                .pattern("ITI")
                .pattern(" G ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .define('T', Items.TORCH)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_torch", has(Items.TORCH))
                .save(consumer);
            GenericRecipeBuilder.customRecipe(FLCraftingRecipes.HANGING_LIGHTS.get())
                .unlockedBy("has_lights", net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(FLCraftingRecipes.LIGHTS).build()))
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "hanging_lights"));
            GenericRecipeBuilder.customRecipe(FLCraftingRecipes.HANGING_LIGHTS_AUGMENTATION.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "hanging_lights_augmentation"));
            GenericRecipeBuilder.customRecipe(FLCraftingRecipes.TINSEL_GARLAND.get())
                .unlockedBy("has_iron", net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(Tags.Items.INGOTS_IRON).build()))
                .unlockedBy("has_string", net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(Items.STRING))
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "tinsel_garland"));
            GenericRecipeBuilder.customRecipe(FLCraftingRecipes.PENNANT_BUNTING.get())
                .unlockedBy("has_pennants", net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(FLCraftingRecipes.PENNANTS).build()))
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "pennant_bunting"));
            GenericRecipeBuilder.customRecipe(FLCraftingRecipes.PENNANT_BUNTING_AUGMENTATION.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "pennant_bunting_augmentation"));
            GenericRecipeBuilder.customRecipe(FLCraftingRecipes.EDIT_COLOR.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "edit_color"));
            GenericRecipeBuilder.customRecipe(FLCraftingRecipes.COPY_COLOR.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "copy_color"));
            this.pennantRecipe(FLCraftingRecipes.TRIANGLE_PENNANT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "triangle_pennant"));
            this.pennantRecipe(FLCraftingRecipes.SPEARHEAD_PENNANT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "spearhead_pennant"));
            this.pennantRecipe(FLCraftingRecipes.SWALLOWTAIL_PENNANT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "swallowtail_pennant"));
            this.pennantRecipe(FLCraftingRecipes.SQUARE_PENNANT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "square_pennant"));
            this.lightRecipe(FLCraftingRecipes.FAIRY_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "fairy_light"));
            this.lightRecipe(FLCraftingRecipes.PAPER_LANTERN.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "paper_lantern"));
            this.lightRecipe(FLCraftingRecipes.ORB_LANTERN.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "orb_lantern"));
            this.lightRecipe(FLCraftingRecipes.FLOWER_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "flower_light"));
            this.lightRecipe(FLCraftingRecipes.CANDLE_LANTERN_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "candle_lantern_light"));
            this.lightRecipe(FLCraftingRecipes.OIL_LANTERN_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "oil_lantern_light"));
            this.lightRecipe(FLCraftingRecipes.JACK_O_LANTERN.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "jack_o_lantern"));
            this.lightRecipe(FLCraftingRecipes.SKULL_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "skull_light"));
            this.lightRecipe(FLCraftingRecipes.GHOST_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "ghost_light"));
            this.lightRecipe(FLCraftingRecipes.SPIDER_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "spider_light"));
            this.lightRecipe(FLCraftingRecipes.WITCH_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "witch_light"));
            this.lightRecipe(FLCraftingRecipes.SNOWFLAKE_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "snowflake_light"));
            this.lightRecipe(FLCraftingRecipes.HEART_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "heart_light"));
            this.lightRecipe(FLCraftingRecipes.MOON_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "moon_light"));
            this.lightRecipe(FLCraftingRecipes.STAR_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "star_light"));
            this.lightRecipe(FLCraftingRecipes.ICICLE_LIGHTS.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "icicle_lights"));
            this.lightRecipe(FLCraftingRecipes.METEOR_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "meteor_light"));
            GenericRecipeBuilder.customRecipe(FLCraftingRecipes.LIGHT_TWINKLE.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "light_twinkle"));
            GenericRecipeBuilder.customRecipe(FLCraftingRecipes.COLOR_CHANGING_LIGHT.get())
                .build(consumer, ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "color_changing_light"));
        }

        GenericRecipeBuilder lightRecipe(final RecipeSerializer<?> serializer) {
            return GenericRecipeBuilder.customRecipe(serializer)
                .unlockedBy("has_iron", net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(Tags.Items.INGOTS_IRON).build()))
                .unlockedBy("has_dye", net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(Tags.Items.DYES).build()));
        }

        GenericRecipeBuilder pennantRecipe(final RecipeSerializer<?> serializer) {
            return GenericRecipeBuilder.customRecipe(serializer)
                .unlockedBy("has_paper", net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(Items.PAPER))
                .unlockedBy("has_string", net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems(Items.STRING));
        }
    }

    static class LootTableGenerator extends LootTableProvider {
        LootTableGenerator(final DataGenerator generator) {
            // LootTableProvider constructor signature in 1.21.1:
            // PackOutput, Set<ResourceKey<LootTable>>, List<SubProviderEntry>, CompletableFuture<Provider>
            super(
                generator.getPackOutput(),
                Set.of(),
                ImmutableList.of(new SubProviderEntry((provider) -> new BlockLootTableGenerator(provider), LootContextParamSets.BLOCK)),
                java.util.concurrent.CompletableFuture.completedFuture(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY))
            );
        }

        @Override
        public List<SubProviderEntry> getTables()
        {
            // getTables() may not be needed if passed to constructor, but keeping for compatibility
            return ImmutableList.of(new SubProviderEntry((provider) -> new BlockLootTableGenerator(provider), LootContextParamSets.BLOCK));
        }

        // validate() method signature changed in 1.21.1 - may not be needed or signature changed
        // Commented out for now as it may not be part of the interface in 1.21.1
        // @Override
        // protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext tracker) {
        //     // For built-in mod loot tables
        //     // for (final ResourceLocation name : Sets.difference(MyBuiltInLootTables.getAll(), map.defineSet())) {
        //     //     tracker.addProblem("Missing built-in table: " + name);
        //     // }
        //     // map.forEach((name, table) -> table.validate(tracker));
        // }
    }

    static class BlockLootTableGenerator extends BlockLootSubProvider
    {
        protected BlockLootTableGenerator(net.minecraft.core.HolderLookup.Provider provider)
        {
            // BlockLootSubProvider constructor signature changed in 1.21.1
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return FLBlocks.REG.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toList());
        }

        @Override
        protected void generate()
        {
            this.add(FLBlocks.FASTENER.get(), noDrop());
            this.add(FLBlocks.FAIRY_LIGHT.get(), noDrop());
            this.add(FLBlocks.PAPER_LANTERN.get(), noDrop());
            this.add(FLBlocks.ORB_LANTERN.get(), noDrop());
            this.add(FLBlocks.FLOWER_LIGHT.get(), noDrop());
            this.add(FLBlocks.CANDLE_LANTERN_LIGHT.get(), noDrop());
            this.add(FLBlocks.OIL_LANTERN_LIGHT.get(), noDrop());
            this.add(FLBlocks.JACK_O_LANTERN.get(), noDrop());
            this.add(FLBlocks.SKULL_LIGHT.get(), noDrop());
            this.add(FLBlocks.GHOST_LIGHT.get(), noDrop());
            this.add(FLBlocks.SPIDER_LIGHT.get(), noDrop());
            this.add(FLBlocks.WITCH_LIGHT.get(), noDrop());
            this.add(FLBlocks.SNOWFLAKE_LIGHT.get(), noDrop());
            this.add(FLBlocks.HEART_LIGHT.get(), noDrop());
            this.add(FLBlocks.MOON_LIGHT.get(), noDrop());
            this.add(FLBlocks.STAR_LIGHT.get(), noDrop());
            this.add(FLBlocks.ICICLE_LIGHTS.get(), noDrop());
            this.add(FLBlocks.METEOR_LIGHT.get(), noDrop());
            this.add(FLBlocks.OIL_LANTERN.get(), noDrop());
            this.add(FLBlocks.CANDLE_LANTERN.get(), noDrop());
            this.add(FLBlocks.INCANDESCENT_LIGHT.get(), noDrop());
        }
    }

    static RecipeOutput addNbt(final RecipeOutput consumer, final CompoundTag nbt) {
        return new RecipeOutput() {
            @Override
            public void accept(ResourceLocation id, net.minecraft.world.item.crafting.Recipe<?> recipe, @Nullable net.minecraft.advancements.AdvancementHolder advancement, net.neoforged.neoforge.common.conditions.ICondition... conditions) {
                // For NBT support, we need to handle this differently in 1.21.1
                // The recipe output system has changed, so we'll need to modify the recipe builder
                consumer.accept(id, recipe, advancement, conditions);
            }
            
            @Override
            public net.minecraft.advancements.Advancement.Builder advancement() {
                return consumer.advancement();
            }
        };
    }
}
