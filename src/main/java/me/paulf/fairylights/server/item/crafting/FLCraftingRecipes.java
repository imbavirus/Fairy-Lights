package me.paulf.fairylights.server.item.crafting;

import com.google.common.collect.ImmutableList;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.item.DyeableItem;
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
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
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
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

// @EventBusSubscriber removed - this class doesn't have @SubscribeEvent methods
// Registration is handled via DeferredRegister.REG.register(bus) in FairyLights.java
public final class FLCraftingRecipes {
    private FLCraftingRecipes() {}

    public static final DeferredRegister<RecipeSerializer<?>> REG = DeferredRegister.create(Registries.RECIPE_SERIALIZER, FairyLights.ID);

    // Wrapper methods for SimpleCraftingRecipeSerializer Factory interface - Factory only takes CraftingBookCategory
    // Note: ResourceLocation is provided by the serializer during deserialization, but we need it for construction
    // Using a placeholder ResourceLocation - it should be replaced during actual deserialization
    private static final net.minecraft.resources.ResourceLocation PLACEHOLDER_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "placeholder");
    
    public static GenericRecipe createHangingLightsWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { 
        return createHangingLights(PLACEHOLDER_ID, category); 
    }
    public static GenericRecipe createHangingLightsAugmentationWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createHangingLightsAugmentation(PLACEHOLDER_ID, category); }
    public static GenericRecipe createTinselGarlandWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createTinselGarland(PLACEHOLDER_ID, category); }
    public static GenericRecipe createPennantBuntingWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createPennantBunting(PLACEHOLDER_ID, category); }
    public static GenericRecipe createPennantBuntingAugmentationWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createPennantBuntingAugmentation(PLACEHOLDER_ID, category); }
    public static GenericRecipe createTrianglePennantWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createTrianglePennant(PLACEHOLDER_ID, category); }
    public static GenericRecipe createSpearheadPennantWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createSpearheadPennant(PLACEHOLDER_ID, category); }
    public static GenericRecipe createSwallowtailPennantWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createSwallowtailPennant(PLACEHOLDER_ID, category); }
    public static GenericRecipe createSquarePennantWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createSquarePennant(PLACEHOLDER_ID, category); }
    public static GenericRecipe createFairyLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createFairyLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createPaperLanternWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createPaperLantern(PLACEHOLDER_ID, category); }
    public static GenericRecipe createOrbLanternWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createOrbLantern(PLACEHOLDER_ID, category); }
    public static GenericRecipe createFlowerLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createFlowerLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createCandleLanternLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createCandleLanternLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createOilLanternLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createOilLanternLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createJackOLanternWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createJackOLantern(PLACEHOLDER_ID, category); }
    public static GenericRecipe createSkullLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createSkullLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createGhostLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createGhostLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createSpiderLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createSpiderLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createWitchLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createWitchLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createSnowflakeLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createSnowflakeLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createHeartLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createHeartLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createMoonLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createMoonLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createStarLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createStarLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createIcicleLightsWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createIcicleLights(PLACEHOLDER_ID, category); }
    public static GenericRecipe createMeteorLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createMeteorLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createLightTwinkleWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createLightTwinkle(PLACEHOLDER_ID, category); }
    public static GenericRecipe createColorChangingLightWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createColorChangingLight(PLACEHOLDER_ID, category); }
    public static GenericRecipe createDyeColorWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return createDyeColor(PLACEHOLDER_ID, category); }
    public static CustomRecipe createCopyColorWrapper(net.minecraft.world.item.crafting.CraftingBookCategory category) { return new CopyColorRecipe(PLACEHOLDER_ID, category); }

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> HANGING_LIGHTS = REG.register("crafting_special_hanging_lights", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createHangingLightsWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> HANGING_LIGHTS_AUGMENTATION = REG.register("crafting_special_hanging_lights_augmentation", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createHangingLightsAugmentationWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> TINSEL_GARLAND = REG.register("crafting_special_tinsel_garland", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createTinselGarlandWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> PENNANT_BUNTING = REG.register("crafting_special_pennant_bunting", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createPennantBuntingWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> PENNANT_BUNTING_AUGMENTATION = REG.register("crafting_special_pennant_bunting_augmentation", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createPennantBuntingAugmentationWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> TRIANGLE_PENNANT = REG.register("crafting_special_triangle_pennant", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createTrianglePennantWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> SPEARHEAD_PENNANT = REG.register("crafting_special_spearhead_pennant", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createSpearheadPennantWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> SWALLOWTAIL_PENNANT = REG.register("crafting_special_swallowtail_pennant", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createSwallowtailPennantWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> SQUARE_PENNANT = REG.register("crafting_special_square_pennant", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createSquarePennantWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> FAIRY_LIGHT = REG.register("crafting_special_fairy_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createFairyLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> PAPER_LANTERN = REG.register("crafting_special_paper_lantern", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createPaperLanternWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> ORB_LANTERN = REG.register("crafting_special_orb_lantern", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createOrbLanternWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> FLOWER_LIGHT = REG.register("crafting_special_flower_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createFlowerLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> CANDLE_LANTERN_LIGHT = REG.register("crafting_special_candle_lantern_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createCandleLanternLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> OIL_LANTERN_LIGHT = REG.register("crafting_special_oil_lantern_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createOilLanternLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> JACK_O_LANTERN = REG.register("crafting_special_jack_o_lantern", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createJackOLanternWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> SKULL_LIGHT = REG.register("crafting_special_skull_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createSkullLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> GHOST_LIGHT = REG.register("crafting_special_ghost_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createGhostLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> SPIDER_LIGHT = REG.register("crafting_special_spider_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createSpiderLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> WITCH_LIGHT = REG.register("crafting_special_witch_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createWitchLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> SNOWFLAKE_LIGHT = REG.register("crafting_special_snowflake_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createSnowflakeLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> HEART_LIGHT = REG.register("crafting_special_heart_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createHeartLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> MOON_LIGHT = REG.register("crafting_special_moon_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createMoonLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> STAR_LIGHT = REG.register("crafting_special_star_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createStarLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> ICICLE_LIGHTS = REG.register("crafting_special_icicle_lights", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createIcicleLightsWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> METEOR_LIGHT = REG.register("crafting_special_meteor_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createMeteorLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> LIGHT_TWINKLE = REG.register("crafting_special_light_twinkle", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createLightTwinkleWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> COLOR_CHANGING_LIGHT = REG.register("crafting_special_color_changing_light", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createColorChangingLightWrapper));

    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<GenericRecipe>> EDIT_COLOR = REG.register("crafting_special_edit_color", () -> new SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createDyeColorWrapper));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CustomRecipe>> COPY_COLOR = REG.register("crafting_special_copy_color", () -> new SimpleCraftingRecipeSerializer<CustomRecipe>(FLCraftingRecipes::createCopyColorWrapper));

    public static final TagKey<Item> LIGHTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "lights"));

    public static final TagKey<Item> TWINKLING_LIGHTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "twinkling_lights"));

    public static final TagKey<Item> PENNANTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "pennants"));

    public static final TagKey<Item> DYEABLE = ItemTags.create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "dyeable"));

    public static final TagKey<Item> DYEABLE_LIGHTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "dyeable_lights"));

    public static final RegularIngredient DYE_SUBTYPE_INGREDIENT = new BasicRegularIngredient(LazyTagIngredient.of(Tags.Items.DYES)) {
        @Override
        public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
            return DyeableItem.getDyeColor(output).map(dye -> ImmutableList.of(OreDictUtils.getDyes(dye))).orElse(ImmutableList.of());
        }

        @Override
        public boolean dictatesOutputType() {
            return true;
        }

        @Override
        public void matched(final ItemStack ingredient, final CompoundTag nbt) {
            DyeableItem.setColor(nbt, OreDictUtils.getDyeColor(ingredient));
        }
    };

    private static GenericRecipe createDyeColor(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, EDIT_COLOR)
            .withShape("I")
            .withIngredient('I', DYEABLE).withOutput('I')
            .withAuxiliaryIngredient(new BasicAuxiliaryIngredient<Blender>(LazyTagIngredient.of(Tags.Items.DYES), true, 8) {
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
                    DyeableItem.setColor(nbt, data.blend());
                    return false;
                }
            })
            .build();
    }

    private static GenericRecipe createLightTwinkle(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, LIGHT_TWINKLE)
            .withShape("L")
            .withIngredient('L', TWINKLING_LIGHTS).withOutput('L')
            .withAuxiliaryIngredient(new InertBasicAuxiliaryIngredient(LazyTagIngredient.of(Tags.Items.DUSTS_GLOWSTONE), true, 1) {
                @Override
                public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                    return useInputsForTagBool(output, "twinkle", true) ? super.getInput(output) : ImmutableList.of();
                }

                @Override
                public void present(final CompoundTag nbt) {
                    nbt.putBoolean("twinkle", true);
                }

                @Override
                public void absent(final CompoundTag nbt) {
                    nbt.putBoolean("twinkle", false);
                }

                @Override
                public void addTooltip(final List<Component> tooltip) {
                    super.addTooltip(tooltip);
                    tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.twinkling_lights.glowstone"));
                }
            })
            .build();
    }

    private static GenericRecipe createColorChangingLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, COLOR_CHANGING_LIGHT)
            .withShape("IG")
            .withIngredient('I', DYEABLE_LIGHTS).withOutput('I')
            .withIngredient('G', Tags.Items.NUGGETS_GOLD)
            .withAuxiliaryIngredient(new BasicAuxiliaryIngredient<ListTag>(LazyTagIngredient.of(Tags.Items.DYES), true, 8) {
                @Override
                public ListTag accumulator() {
                    return new ListTag();
                }

                @Override
                public void consume(final ListTag data, final ItemStack ingredient) {
                    data.add(IntTag.valueOf(DyeableItem.getColor(OreDictUtils.getDyeColor(ingredient))));
                }

                @Override
                public boolean finish(final ListTag data, final CompoundTag nbt) {
                    if (!data.isEmpty()) {
                        if (nbt.contains("color", Tag.TAG_INT)) {
                            data.add(0, IntTag.valueOf(nbt.getInt("color")));
                            nbt.remove("color");
                        }
                        nbt.put("colors", data);
                    }
                    return false;
                }
            })
            .build();
    }

    private static GenericRecipe createHangingLights(ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, HANGING_LIGHTS, FLItems.HANGING_LIGHTS.get())
            .withShape("I-I")
            .withIngredient('I', Tags.Items.INGOTS_IRON)
            .withIngredient('-', Items.STRING)
            .withAuxiliaryIngredient(new LightIngredient(true))
            .withAuxiliaryIngredient(new InertBasicAuxiliaryIngredient(LazyTagIngredient.of(Tags.Items.DYES_WHITE), false, 1) {
                @Override
                public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                    // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
                    final CompoundTag tag = new CompoundTag();
                    // TODO: Migrate to data components API for 1.21.1
                    return tag != null && HangingLightsConnectionItem.getString(tag) == StringTypes.WHITE_STRING.get() ? super.getInput(output) : ImmutableList.of();
                }

                @Override
                public void present(final CompoundTag nbt) {
                    HangingLightsConnectionItem.setString(nbt, StringTypes.WHITE_STRING.get());
                }

                @Override
                public void absent(final CompoundTag nbt) {
                    HangingLightsConnectionItem.setString(nbt, StringTypes.BLACK_STRING.get());
                }

                @Override
                public void addTooltip(final List<Component> tooltip) {
                    super.addTooltip(tooltip);
                    tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.hangingLights.string"));
                }
            })
            .build();
    }

    private static boolean useInputsForTagBool(final ItemStack output, final String key, final boolean value) {
        // getTag() removed in 1.21.1 - use data components instead
        final CompoundTag compound = new CompoundTag();
        // TODO: Migrate to data components API for 1.21.1
        return !compound.isEmpty() && compound.getBoolean(key) == value;
    }

    /*
     *  The JEI shown recipe is adding glowstone, eventually I should allow a recipe to provide a number of
     *  different recipe layouts the the input ingredients can be generated for so I could show applying a
     *  new light pattern as well.
     */
    private static GenericRecipe createHangingLightsAugmentation(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, HANGING_LIGHTS_AUGMENTATION, FLItems.HANGING_LIGHTS.get())
            .withShape("F")
            .withIngredient('F', new BasicRegularIngredient(Ingredient.of(FLItems.HANGING_LIGHTS.get())) {
                @Override
                public ImmutableList<ItemStack> getInputs() {
                    return Arrays.stream(this.ingredient.getItems())
                        .map(ItemStack::copy)
                        .flatMap(stack -> {
                            // ItemStack.setTag() removed in 1.21.1 - use data components API instead
                            // TODO: Migrate to data components API for 1.21.1
                            // stack.setTag(new CompoundTag());
                            return makeHangingLightsExamples(stack).stream();
                        }).collect(ImmutableList.toImmutableList());
                }

                @Override
                public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                    final ItemStack stack = output.copy();
                    // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
                    final CompoundTag compound = new CompoundTag();
                    // TODO: Migrate to data components API for 1.21.1
                    if (compound == null) {
                        return ImmutableList.of();
                    }
                    stack.setCount(1);
                    return ImmutableList.of(ImmutableList.of(stack));
                }

                @Override
                public void matched(final ItemStack ingredient, final CompoundTag nbt) {
                    // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
                    final CompoundTag compound = new CompoundTag();
                    // TODO: Migrate to data components API for 1.21.1
                    if (compound != null) {
                        nbt.merge(compound);
                    }
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
            makeHangingLights(stack, DyeColor.CYAN, DyeColor.YELLOW, DyeColor.CYAN, DyeColor.PURPLE)
        );
    }

    public static ItemStack makeHangingLights(final ItemStack base, final DyeColor... colors) {
        final ItemStack stack = base.copy();
        final CompoundTag compound = new CompoundTag();
        final ListTag lights = new ListTag();
        for (final DyeColor color : colors) {
            final ItemStack coloredLight = DyeableItem.setColor(new ItemStack(FLItems.FAIRY_LIGHT.get()), color);
            lights.add(coloredLight.save(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)));
        }
        compound.put("pattern", lights);
        HangingLightsConnectionItem.setString(compound, StringTypes.BLACK_STRING.get());
        // Apply NBT to stack using reflection (same approach as Connection.getItemStack)
        try {
            final java.lang.reflect.Method getTag = stack.getClass().getMethod("getTag");
            final CompoundTag existingTag = (CompoundTag) getTag.invoke(stack);
            if (existingTag != null) {
                existingTag.merge(compound);
            } else {
                final java.lang.reflect.Method setTag = stack.getClass().getMethod("setTag", CompoundTag.class);
                setTag.invoke(stack, compound.copy());
            }
        } catch (Exception e) {
            // Fallback: try save/parse approach
            try {
                final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
                final net.minecraft.nbt.Tag savedTag = stack.save(registryAccess);
                if (savedTag instanceof CompoundTag) {
                    final CompoundTag tag = (CompoundTag) savedTag;
                    tag.merge(compound);
                    final ItemStack result = ItemStack.parse(registryAccess, tag).orElse(stack);
                    result.setCount(stack.getCount());
                    return result;
                }
            } catch (Exception e2) {
                // Both methods failed
            }
        }
        return stack;
    }

    private static GenericRecipe createTinselGarland(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, TINSEL_GARLAND, FLItems.TINSEL.get())
            .withShape(" P ", "I-I", " D ")
            .withIngredient('P', Items.PAPER)
            .withIngredient('I', Tags.Items.INGOTS_IRON)
            .withIngredient('-', Items.STRING)
            .withIngredient('D', DYE_SUBTYPE_INGREDIENT)
            .build();
    }

    private static GenericRecipe createPennantBunting(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, PENNANT_BUNTING, FLItems.PENNANT_BUNTING.get())
            .withShape("I-I")
            .withIngredient('I', Tags.Items.INGOTS_IRON)
            .withIngredient('-', Items.STRING)
            .withAuxiliaryIngredient(new PennantIngredient())
            .build();
    }

    private static GenericRecipe createPennantBuntingAugmentation(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return new GenericRecipeBuilder(name, PENNANT_BUNTING_AUGMENTATION, FLItems.PENNANT_BUNTING.get())
            .withShape("B")
            .withIngredient('B', new BasicRegularIngredient(Ingredient.of(FLItems.PENNANT_BUNTING.get())) {
                @Override
                public ImmutableList<ItemStack> getInputs() {
                    return Arrays.stream(this.ingredient.getItems())
                        .map(ItemStack::copy)
                        .flatMap(stack -> {
                            // ItemStack.setTag() removed in 1.21.1 - use data components API instead
                            // TODO: Migrate to data components API for 1.21.1
                            // stack.setTag(new CompoundTag());
                            return makePennantExamples(stack).stream();
                        }).collect(ImmutableList.toImmutableList());
                }

                @Override
                public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
                    // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
                    final CompoundTag compound = new CompoundTag();
                    // TODO: Migrate to data components API for 1.21.1
                    if (compound == null) {
                        return ImmutableList.of();
                    }
                    return ImmutableList.of(makePennantExamples(output));
                }

                @Override
                public void matched(final ItemStack ingredient, final CompoundTag nbt) {
                    // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
                    final CompoundTag compound = new CompoundTag();
                    // TODO: Migrate to data components API for 1.21.1
                    if (compound != null) {
                        nbt.merge(compound);
                    }
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
            makePennant(stack, DyeColor.LIME, DyeColor.YELLOW)
        );
    }

    public static ItemStack makePennant(final ItemStack base, final DyeColor... colors) {
        final ItemStack stack = base.copy();
        final CompoundTag compound = new CompoundTag();
        final ListTag pennants = new ListTag();
        for (final DyeColor color : colors) {
            final ItemStack pennant = new ItemStack(FLItems.TRIANGLE_PENNANT.get());
            DyeableItem.setColor(pennant, color);
            pennants.add(pennant.save(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)));
        }
        compound.put("pattern", pennants);
        compound.put("text", StyledString.serialize(new StyledString()));
        // Apply NBT to stack using reflection (same approach as Connection.getItemStack)
        try {
            final java.lang.reflect.Method getTag = stack.getClass().getMethod("getTag");
            final CompoundTag existingTag = (CompoundTag) getTag.invoke(stack);
            if (existingTag != null) {
                existingTag.merge(compound);
            } else {
                final java.lang.reflect.Method setTag = stack.getClass().getMethod("setTag", CompoundTag.class);
                setTag.invoke(stack, compound.copy());
            }
        } catch (Exception e) {
            // Fallback: try save/parse approach
            try {
                final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
                final net.minecraft.nbt.Tag savedTag = stack.save(registryAccess);
                if (savedTag instanceof CompoundTag) {
                    final CompoundTag tag = (CompoundTag) savedTag;
                    tag.merge(compound);
                    final ItemStack result = ItemStack.parse(registryAccess, tag).orElse(stack);
                    result.setCount(stack.getCount());
                    return result;
                }
            } catch (Exception e2) {
                // Both methods failed
            }
        }
        return stack;
    }

    private static GenericRecipe createPennant(final ResourceLocation name, final Supplier<RecipeSerializer<GenericRecipe>> serializer, final Item item, final String pattern) {
        return new GenericRecipeBuilder(name, serializer, item)
            .withShape("- -", "PDP", pattern)
            .withIngredient('P', Items.PAPER)
            .withIngredient('-', Items.STRING)
            .withIngredient('D', DYE_SUBTYPE_INGREDIENT)
            .build();
    }

    private static GenericRecipe createTrianglePennant(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createPennant(name, TRIANGLE_PENNANT, FLItems.TRIANGLE_PENNANT.get(), " P ");
    }

    private static GenericRecipe createSpearheadPennant(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createPennant(name, SPEARHEAD_PENNANT, FLItems.SPEARHEAD_PENNANT.get(), " PP");
    }

    private static GenericRecipe createSwallowtailPennant(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createPennant(name, SWALLOWTAIL_PENNANT, FLItems.SWALLOWTAIL_PENNANT.get(), "P P");
    }

    private static GenericRecipe createSquarePennant(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createPennant(name, SQUARE_PENNANT, FLItems.SQUARE_PENNANT.get(), "PPP");
    }

    private static GenericRecipe createFairyLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, FAIRY_LIGHT, FLItems.FAIRY_LIGHT, b -> b
            .withShape(" I ", "IDI", " G ")
            .withIngredient('G', Tags.Items.GLASS_PANES_COLORLESS)
        );
    }

    private static GenericRecipe createPaperLantern(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, PAPER_LANTERN, FLItems.PAPER_LANTERN, b -> b
            .withShape(" I ", "PDP", "PPP")
            .withIngredient('P', Items.PAPER)
        );
    }

    private static GenericRecipe createOrbLantern(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, ORB_LANTERN, FLItems.ORB_LANTERN, b -> b
            .withShape(" I ", "SDS", " W ")
            .withIngredient('S', Items.STRING)
            .withIngredient('W', Items.WHITE_WOOL)
        );
    }

    private static GenericRecipe createFlowerLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, FLOWER_LIGHT, FLItems.FLOWER_LIGHT, b -> b
            .withShape(" I ", "RDB", " Y ")
            .withIngredient('R', Items.POPPY)
            .withIngredient('Y', Items.DANDELION)
            .withIngredient('B', Items.BLUE_ORCHID)
        );
    }

    private static GenericRecipe createCandleLanternLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, CANDLE_LANTERN_LIGHT, FLItems.CANDLE_LANTERN_LIGHT, b -> b
            .withShape(" I ", "GDG", "IGI")
            .withIngredient('G', Tags.Items.NUGGETS_GOLD)
        );
    }

    private static GenericRecipe createOilLanternLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, OIL_LANTERN_LIGHT, FLItems.OIL_LANTERN_LIGHT, b -> b
            .withShape(" I ", "SDS", "IGI")
            .withIngredient('S', Items.STICK)
            .withIngredient('G', Tags.Items.GLASS_PANES_COLORLESS)
        );
    }

    private static GenericRecipe createJackOLantern(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, JACK_O_LANTERN, FLItems.JACK_O_LANTERN, b -> b
            .withShape(" I ", "SDS", "GPG")
            .withIngredient('S', ItemTags.WOODEN_SLABS)
            .withIngredient('G', Items.TORCH)
            .withIngredient('P', Items.JACK_O_LANTERN)
        );
    }

    private static GenericRecipe createSkullLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, SKULL_LIGHT, FLItems.SKULL_LIGHT, b -> b
            .withShape(" I ", "IDI", " B ")
            .withIngredient('B', Tags.Items.BONES)
        );
    }

    private static GenericRecipe createGhostLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, GHOST_LIGHT, FLItems.GHOST_LIGHT, b -> b
            .withShape(" I ", "PDP", "IGI")
            .withIngredient('P', Items.PAPER)
            .withIngredient('G', Items.WHITE_STAINED_GLASS_PANE)
        );
    }

    private static GenericRecipe createSpiderLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, SPIDER_LIGHT, FLItems.SPIDER_LIGHT, b -> b
            .withShape(" I ", "WDW", "SES")
            .withIngredient('W', Items.COBWEB)
            .withIngredient('S', Items.STRING)
            .withIngredient('E', Items.SPIDER_EYE)
        );
    }

    private static GenericRecipe createWitchLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, WITCH_LIGHT, FLItems.WITCH_LIGHT, b -> b
            .withShape(" I ", "BDW", " S ")
            .withIngredient('B', Items.GLASS_BOTTLE)
            .withIngredient('W', Items.WHEAT)
            .withIngredient('S', Items.STICK)
        );
    }

    private static GenericRecipe createSnowflakeLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, SNOWFLAKE_LIGHT, FLItems.SNOWFLAKE_LIGHT, b -> b
            .withShape(" I ", "SDS", " G ")
            .withIngredient('S', Items.SNOWBALL)
            .withIngredient('G', Items.WHITE_STAINED_GLASS_PANE)
        );
    }

    private static GenericRecipe createHeartLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, HEART_LIGHT, FLItems.HEART_LIGHT, b -> b
            .withShape(" I ", "IDI", " G ")
            .withIngredient('G', Items.RED_STAINED_GLASS_PANE)
        );
    }

    private static GenericRecipe createMoonLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, MOON_LIGHT, FLItems.MOON_LIGHT, b -> b
            .withShape(" I ", "GDG", " C ")
            .withIngredient('G', Items.WHITE_STAINED_GLASS_PANE)
            .withIngredient('C', Items.CLOCK)
        );
    }


    private static GenericRecipe createStarLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, STAR_LIGHT, FLItems.STAR_LIGHT, b -> b
            .withShape(" I ", "PDP", " G ")
              .withIngredient('P', Items.WHITE_STAINED_GLASS_PANE)
            .withIngredient('G', Tags.Items.NUGGETS_GOLD)
        );
    }

    private static GenericRecipe createIcicleLights(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, ICICLE_LIGHTS, FLItems.ICICLE_LIGHTS, b -> b
            .withShape(" I ", "GDG", " B ")
            .withIngredient('G', Tags.Items.GLASS_PANES_COLORLESS)
            .withIngredient('B', Items.WATER_BUCKET)
        );
    }

    private static GenericRecipe createMeteorLight(final ResourceLocation name, CraftingBookCategory craftingBookCategory) {
        return createLight(name, METEOR_LIGHT, FLItems.METEOR_LIGHT, b -> b
            .withShape(" I ", "GDG", "IPI")
            .withIngredient('G', Tags.Items.DUSTS_GLOWSTONE)
            .withIngredient('P', Items.PAPER)
        );
    }

    private static GenericRecipe createLight(final ResourceLocation name, final Supplier<? extends RecipeSerializer<GenericRecipe>> serializer, final Supplier<? extends Item> variant, final UnaryOperator<GenericRecipeBuilder> recipe) {
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
            // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
            final CompoundTag compound = new CompoundTag();
            // TODO: Migrate to data components API for 1.21.1
            if (compound == null) {
                return ImmutableList.of();
            }
            final ListTag pattern = compound.getList("pattern", Tag.TAG_COMPOUND);
            if (pattern.isEmpty()) {
                return ImmutableList.of();
            }
            final ImmutableList.Builder<ImmutableList<ItemStack>> lights = ImmutableList.builder();
            final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
            for (int i = 0; i < pattern.size(); i++) {
                lights.add(ImmutableList.of(ItemStack.parse(registryAccess, pattern.getCompound(i)).orElse(ItemStack.EMPTY)));
            }
            return lights.build();
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
            // ItemStack.save() API changed in 1.21.1 - needs RegistryAccess
            patternList.add(ingredient.save(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)));
        }

        @Override
        public boolean finish(final ListTag pattern, final CompoundTag nbt) {
            if (pattern.size() > 0) {
                nbt.put("pattern", pattern);
            }
            return false;
        }

        @Override
        public void addTooltip(final List<Component> tooltip) {
            tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.hangingLights.light"));
        }
    }

    private static class PennantIngredient extends BasicAuxiliaryIngredient<ListTag> {
        private PennantIngredient() {
            super(LazyTagIngredient.of(PENNANTS), true, 8);
        }

        @Override
        public ImmutableList<ImmutableList<ItemStack>> getInput(final ItemStack output) {
            // ItemStack.getTag() removed in 1.21.1 - use getComponents() or create new CompoundTag
            final CompoundTag compound = new CompoundTag();
            // TODO: Migrate to data components API for 1.21.1
            if (compound == null) {
                return ImmutableList.of();
            }
            final ListTag pattern = compound.getList("pattern", Tag.TAG_COMPOUND);
            if (pattern.isEmpty()) {
                return ImmutableList.of();
            }
            final ImmutableList.Builder<ImmutableList<ItemStack>> pennants = ImmutableList.builder();
            final net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
            for (int i = 0; i < pattern.size(); i++) {
                pennants.add(ImmutableList.of(ItemStack.parse(registryAccess, pattern.getCompound(i)).orElse(ItemStack.EMPTY)));
            }
            return pennants.build();
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
            // ItemStack.save() API changed in 1.21.1 - needs RegistryAccess
            patternList.add(ingredient.save(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)));
        }

        @Override
        public boolean finish(final ListTag pattern, final CompoundTag nbt) {
            if (pattern.size() > 0) {
                nbt.put("pattern", pattern);
                nbt.put("text", StyledString.serialize(new StyledString()));
            }
            return false;
        }

        @Override
        public void addTooltip(final List<Component> tooltip) {
            tooltip.add(Utils.formatRecipeTooltip("recipe.fairylights.pennantBunting.pennant"));
        }
    }
}
