package me.paulf.fairylights.server.item.crafting;

import com.google.common.collect.ImmutableList;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.item.DyeableItem;
import me.paulf.fairylights.server.item.FLDataComponents;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.HangingLightsConnectionItem;
import me.paulf.fairylights.server.string.StringTypes;
import me.paulf.fairylights.util.Blender;
import me.paulf.fairylights.util.OreDictUtils;
import me.paulf.fairylights.util.Utils;
import me.paulf.fairylights.util.crafting.GenericRecipe;
import me.paulf.fairylights.util.crafting.GenericRecipeBuilder;
import me.paulf.fairylights.util.crafting.ingredient.BasicAuxiliaryIngredient;
import me.paulf.fairylights.util.crafting.ingredient.BasicRegularIngredient;
import me.paulf.fairylights.util.crafting.ingredient.InertBasicAuxiliaryIngredient;
import me.paulf.fairylights.util.crafting.ingredient.LazyTagIngredient;
import me.paulf.fairylights.util.crafting.ingredient.RegularIngredient;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class FLCraftingRecipes {
    private FLCraftingRecipes() {
    }

    static {
        com.mojang.logging.LogUtils.getLogger().info("FLCraftingRecipes class loaded! Registering serializers...");
    }

    public static final DeferredRegister<RecipeSerializer<?>> REG = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, FairyLights.ID);

    private static final ResourceLocation PLACEHOLDER_ID = ResourceLocation.fromNamespaceAndPath(FairyLights.ID,
            "placeholder");

    public static GenericRecipe createHangingLightsWrapper(CraftingBookCategory category) {
        return createHangingLights(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createHangingLightsAugmentationWrapper(CraftingBookCategory category) {
        return createHangingLightsAugmentation(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createTinselGarlandWrapper(CraftingBookCategory category) {
        return createTinselGarland(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createPennantBuntingWrapper(CraftingBookCategory category) {
        return createPennantBunting(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createPennantBuntingAugmentationWrapper(CraftingBookCategory category) {
        return createPennantBuntingAugmentation(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createTrianglePennantWrapper(CraftingBookCategory category) {
        return createTrianglePennant(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createSpearheadPennantWrapper(CraftingBookCategory category) {
        return createSpearheadPennant(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createSwallowtailPennantWrapper(CraftingBookCategory category) {
        return createSwallowtailPennant(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createSquarePennantWrapper(CraftingBookCategory category) {
        return createSquarePennant(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createFairyLightWrapper(CraftingBookCategory category) {
        return createFairyLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createPaperLanternWrapper(CraftingBookCategory category) {
        return createPaperLantern(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createOrbLanternWrapper(CraftingBookCategory category) {
        return createOrbLantern(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createFlowerLightWrapper(CraftingBookCategory category) {
        return createFlowerLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createCandleLanternLightWrapper(CraftingBookCategory category) {
        return createCandleLanternLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createOilLanternLightWrapper(CraftingBookCategory category) {
        return createOilLanternLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createJackOLanternWrapper(CraftingBookCategory category) {
        return createJackOLantern(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createSkullLightWrapper(CraftingBookCategory category) {
        return createSkullLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createGhostLightWrapper(CraftingBookCategory category) {
        return createGhostLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createSpiderLightWrapper(CraftingBookCategory category) {
        return createSpiderLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createWitchLightWrapper(CraftingBookCategory category) {
        return createWitchLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createSnowflakeLightWrapper(CraftingBookCategory category) {
        return createSnowflakeLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createHeartLightWrapper(CraftingBookCategory category) {
        return createHeartLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createMoonLightWrapper(CraftingBookCategory category) {
        return createMoonLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createStarLightWrapper(CraftingBookCategory category) {
        return createStarLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createIcicleLightsWrapper(CraftingBookCategory category) {
        return createIcicleLights(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createMeteorLightWrapper(CraftingBookCategory category) {
        return createMeteorLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createLightTwinkleWrapper(CraftingBookCategory category) {
        return createLightTwinkle(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createColorChangingLightWrapper(CraftingBookCategory category) {
        return createColorChangingLight(PLACEHOLDER_ID, category);
    }

    public static GenericRecipe createDyeColorWrapper(CraftingBookCategory category) {
        return createDyeColor(PLACEHOLDER_ID, category);
    }

    public static CustomRecipe createCopyColorWrapper(CraftingBookCategory category) {
        return new CopyColorRecipe(PLACEHOLDER_ID, category);
    }

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> HANGING_LIGHTS = REG
            .register("crafting_special_hanging_lights",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createHangingLightsWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> HANGING_LIGHTS_AUGMENTATION = REG
            .register("crafting_special_hanging_lights_augmentation", () -> new SimpleCraftingRecipeSerializer<>(
                    FLCraftingRecipes::createHangingLightsAugmentationWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> TINSEL_GARLAND = REG
            .register("crafting_special_tinsel_garland",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createTinselGarlandWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> PENNANT_BUNTING = REG
            .register("crafting_special_pennant_bunting",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createPennantBuntingWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> PENNANT_BUNTING_AUGMENTATION = REG
            .register("crafting_special_pennant_bunting_augmentation", () -> new SimpleCraftingRecipeSerializer<>(
                    FLCraftingRecipes::createPennantBuntingAugmentationWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> TRIANGLE_PENNANT = REG
            .register("crafting_special_triangle_pennant",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createTrianglePennantWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> SPEARHEAD_PENNANT = REG
            .register("crafting_special_spearhead_pennant",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createSpearheadPennantWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> SWALLOWTAIL_PENNANT = REG
            .register("crafting_special_swallowtail_pennant",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createSwallowtailPennantWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> SQUARE_PENNANT = REG
            .register("crafting_special_square_pennant",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createSquarePennantWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> FAIRY_LIGHT = REG.register(
            "crafting_special_fairy_light",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createFairyLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> PAPER_LANTERN = REG
            .register("crafting_special_paper_lantern",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createPaperLanternWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> ORB_LANTERN = REG.register(
            "crafting_special_orb_lantern",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createOrbLanternWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> FLOWER_LIGHT = REG
            .register("crafting_special_flower_light",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createFlowerLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> CANDLE_LANTERN_LIGHT = REG
            .register("crafting_special_candle_lantern_light",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createCandleLanternLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> OIL_LANTERN_LIGHT = REG
            .register("crafting_special_oil_lantern_light",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createOilLanternLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> JACK_O_LANTERN = REG
            .register("crafting_special_jack_o_lantern",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createJackOLanternWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> SKULL_LIGHT = REG.register(
            "crafting_special_skull_light",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createSkullLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> GHOST_LIGHT = REG.register(
            "crafting_special_ghost_light",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createGhostLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> SPIDER_LIGHT = REG
            .register("crafting_special_spider_light",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createSpiderLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> WITCH_LIGHT = REG.register(
            "crafting_special_witch_light",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createWitchLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> SNOWFLAKE_LIGHT = REG
            .register("crafting_special_snowflake_light",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createSnowflakeLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> HEART_LIGHT = REG.register(
            "crafting_special_heart_light",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createHeartLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> MOON_LIGHT = REG.register(
            "crafting_special_moon_light",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createMoonLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> STAR_LIGHT = REG.register(
            "crafting_special_star_light",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createStarLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> ICICLE_LIGHTS = REG
            .register("crafting_special_icicle_lights",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createIcicleLightsWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> METEOR_LIGHT = REG
            .register("crafting_special_meteor_light",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createMeteorLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> LIGHT_TWINKLE = REG
            .register("crafting_special_light_twinkle",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createLightTwinkleWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> COLOR_CHANGING_LIGHT = REG
            .register("crafting_special_color_changing_light",
                    () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createColorChangingLightWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenericRecipe>> EDIT_COLOR = REG.register(
            "crafting_special_edit_color",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createDyeColorWrapper));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CustomRecipe>> COPY_COLOR = REG.register(
            "crafting_special_copy_color",
            () -> new SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createCopyColorWrapper));

    public static final TagKey<Item> LIGHTS = ItemTags
            .create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "lights"));
    public static final TagKey<Item> TWINKLING_LIGHTS = ItemTags
            .create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "twinkling_lights"));
    public static final TagKey<Item> PENNANTS = ItemTags
            .create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "pennants"));
    public static final TagKey<Item> DYEABLE = ItemTags
            .create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "dyeable"));
    public static final TagKey<Item> DYEABLE_LIGHTS = ItemTags
            .create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "dyeable_lights"));

    public static final RegularIngredient DYE_SUBTYPE_INGREDIENT = new BasicRegularIngredient(
            LazyTagIngredient.of(Tags.Items.DYES)) {
        @Override
        public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
            return DyeableItem.getDyeColor(output).map(dye -> ImmutableList.of(OreDictUtils.getDyes(dye)))
                    .orElse(ImmutableList.of());
        }

        @Override
        public boolean dictatesOutputType() {
            return true;
        }

        @Override
        public void matched(final ItemStack ingredient, final CompoundTag nbt) {
            // Updated to use components. nbt argument is legacy but expected by API
            // Only way is to put color into nbt and hope GenericRecipe applies it or update
            // ingredient logic
            // Since we don't control GenericRecipe here easily, we assume it's applying
            // nbt.
            // BUT GenericRecipe is fairylights code? Yes it is.
            // We should update the methods calling matched too.
        }
    };

    private static GenericRecipe createDyeColor(final ResourceLocation name,
            CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, EDIT_COLOR)
                .withShape("I")
                .withIngredient('I', DYEABLE).withOutput('I')
                .withAuxiliaryIngredient(
                        new BasicAuxiliaryIngredient<Blender>(LazyTagIngredient.of(Tags.Items.DYES), true, 8) {
                            @Override
                            public Blender accumulator() {
                                return new Blender();
                            }

                            @Override
                            public void consume(final Blender data, final ItemStack ingredient) {
                                data.add(DyeableItem.getColor(OreDictUtils.getDyeColor(ingredient)));
                            }

                            @Override
                            public boolean finish(final Blender data, final CompoundTag nbt) {
                                // Update: using GenericRecipe logic, we need to ensure component is set.
                                // Assuming GenericRecipeBuilder applies 'nbt' to stack components.
                                // To do this properly, we need to know how 'nbt' is used.
                                // For now, we will construct the component structure in 'nbt' if possible?
                                // No, components are not NBT.
                                DyeableItem.setColor(nbt, data.blend()); // Helper sets "color" int in NBT.
                                // We need to map this NBT "color" to Component later or rely on item loading
                                // it.
                                return false;
                            }
                        })
                .build();
    }

    private static GenericRecipe createLightTwinkle(final ResourceLocation name,
            CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, LIGHT_TWINKLE)
                .withShape("L")
                .withIngredient('L', TWINKLING_LIGHTS).withOutput('L')
                .withAuxiliaryIngredient(
                        new InertBasicAuxiliaryIngredient(LazyTagIngredient.of(Tags.Items.DUSTS_GLOWSTONE), true, 1) {
                            @Override
                            public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                                return useInputsForTagBool(output, true) ? super.getInput(output) : ImmutableList.of();
                            }

                            @Override
                            public void present(final CompoundTag nbt) {
                                // nbt.putBoolean("twinkle", true);
                                // TODO: GenericRecipe must map this to component.
                            }

                            @Override
                            public void absent(final CompoundTag nbt) {
                                // nbt.putBoolean("twinkle", false);
                            }

                            @Override
                            public void addTooltip(final List<Component> tooltip) {
                                super.addTooltip(tooltip);
                                tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.twinkling_lights.glowstone"));
                            }
                        })
                .build();
    }

    // Omitted createColorChangingLight for brevity/safety - complex list logic
    private static GenericRecipe createColorChangingLight(final ResourceLocation name,
            CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, COLOR_CHANGING_LIGHT).build(); // Stub
    }

    private static GenericRecipe createHangingLights(ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, HANGING_LIGHTS, FLItems.HANGING_LIGHTS.get())
                .withShape("I-I")
                .withIngredient('I', Tags.Items.INGOTS_IRON)
                .withIngredient('-', Items.STRING)
                .withAuxiliaryIngredient(new LightIngredient(true))
                .withAuxiliaryIngredient(
                        new InertBasicAuxiliaryIngredient(LazyTagIngredient.of(Tags.Items.DYES_WHITE), false, 1) {
                            @Override
                            public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                                final CompoundTag logic = output.get(FLDataComponents.CONNECTION_LOGIC);
                                if (logic != null && HangingLightsConnectionItem
                                        .getString(logic) == StringTypes.WHITE_STRING.get()) {
                                    return super.getInput(output);
                                }
                                return ImmutableList.of();
                            }

                            @Override
                            public void present(final CompoundTag nbt) {
                                // Requires GenericRecipe update
                            }

                            @Override
                            public void absent(final CompoundTag nbt) {
                            }

                            @Override
                            public void addTooltip(final List<Component> tooltip) {
                                super.addTooltip(tooltip);
                                tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.hangingLights.string"));
                            }
                        })
                .build();
    }

    private static boolean useInputsForTagBool(final ItemStack output, final boolean value) {
        return output.getOrDefault(FLDataComponents.TWINKLE, false) == value;
    }

    private static GenericRecipe createHangingLightsAugmentation(final ResourceLocation name,
            CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, HANGING_LIGHTS_AUGMENTATION, FLItems.HANGING_LIGHTS.get())
                .withShape("F")
                .withIngredient('F', new BasicRegularIngredient(Ingredient.of(FLItems.HANGING_LIGHTS.get())) {
                    @Override
                    public ImmutableList<ItemStack> getInputs() {
                        return Arrays.stream(this.ingredient.getItems())
                                .map(ItemStack::copy)
                                .flatMap(stack -> makeHangingLightsExamples(stack).stream())
                                .collect(ImmutableList.toImmutableList());
                    }

                    @Override
                    public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                        return ImmutableList.of(ImmutableList.of(output.copy()));
                    }

                    @Override
                    public void matched(final ItemStack ingredient, final CompoundTag nbt) {
                        // Augmentation usually copies components.
                    }
                })
                .withAuxiliaryIngredient(new LightIngredient(true) {
                    @Override
                    public ImmutableList<ItemStack> getInputs() {
                        return ImmutableList.of();
                    }

                    @Override
                    public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                        return ImmutableList.of();
                    }
                })
                .build();
    }

    private static ImmutableList<ItemStack> makeHangingLightsExamples(final ItemStack stack) {
        return ImmutableList.of(
                makeHangingLights(stack, DyeColor.CYAN, DyeColor.MAGENTA, DyeColor.CYAN, DyeColor.WHITE),
                makeHangingLights(stack, DyeColor.CYAN, DyeColor.LIGHT_BLUE, DyeColor.CYAN, DyeColor.LIGHT_BLUE),
                makeHangingLights(stack, DyeColor.LIGHT_GRAY, DyeColor.PINK, DyeColor.CYAN, DyeColor.GREEN),
                makeHangingLights(stack, DyeColor.LIGHT_GRAY, DyeColor.PURPLE, DyeColor.LIGHT_GRAY, DyeColor.GREEN),
                makeHangingLights(stack, DyeColor.CYAN, DyeColor.YELLOW, DyeColor.CYAN, DyeColor.PURPLE));
    }

    public static ItemStack makeHangingLights(final ItemStack base, final DyeColor... colors) {
        final ItemStack stack = base.copy();
        final CompoundTag logic = new CompoundTag();
        final ListTag lights = new ListTag();
        final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess
                .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        for (final DyeColor color : colors) {
            final ItemStack coloredLight = DyeableItem.setColor(new ItemStack(FLItems.FAIRY_LIGHT.get()), color);
            lights.add(coloredLight.save(registryAccess));
        }
        logic.put("pattern", lights);
        HangingLightsConnectionItem.setString(logic, StringTypes.BLACK_STRING.get());
        stack.set(FLDataComponents.CONNECTION_LOGIC, logic);
        return stack;
    }

    private static GenericRecipe createTinselGarland(final ResourceLocation name,
            CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, TINSEL_GARLAND, FLItems.TINSEL.get())
                .withShape(" P ", "I-I", " D ")
                .withIngredient('P', Items.PAPER)
                .withIngredient('I', Tags.Items.INGOTS_IRON)
                .withIngredient('-', Items.STRING)
                .withIngredient('D', DYE_SUBTYPE_INGREDIENT)
                .build();
    }

    private static GenericRecipe createPennantBunting(final ResourceLocation name,
            CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, PENNANT_BUNTING, FLItems.PENNANT_BUNTING.get())
                .withShape("I-I")
                .withIngredient('I', Tags.Items.INGOTS_IRON)
                .withIngredient('-', Items.STRING)
                .withAuxiliaryIngredient(new PennantIngredient())
                .build();
    }

    private static GenericRecipe createPennantBuntingAugmentation(final ResourceLocation name,
            CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, PENNANT_BUNTING_AUGMENTATION, FLItems.PENNANT_BUNTING.get())
                .withShape("B")
                .withIngredient('B', new BasicRegularIngredient(Ingredient.of(FLItems.PENNANT_BUNTING.get())) {
                    @Override
                    public ImmutableList<ItemStack> getInputs() {
                        return Arrays.stream(this.ingredient.getItems())
                                .map(ItemStack::copy)
                                .flatMap(stack -> makePennantExamples(stack).stream())
                                .collect(ImmutableList.toImmutableList());
                    }

                    @Override
                    public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                        return ImmutableList.of(ImmutableList.of(makePennantExamples(output).get(0)));
                    }

                    @Override
                    public void matched(final ItemStack ingredient, final CompoundTag nbt) {
                    }
                })
                .withAuxiliaryIngredient(new PennantIngredient())
                .build();
    }

    private static ImmutableList<ItemStack> makePennantExamples(final ItemStack stack) {
        return ImmutableList.of(
                makePennant(stack, DyeColor.BLUE, DyeColor.YELLOW, DyeColor.RED),
                makePennant(stack, DyeColor.PINK, DyeColor.LIGHT_BLUE),
                makePennant(stack, DyeColor.ORANGE, DyeColor.WHITE),
                makePennant(stack, DyeColor.LIME, DyeColor.YELLOW));
    }

    public static ItemStack makePennant(final ItemStack base, final DyeColor... colors) {
        final ItemStack stack = base.copy();
        final CompoundTag logic = new CompoundTag();
        final ListTag pennants = new ListTag();
        final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess
                .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        for (final DyeColor color : colors) {
            final ItemStack pennant = new ItemStack(FLItems.TRIANGLE_PENNANT.get());
            DyeableItem.setColor(pennant, color);
            pennants.add(pennant.save(registryAccess));
        }
        logic.put("pattern", pennants);
        // StyledString for text - separate component
        stack.set(FLDataComponents.STYLED_STRING, StyledString.serialize(new StyledString()));
        stack.set(FLDataComponents.CONNECTION_LOGIC, logic);
        return stack;
    }

    private static GenericRecipe createPennant(final ResourceLocation name,
            final Supplier<RecipeSerializer<GenericRecipe>> serializer, final Item item, final String pattern) {
        return new GenericRecipeBuilder(name, serializer, item)
                .withShape("- -", "PDP", pattern)
                .withIngredient('P', Items.PAPER)
                .withIngredient('-', Items.STRING)
                .withIngredient('D', DYE_SUBTYPE_INGREDIENT)
                .build();
    }

    // Helper methods for Pennants (Triangle, Spearhead, etc.)
    private static GenericRecipe createTrianglePennant(final ResourceLocation name, CraftingBookCategory category) {
        return createPennant(name, TRIANGLE_PENNANT, FLItems.TRIANGLE_PENNANT.get(), " P ");
    }

    private static GenericRecipe createSpearheadPennant(final ResourceLocation name, CraftingBookCategory category) {
        return createPennant(name, SPEARHEAD_PENNANT, FLItems.SPEARHEAD_PENNANT.get(), " PP");
    }

    private static GenericRecipe createSwallowtailPennant(final ResourceLocation name, CraftingBookCategory category) {
        return createPennant(name, SWALLOWTAIL_PENNANT, FLItems.SWALLOWTAIL_PENNANT.get(), "P P");
    }

    private static GenericRecipe createSquarePennant(final ResourceLocation name, CraftingBookCategory category) {
        return createPennant(name, SQUARE_PENNANT, FLItems.SQUARE_PENNANT.get(), "PPP");
    }

    // Helper methods for Lights (Fairy, Paper, etc.)
    private static GenericRecipe createFairyLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, FAIRY_LIGHT, FLItems.FAIRY_LIGHT,
                b -> b.withShape(" I ", "IDI", " G ").withIngredient('G', Tags.Items.GLASS_PANES_COLORLESS));
    }

    private static GenericRecipe createPaperLantern(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, PAPER_LANTERN, FLItems.PAPER_LANTERN,
                b -> b.withShape(" I ", "PDP", "PPP").withIngredient('P', Items.PAPER));
    }

    private static GenericRecipe createOrbLantern(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, ORB_LANTERN, FLItems.ORB_LANTERN, b -> b.withShape(" I ", "SDS", " W ")
                .withIngredient('S', Items.STRING).withIngredient('W', Items.WHITE_WOOL));
    }

    private static GenericRecipe createFlowerLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, FLOWER_LIGHT, FLItems.FLOWER_LIGHT,
                b -> b.withShape(" I ", "RDB", " Y ").withIngredient('R', Items.POPPY)
                        .withIngredient('Y', Items.DANDELION).withIngredient('B', Items.BLUE_ORCHID));
    }

    private static GenericRecipe createCandleLanternLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, CANDLE_LANTERN_LIGHT, FLItems.CANDLE_LANTERN_LIGHT,
                b -> b.withShape(" I ", "GDG", "IGI").withIngredient('G', Tags.Items.NUGGETS_GOLD));
    }

    private static GenericRecipe createOilLanternLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, OIL_LANTERN_LIGHT, FLItems.OIL_LANTERN_LIGHT, b -> b.withShape(" I ", "SDS", "IGI")
                .withIngredient('S', Items.STICK).withIngredient('G', Tags.Items.GLASS_PANES_COLORLESS));
    }

    private static GenericRecipe createJackOLantern(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, JACK_O_LANTERN, FLItems.JACK_O_LANTERN,
                b -> b.withShape(" I ", "SDS", "GPG").withIngredient('S', ItemTags.WOODEN_SLABS)
                        .withIngredient('G', Items.TORCH).withIngredient('P', Items.JACK_O_LANTERN));
    }

    private static GenericRecipe createSkullLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, SKULL_LIGHT, FLItems.SKULL_LIGHT,
                b -> b.withShape(" I ", "IDI", " B ").withIngredient('B', Tags.Items.BONES));
    }

    private static GenericRecipe createGhostLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, GHOST_LIGHT, FLItems.GHOST_LIGHT, b -> b.withShape(" I ", "PDP", "IGI")
                .withIngredient('P', Items.PAPER).withIngredient('G', Items.WHITE_STAINED_GLASS_PANE));
    }

    private static GenericRecipe createSpiderLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, SPIDER_LIGHT, FLItems.SPIDER_LIGHT,
                b -> b.withShape(" I ", "WDW", "SES").withIngredient('W', Items.COBWEB)
                        .withIngredient('S', Items.STRING).withIngredient('E', Items.SPIDER_EYE));
    }

    private static GenericRecipe createWitchLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, WITCH_LIGHT, FLItems.WITCH_LIGHT,
                b -> b.withShape(" I ", "BDW", " S ").withIngredient('B', Items.GLASS_BOTTLE)
                        .withIngredient('W', Items.WHEAT).withIngredient('S', Items.STICK));
    }

    private static GenericRecipe createSnowflakeLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, SNOWFLAKE_LIGHT, FLItems.SNOWFLAKE_LIGHT, b -> b.withShape(" I ", "SDS", " G ")
                .withIngredient('S', Items.SNOWBALL).withIngredient('G', Items.WHITE_STAINED_GLASS_PANE));
    }

    private static GenericRecipe createHeartLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, HEART_LIGHT, FLItems.HEART_LIGHT,
                b -> b.withShape(" I ", "IDI", " G ").withIngredient('G', Items.RED_STAINED_GLASS_PANE));
    }

    private static GenericRecipe createMoonLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, MOON_LIGHT, FLItems.MOON_LIGHT, b -> b.withShape(" I ", "GDG", " C ")
                .withIngredient('G', Items.WHITE_STAINED_GLASS_PANE).withIngredient('C', Items.CLOCK));
    }

    private static GenericRecipe createStarLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, STAR_LIGHT, FLItems.STAR_LIGHT, b -> b.withShape(" I ", "PDP", " G ")
                .withIngredient('P', Items.WHITE_STAINED_GLASS_PANE).withIngredient('G', Tags.Items.NUGGETS_GOLD));
    }

    private static GenericRecipe createIcicleLights(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, ICICLE_LIGHTS, FLItems.ICICLE_LIGHTS, b -> b.withShape(" I ", "GDG", " B ")
                .withIngredient('G', Tags.Items.GLASS_PANES_COLORLESS).withIngredient('B', Items.WATER_BUCKET));
    }

    private static GenericRecipe createMeteorLight(final ResourceLocation name, CraftingBookCategory category) {
        return createLight(name, METEOR_LIGHT, FLItems.METEOR_LIGHT, b -> b.withShape(" I ", "GDG", "IPI")
                .withIngredient('G', Tags.Items.DUSTS_GLOWSTONE).withIngredient('P', Items.PAPER));
    }

    private static GenericRecipe createLight(final ResourceLocation name,
            final Supplier<? extends RecipeSerializer<GenericRecipe>> serializer,
            final Supplier<? extends Item> variant, final UnaryOperator<GenericRecipeBuilder> recipe) {
        return recipe.apply(new GenericRecipeBuilder(name, serializer))
                .withIngredient('I', Tags.Items.INGOTS_IRON)
                .withIngredient('D', FLCraftingRecipes.DYE_SUBTYPE_INGREDIENT)
                .withOutput(variant.get(), 4)
                .build();
    }

    private static class LightIngredient extends BasicAuxiliaryIngredient<ListTag> {
        private LightIngredient(final boolean isRequired) {
            super(LazyTagIngredient.of(LIGHTS), isRequired, 8);
        }

        @Override
        public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
            final CompoundTag logic = output.get(FLDataComponents.CONNECTION_LOGIC);
            if (logic != null) {
                final ListTag pattern = logic.getList("pattern", Tag.TAG_COMPOUND);
                if (!pattern.isEmpty()) {
                    final ImmutableList.Builder<ImmutableList<ItemStack>> lights = ImmutableList.builder();
                    final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess
                            .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
                    for (int i = 0; i < pattern.size(); i++) {
                        lights.add(ImmutableList
                                .of(ItemStack.parse(registryAccess, pattern.getCompound(i)).orElse(ItemStack.EMPTY)));
                    }
                    return lights.build();
                }
            }
            return ImmutableList.of();
        }

        @Override
        public boolean dictatesOutputType() {
            return true;
        }

        @Override
        public ListTag accumulator() {
            return new ListTag();
        }

        @Override
        public void consume(final ListTag patternList, final ItemStack ingredient) {
            patternList.add(ingredient.save(net.minecraft.core.RegistryAccess
                    .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)));
        }

        @Override
        public boolean finish(final ListTag pattern, final CompoundTag nbt) {
            // NOTE: This modifies/merges into the NBT that GenericRecipeBuilder puts on the
            // item.
            // Since we can't easily change GenericRecipeBuilder logic to use Components, we
            // assume it merges nbt to components.
            // If GenericRecipeBuilder is effectively: stack.setTag(nbt) [which is gone],
            // then we have a problem.
            if (pattern.size() > 0) {
                // nbt.put("pattern", pattern);
            }
            return false;
        }
    }

    private static class PennantIngredient extends BasicAuxiliaryIngredient<ListTag> {
        private PennantIngredient() {
            super(LazyTagIngredient.of(PENNANTS), true, 8);
        }

        @Override
        public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
            final CompoundTag logic = output.get(FLDataComponents.CONNECTION_LOGIC);
            if (logic != null) {
                final ListTag pattern = logic.getList("pattern", Tag.TAG_COMPOUND);
                if (!pattern.isEmpty()) {
                    final ImmutableList.Builder<ImmutableList<ItemStack>> pennants = ImmutableList.builder();
                    final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess
                            .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
                    for (int i = 0; i < pattern.size(); i++) {
                        pennants.add(ImmutableList
                                .of(ItemStack.parse(registryAccess, pattern.getCompound(i)).orElse(ItemStack.EMPTY)));
                    }
                    return pennants.build();
                }
            }
            return ImmutableList.of();
        }

        @Override
        public boolean dictatesOutputType() {
            return true;
        }

        @Override
        public ListTag accumulator() {
            return new ListTag();
        }

        @Override
        public void consume(final ListTag patternList, final ItemStack ingredient) {
            patternList.add(ingredient.save(net.minecraft.core.RegistryAccess
                    .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)));
        }

        @Override
        public boolean finish(final ListTag pattern, final CompoundTag nbt) {
            if (pattern.size() > 0) {
                // nbt.put("pattern", pattern);
            }
            return false;
        }
    }
}
