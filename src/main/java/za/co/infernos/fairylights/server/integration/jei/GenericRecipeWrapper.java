package za.co.infernos.fairylights.server.integration.jei;

import com.google.common.collect.ImmutableList;
import za.co.infernos.fairylights.util.FLMth;
import za.co.infernos.fairylights.util.crafting.GenericRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import za.co.infernos.fairylights.util.crafting.ingredient.AuxiliaryIngredient;
import za.co.infernos.fairylights.util.crafting.ingredient.GenericIngredient;
import za.co.infernos.fairylights.util.crafting.ingredient.RegularIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class GenericRecipeWrapper implements ICraftingCategoryExtension<GenericRecipe> {
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();
    private GenericRecipe recipe;

    private List<List<ItemStack>> allInputs;

    // Only minimal stacks, ingredients that support multiple will only have first taken unless dictatesOutputType
    private List<List<ItemStack>> minimalInputStacks;

    private List<ItemStack> outputs;

    private GenericIngredient<?, ?>[] ingredientMatrix;

    private int subtypeIndex;

    public GenericRecipeWrapper() {
    }

    public GenericRecipeWrapper(final GenericRecipe recipe) {
        // this.setRecipe(recipe, null, null, null);
        this.recipe = recipe;
        // Re-initialize state logic here if needed, or deprecate this constructor
    }


    private void forOutputMatches(final BiConsumer<ItemStack, ItemStack> outputConsumer) {
        if (this.subtypeIndex == -1) {
            final List<ItemStack> inputStacks = new ArrayList<>(this.minimalInputStacks.size());
            for (final List<ItemStack> stacks : this.minimalInputStacks) {
                inputStacks.add(stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0));
            }
            CraftingInput input = CraftingInput.of(this.getWidth(), this.getHeight(), inputStacks);
            final net.minecraft.core.HolderLookup.Provider registries = registryAccess();
            if (this.recipe.matches(input, null)) {
                outputConsumer.accept(ItemStack.EMPTY, this.recipe.assemble(input, registries));
            }
        } else {
            final List<ItemStack> dictators = this.minimalInputStacks.get(this.subtypeIndex);
            for (final ItemStack subtype : dictators) {
                final List<ItemStack> inputStacks = new ArrayList<>(this.minimalInputStacks.size());
                for (int i = 0; i < this.minimalInputStacks.size(); i++) {
                    if (i == this.subtypeIndex) {
                        inputStacks.add(subtype);
                    } else {
                        final List<ItemStack> stacks = this.minimalInputStacks.get(i);
                        inputStacks.add(stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0));
                    }
                }
                CraftingInput input = CraftingInput.of(this.getWidth(), this.getHeight(), inputStacks);
                final net.minecraft.core.HolderLookup.Provider registries = registryAccess();
                if (this.recipe.matches(input, null)) {
                    outputConsumer.accept(subtype, this.recipe.assemble(input, registries));
                }
            }
        }
    }

    private static net.minecraft.core.HolderLookup.Provider registryAccess() {
        final net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level != null) {
            return mc.level.registryAccess();
        }
        return net.minecraft.core.RegistryAccess
                .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public int getWidth(final RecipeHolder<GenericRecipe> recipeHolder) {
        return 3;
    }

    @Override
    public int getHeight(final RecipeHolder<GenericRecipe> recipeHolder) {
        return 3;
    }

    public Input getInputsForOutput(final ItemStack output) {
        final RegularIngredient[] ingredients = this.recipe.getGenericIngredients();
        final List<List<ItemStack>> inputs = new ArrayList<>(9);
        final GenericIngredient<?, ?>[] ingredientMat = new GenericIngredient<?, ?>[9];
        final AuxiliaryIngredient<?>[] aux = this.recipe.getAuxiliaryIngredients();
        
        try {
            for (int i = 0, auxIngIdx = 0, auxIdx = 0; i < 9; i++) {
                final int x = i % 3;
                final int y = i / 3;
                final ImmutableList<ImmutableList<ItemStack>> ingInputs;
                GenericIngredient<?, ?> ingredient = null;
                if (x < this.recipe.getWidth() && y < this.recipe.getHeight()) {
                    ingredient = ingredients[x + y * this.recipe.getWidth()];
                    ingInputs = ingredient.getInput(output);
                   
                    if (ingInputs == null || ingInputs.isEmpty()) {
                         // Only log if it's supposed to be a core ingredient but failed to produce input
                         if (ingredient != null && !(ingredient instanceof za.co.infernos.fairylights.util.crafting.ingredient.EmptyRegularIngredient)) {
                             LOGGER.warn("GenericRecipeWrapper: Failed to get inputs for ingredient at index " + i + " for " + BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.recipe.getSerializer()));
                         }
                    }

                } else {
                    ingInputs = null;
                }
                if (ingInputs == null || ingInputs.isEmpty()) {
                    boolean isEmpty = true;
                    if (auxIngIdx < aux.length) {
                        ImmutableList<ImmutableList<ItemStack>> auxInputs = null;
                        AuxiliaryIngredient<?> ingredientAux = null;
                        for (; auxIngIdx < aux.length; auxIngIdx++) {
                            ingredientAux = aux[auxIngIdx];
                            auxInputs = ingredientAux.getInput(output);
                            if (auxInputs.size() > 0) {
                                break;
                            }
                        }
                        if (auxInputs.size() > 0) {
                            inputs.add(auxInputs.get(auxIdx++));
                            ingredientMat[i] = ingredientAux;
                            if (auxIdx == auxInputs.size()) {
                                auxIdx = 0;
                                auxIngIdx++;
                            }
                            isEmpty = false;
                        }
                    }
                    if (isEmpty) {
                        inputs.add(Collections.emptyList());
                    }
                } else {
                    inputs.add(ingInputs.get(0));
                    ingredientMat[i] = ingredient;
                }
            }
        } catch (Exception e) {
             LOGGER.error("GenericRecipeWrapper: Exception deriving inputs for " + BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.recipe.getSerializer()), e);
             return null;
        }
        // If a required aux was omitted (e.g. glowstone when the focused light is not twinkling),
        // bail out so setRecipe can show the full default layout instead of an empty output.
        for (final AuxiliaryIngredient<?> ingredientAux : aux) {
            if (ingredientAux.isRequired()) {
                boolean present = false;
                for (final GenericIngredient<?, ?> placed : ingredientMat) {
                    if (placed == ingredientAux) {
                        present = true;
                        break;
                    }
                }
                if (!present) {
                    return null;
                }
            }
        }
        return new Input(inputs, ingredientMat);
    }

    @Nullable
    private Input getInputsForIngredient(final ItemStack ingredient) {
        for (int i = 0; i < this.allInputs.size(); i++) {
            final List<ItemStack> options = this.allInputs.get(i);
            ItemStack matched = null;
            for (final ItemStack o : options) {
                if (ingredient.getItem() == o.getItem()) {
                    matched = ingredient.copy();
                    matched.setCount(1);
                    break;
                }
            }
            if (matched == null) {
                continue;
            }

            final List<ItemStack> inputStacks = new ArrayList<>(this.minimalInputStacks.size());
            for (int n = 0; n < this.minimalInputStacks.size(); n++) {
                final List<ItemStack> stacks = this.minimalInputStacks.get(n);
                inputStacks.add(i == n ? matched : stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0));
            }
            CraftingInput input = CraftingInput.of(this.getWidth(), this.getHeight(), inputStacks);

            if (this.recipe.matches(input, null)) {
                final List<List<ItemStack>> inputs = new ArrayList<>(this.allInputs.size());
                for (int n = 0; n < this.allInputs.size(); n++) {
                    final List<ItemStack> stacks = this.allInputs.get(n);
                    inputs.add(i == n ? Collections.singletonList(matched) : stacks);
                }
                return new Input(inputs, this.ingredientMatrix);
            }
        }
        return null;
    }

    public List<ItemStack> getOutput(final List<List<ItemStack>> inputs) {
        int size = 1;
        for (final List<ItemStack> stack : inputs) {
            if (stack.size() > 0) {
                size = FLMth.lcm(stack.size(), size);
            }
        }
        final List<ItemStack> outputs = new ArrayList<>(size);
        for (int n = 0; n < size; n++) {
            final List<ItemStack> inputStacks = new ArrayList<>(inputs.size());
            for (int i = 0; i < inputs.size(); i++) {
                final List<ItemStack> stacks = inputs.get(i);
                inputStacks.add(stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(n % stacks.size()));
            }
            CraftingInput input = CraftingInput.of(this.getWidth(), this.getHeight(), inputStacks);
            final net.minecraft.core.HolderLookup.Provider registries = registryAccess();

            if (this.recipe.matches(input, null)) {
                outputs.add(this.recipe.assemble(input, registries));
            }
        }
        return outputs;
    }

    @Override
    public void setRecipe(RecipeHolder<GenericRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        GenericRecipe recipe = recipeHolder.value();
        this.recipe = recipe;
        final List<List<ItemStack>> allInputs = new ArrayList<>();
        final List<List<ItemStack>> minimalInputStacks = new ArrayList<>();
        final RegularIngredient[] ingredients = recipe.getGenericIngredients();
        final AuxiliaryIngredient<?>[] aux = recipe.getAuxiliaryIngredients();
        this.ingredientMatrix = new GenericIngredient<?, ?>[9];
        int subtypeIndex = -1;
        for (int i = 0, auxIdx = 0; i < 9; i++) {
            final int x = i % 3;
            final int y = i / 3;
            boolean isEmpty = true;
            if (x < recipe.getWidth() && y < recipe.getHeight()) {
                final RegularIngredient ingredient = ingredients[x + y * recipe.getWidth()];
                final ImmutableList<ItemStack> ingInputs = ingredient.getInputs();
                if (ingInputs.size() > 0) {
                    if (ingredient.dictatesOutputType()) {
                        minimalInputStacks.add(ingInputs);
                        subtypeIndex = i;
                    } else {
                        minimalInputStacks.add(ImmutableList.of(ingInputs.get(0)));
                    }
                    this.ingredientMatrix[i] = ingredient;
                    allInputs.add(ingInputs);
                    isEmpty = false;
                } else if (!(ingredient instanceof za.co.infernos.fairylights.util.crafting.ingredient.EmptyRegularIngredient)) {
                     LOGGER.warn("GenericRecipeWrapper: Ingredient at " + i + " returned empty inputs! " + ingredient);
                }
            }
            if (isEmpty) {
                AuxiliaryIngredient<?> ingredient = null;
                ImmutableList<ItemStack> stacks = null;
                boolean dictator = false;
                for (; auxIdx < aux.length; ) {
                    ingredient = aux[auxIdx++];
                    final ImmutableList<ItemStack> a = ingredient.getInputs();
                    if (a.size() > 0) {
                        stacks = a;
                        if (ingredient.dictatesOutputType()) {
                            subtypeIndex = i;
                            dictator = true;
                        }
                        break;
                    }
                }
                if (stacks == null) {
                    stacks = ImmutableList.of();
                    ingredient = null;
                }
                minimalInputStacks.add(stacks.isEmpty() || dictator ? stacks : ImmutableList.of(stacks.get(0)));
                this.ingredientMatrix[i] = ingredient;
                allInputs.add(stacks);
            }
        }
        this.allInputs = allInputs;
        this.minimalInputStacks = minimalInputStacks;
        this.subtypeIndex = subtypeIndex;
        final ImmutableList.Builder<ItemStack> outputs = ImmutableList.builder();
        this.forOutputMatches((v, output) -> outputs.add(output));
        this.outputs = outputs.build();

        if (builder != null && craftingGridHelper != null && focuses != null) {
             this.setRecipe(builder, craftingGridHelper, focuses);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        focuses.getFocuses(VanillaTypes.ITEM_STACK).flatMap(focus -> {
            ItemStack stack = focus.getTypedValue().getIngredient();
            Input input = null;
            if (focus.getRole() == RecipeIngredientRole.INPUT) {
                input = this.getInputsForIngredient(stack);
            } else if (focus.getRole() == RecipeIngredientRole.OUTPUT) {
                input = this.getInputsForOutput(stack);
            }
            return Stream.ofNullable(input);
        }).findFirst().ifPresentOrElse(input -> {
            final List<ItemStack> focusedOutputs = this.getOutput(input.inputs);
            if (focusedOutputs.isEmpty() || focusedOutputs.stream().allMatch(ItemStack::isEmpty)) {
                this.setDefaultRecipe(builder, craftingGridHelper);
                return;
            }
            craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, focusedOutputs);
            List<IRecipeSlotBuilder> slots = craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, input.inputs, this.getWidth(), this.getHeight());
            for (int i = 0; i < 9; i++) {
                GenericIngredient<?, ?> ingredient = input.ingredients[i];
                if (ingredient != null) {
                    IRecipeSlotBuilder slot = slots.get(i);
                    slot.addTooltipCallback((recipeSlotView, tooltip) -> {
                        if (recipeSlotView.getRole() == RecipeIngredientRole.INPUT) {
                            ingredient.addTooltip(tooltip);
                        }
                    });
                }
            }
        }, () -> this.setDefaultRecipe(builder, craftingGridHelper));
    }

    private void setDefaultRecipe(final IRecipeLayoutBuilder builder, final ICraftingGridHelper craftingGridHelper) {
        craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, this.outputs);
        craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, this.allInputs, this.getWidth(), this.getHeight());
    }

    private static final class Input {
        List<List<ItemStack>> inputs;

        GenericIngredient<?, ?>[] ingredients;

        private Input(final List<List<ItemStack>> inputs, final GenericIngredient<?, ?>[] ingredients) {
            this.inputs = inputs;
            this.ingredients = ingredients;
        }
    }
}
