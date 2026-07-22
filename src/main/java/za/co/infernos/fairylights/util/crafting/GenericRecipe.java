package za.co.infernos.fairylights.util.crafting;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.math.IntMath;
import za.co.infernos.fairylights.util.crafting.ingredient.AuxiliaryIngredient;
import za.co.infernos.fairylights.util.crafting.ingredient.EmptyRegularIngredient;
import za.co.infernos.fairylights.util.crafting.ingredient.GenericIngredient;
import za.co.infernos.fairylights.util.crafting.ingredient.RegularIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

public final class GenericRecipe extends CustomRecipe {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    public static final EmptyRegularIngredient EMPTY = new EmptyRegularIngredient();

    private final Supplier<? extends RecipeSerializer<GenericRecipe>> serializer;

    private final ItemStack output;

    private final RegularIngredient[] ingredients;

    private final AuxiliaryIngredient<?>[] auxiliaryIngredients;

    private final int width;

    private final int height;

    private final int outputIngredient;

    private ItemStack result = ItemStack.EMPTY;

    private final ImmutableList<IntUnaryOperator> xFunctions = ImmutableList.of(IntUnaryOperator.identity(),
            i -> this.getWidth() - 1 - i);

    private int room;

    GenericRecipe(final ResourceLocation id, final Supplier<? extends RecipeSerializer<GenericRecipe>> serializer,
            final ItemStack output, final RegularIngredient[] ingredients,
            final AuxiliaryIngredient<?>[] auxiliaryIngredients, final int width, final int height,
            final int outputIngredient) {
        // CustomRecipe constructor signature changed in 1.21.1 - may need different
        // parameters
        super(CraftingBookCategory.MISC);
        Preconditions.checkArgument(width > 0, "width must be greater than zero");
        Preconditions.checkArgument(height > 0, "height must be greater than zero");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.output = Objects.requireNonNull(output, "output");
        this.ingredients = Objects.requireNonNull(ingredients, "ingredients");
        this.auxiliaryIngredients = checkIngredients(ingredients,
                Objects.requireNonNull(auxiliaryIngredients, "auxiliaryIngredients"));
        this.width = width;
        this.height = height;
        this.outputIngredient = outputIngredient;
        this.room = -1;
    }

    private int getRoom() {
        if (this.room < 0) {
            int room = 0;
            for (final RegularIngredient ing : this.ingredients) {
                if (ing.getInputs().isEmpty()) {
                    room++;
                }
            }
            for (final AuxiliaryIngredient<?> aux : this.auxiliaryIngredients) {
                if (aux.isRequired()) {
                    room--;
                }
            }
            this.room = room;
        }
        return this.room;
    }

    private NonNullList<Ingredient> getDisplayIngredients() {
        final NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
        for (int i = 0; i < this.ingredients.length; i++) {
            final int x = i % this.width;
            final int y = i / this.width;
            final ItemStack[] stacks = this.ingredients[i].getInputs().toArray(new ItemStack[0]);
            ingredients.set(x + y * 3, Ingredient.of(stacks));
        }
        for (int i = 0, slot = 0; slot < ingredients.size(); slot++) {
            final Ingredient ing = ingredients.get(slot);
            if (ing.isEmpty()) {
                while (i < this.auxiliaryIngredients.length) {
                    final AuxiliaryIngredient<?> aux = this.auxiliaryIngredients[i++];
                    if (aux.isRequired()) {
                        final ItemStack[] stacks = aux.getInputs().toArray(new ItemStack[0]);
                        ingredients.set(slot, Ingredient.of(stacks));
                        break;
                    }
                }
            }
        }
        return ingredients;
    }

    @Override
    public boolean isSpecial() {
        return this.output.isEmpty();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer.get();
    }

    public ItemStack getOutput() {
        return this.output.copy();
    }

    public RegularIngredient[] getGenericIngredients() {
        return this.ingredients.clone();
    }

    public AuxiliaryIngredient<?>[] getAuxiliaryIngredients() {
        return this.auxiliaryIngredients.clone();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.getDisplayIngredients();
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height) {
        return this.width <= width && this.height <= height
                && (this.getRoom() >= 0 || width * height - this.width * this.height + this.getRoom() >= 0);
    }

    // matches() signature changed in 1.21.1 - now uses CraftingInput
    @Override
    public boolean matches(final net.minecraft.world.item.crafting.CraftingInput input, final Level world) {
        // LOGGER.debug("GenericRecipe.matches called for " + this.output);
        if (!this.canCraftInDimensions(input.width(), input.height())) {
            return false;
        }
        final int scanWidth = input.width() + 1 - this.width;
        final int scanHeight = input.height() + 1 - this.height;
        for (int i = 0, end = scanWidth * scanHeight; i < end; i++) {
            final int x = i % scanWidth;
            final int y = i / scanWidth;
            for (final IntUnaryOperator func : this.xFunctions) {
                final ItemStack result = this.resolve(input, x, y, func);
                if (!result.isEmpty()) {
                    // LOGGER.info("GenericRecipe.matches SUCCESS for " + result);
                    this.result = result;
                    return true;
                }
            }
        }
        this.result = ItemStack.EMPTY;
        return false;
    }

    private ItemStack resolve(final net.minecraft.world.item.crafting.CraftingInput input, final int originX,
            final int originY, final IntUnaryOperator funcX) {
        final MatchResultRegular[] match = new MatchResultRegular[this.ingredients.length];
        final Multimap<AuxiliaryIngredient<?>, MatchResultAuxiliary> auxMatchResults = LinkedListMultimap.create();
        final Map<AuxiliaryIngredient<?>, Integer> auxMatchTotals = new HashMap<>();
        final Set<GenericIngredient<?, ?>> presentCalled = new HashSet<>();
        final List<MatchResultAuxiliary> auxResults = new ArrayList<>();
        Item item = this.output.getItem();
        final CompoundTag tag = new CompoundTag();
        for (int i = 0, w = input.width(), size = w * input.height(); i < size; i++) {
             final int x = i % w;
            final int y = i / w;
            final int ingX = x - originX;
            final int ingY = y - originY;
            final ItemStack stack = input.getItem(i);
            if (this.contains(ingX, ingY)) {
                final int index = funcX.applyAsInt(ingX) + ingY * this.width;
                final RegularIngredient ingredient = this.ingredients[index];
                final MatchResultRegular result = ingredient.matches(stack);
                if (!result.doesMatch()) {
                    return ItemStack.EMPTY;
                }
                match[index] = result;
                result.forMatch(presentCalled, tag);
                if (index == this.outputIngredient) {
                    final CompoundTag inputTag = new CompoundTag();
                    if (stack.has(za.co.infernos.fairylights.server.item.FLDataComponents.COLOR)) {
                        za.co.infernos.fairylights.server.item.DyeableItem.setColor(inputTag, za.co.infernos.fairylights.server.item.DyeableItem.getColor(stack));
                    }
                    if (stack.has(za.co.infernos.fairylights.server.item.FLDataComponents.CONNECTION_LOGIC)) {
                        inputTag.merge(stack.get(za.co.infernos.fairylights.server.item.FLDataComponents.CONNECTION_LOGIC));
                    }
                    if (stack.has(za.co.infernos.fairylights.server.item.FLDataComponents.STYLED_STRING)) {
                        inputTag.put("text",
                                stack.get(za.co.infernos.fairylights.server.item.FLDataComponents.STYLED_STRING));
                    }
                    // Only preserve an active twinkle; a false component must not stick around
                    // or later crafts/tooltips treat the stack as twinkle-capable incorrectly.
                    if (Boolean.TRUE.equals(stack.get(za.co.infernos.fairylights.server.item.FLDataComponents.TWINKLE))) {
                        inputTag.putBoolean("twinkle", true);
                    }
                    final java.util.List<Integer> inputColors =
                            stack.get(za.co.infernos.fairylights.server.item.FLDataComponents.COLORS.get());
                    if (inputColors != null && !inputColors.isEmpty()) {
                        final net.minecraft.nbt.ListTag colorsTag = new net.minecraft.nbt.ListTag();
                        for (final int color : inputColors) {
                            colorsTag.add(net.minecraft.nbt.IntTag.valueOf(color));
                        }
                        inputTag.put("colors", colorsTag);
                    } else if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
                        final CompoundTag custom = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();
                        if (custom.contains("colors", net.minecraft.nbt.Tag.TAG_LIST)) {
                            inputTag.put("colors", custom.getList("colors", net.minecraft.nbt.Tag.TAG_INT).copy());
                        }
                    }

                    if (tag.isEmpty()) {
                        tag.merge(inputTag);
                    } else {
                        final CompoundTag temp = inputTag.copy();
                        temp.merge(tag);
                        tag.merge(temp);
                    }
                    item = stack.getItem();
                }
            } else if (!EMPTY.matches(stack).doesMatch()) {
                boolean nonAuxiliary = true;
                for (final AuxiliaryIngredient<?> auxiliaryIngredient : this.auxiliaryIngredients) {
                    final MatchResultAuxiliary result = auxiliaryIngredient.matches(stack);
                    if (result.doesMatch()) {
                        if (result.isAtLimit(auxMatchTotals.getOrDefault(result.ingredient, 0))) {
                            return ItemStack.EMPTY;
                        }
                        result.forMatch(presentCalled, tag);
                        auxMatchTotals.merge(result.ingredient, 1, IntMath::checkedAdd);
                        nonAuxiliary = false;
                        result.propagate(auxMatchResults);
                    }
                    auxResults.add(result);
                }
                if (nonAuxiliary) {
                    return ItemStack.EMPTY;
                }
            }
        }
        final Set<GenericIngredient<?, ?>> absentCalled = new HashSet<>();
        for (final MatchResultRegular result : match) {
            if (result != null) {
                result.notifyAbsence(presentCalled, absentCalled, tag);
            }
        }
        for (final MatchResultAuxiliary result : auxResults) {
            result.notifyAbsence(presentCalled, absentCalled, tag);
        }
        for (final AuxiliaryIngredient<?> ingredient : this.auxiliaryIngredients) {
            if (ingredient.process(auxMatchResults, tag)) {
                return ItemStack.EMPTY;
            }
        }
        final ItemStack output = this.output.isEmpty() ? new ItemStack(item) : this.output.copy();
        if (!tag.isEmpty()) {
            if (tag.contains("color", net.minecraft.nbt.Tag.TAG_INT)) {
                za.co.infernos.fairylights.server.item.DyeableItem.setColor(output, tag.getInt("color"));
                // Solid recolor replaces any prior color-changing list.
                if (!tag.contains("colors", net.minecraft.nbt.Tag.TAG_LIST)) {
                    output.remove(za.co.infernos.fairylights.server.item.FLDataComponents.COLORS.get());
                }
            }
            if (tag.contains("twinkle")) {
                if (tag.getBoolean("twinkle")) {
                    output.set(za.co.infernos.fairylights.server.item.FLDataComponents.TWINKLE, true);
                } else {
                    output.remove(za.co.infernos.fairylights.server.item.FLDataComponents.TWINKLE);
                }
            }
            if (tag.contains("colors", net.minecraft.nbt.Tag.TAG_LIST)) {
                final net.minecraft.nbt.ListTag colorsTag = tag.getList("colors", net.minecraft.nbt.Tag.TAG_INT);
                final java.util.List<Integer> colors = new java.util.ArrayList<>(colorsTag.size());
                for (int i = 0; i < colorsTag.size(); i++) {
                    colors.add(colorsTag.getInt(i));
                }
                if (!colors.isEmpty()) {
                    output.set(za.co.infernos.fairylights.server.item.FLDataComponents.COLORS.get(), colors);
                    // Keep a solid tint fallback for UIs that only read COLOR.
                    za.co.infernos.fairylights.server.item.DyeableItem.setColor(output, colors.get(0));
                    // Legacy CUSTOM_DATA copy for older readers / backups.
                    final CompoundTag custom = output.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                    custom.put("colors", colorsTag.copy());
                    output.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.of(custom));
                }
            }
            CompoundTag logic = output
                    .getOrDefault(za.co.infernos.fairylights.server.item.FLDataComponents.CONNECTION_LOGIC, new CompoundTag())
                    .copy();
            boolean updateLogic = false;
            if (tag.contains("pattern")) {
                logic.put("pattern", tag.getList("pattern", net.minecraft.nbt.Tag.TAG_COMPOUND));
                updateLogic = true;
            }
            if (tag.contains("string")) {
                logic.putString("string", tag.getString("string"));
                updateLogic = true;
            }
            if (updateLogic) {
                output.set(za.co.infernos.fairylights.server.item.FLDataComponents.CONNECTION_LOGIC, logic);
            }

            if (tag.contains("text")) {
                output.set(za.co.infernos.fairylights.server.item.FLDataComponents.STYLED_STRING, tag.getCompound("text"));
            }
        }
        return output;
    }

    private boolean contains(final int x, final int y) {
        return x >= 0 && y >= 0 && x < this.width && y < this.height;
    }

    @Override
    public ItemStack assemble(final net.minecraft.world.item.crafting.CraftingInput input,
            final net.minecraft.core.HolderLookup.Provider provider) {
         if (!this.canCraftInDimensions(input.width(), input.height())) {
            return ItemStack.EMPTY;
        }
        final int scanWidth = input.width() + 1 - this.width;
        final int scanHeight = input.height() + 1 - this.height;
        for (int i = 0, end = scanWidth * scanHeight; i < end; i++) {
            final int x = i % scanWidth;
            final int y = i / scanWidth;
            for (final IntUnaryOperator func : this.xFunctions) {
                final ItemStack result = this.resolve(input, x, y, func);
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    // getResultItem() signature changed in 1.21.1 - now uses Provider
    @Override
    public ItemStack getResultItem(final net.minecraft.core.HolderLookup.Provider provider) {
        if (!this.output.isEmpty()) {
            return this.output;
        }
        // Twinkle / color-changing recipes derive output from inputs (isSpecial). Assemble a
        // sample with required auxiliaries so JEI indexes the real result (e.g. twinkle=true).
        final ItemStack assembled = this.assembleSample(provider);
        if (!assembled.isEmpty()) {
            return assembled;
        }
        if (this.outputIngredient >= 0 && this.outputIngredient < this.ingredients.length) {
            final ImmutableList<ItemStack> samples = this.ingredients[this.outputIngredient].getInputs();
            if (!samples.isEmpty()) {
                return samples.get(0).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack assembleSample(final net.minecraft.core.HolderLookup.Provider provider) {
        final java.util.List<ItemStack> stacks = new java.util.ArrayList<>(java.util.Collections.nCopies(9, ItemStack.EMPTY));
        for (int i = 0; i < this.ingredients.length; i++) {
            final ImmutableList<ItemStack> inputs = this.ingredients[i].getInputs();
            if (inputs.isEmpty()) {
                continue;
            }
            final int x = i % this.width;
            final int y = i / this.width;
            stacks.set(x + y * 3, inputs.get(0).copy());
        }
        int slot = 0;
        for (final AuxiliaryIngredient<?> aux : this.auxiliaryIngredients) {
            if (!aux.isRequired()) {
                continue;
            }
            final ImmutableList<ItemStack> inputs = aux.getInputs();
            if (inputs.isEmpty()) {
                continue;
            }
            while (slot < 9 && !stacks.get(slot).isEmpty()) {
                slot++;
            }
            if (slot >= 9) {
                break;
            }
            stacks.set(slot++, inputs.get(0).copy());
        }
        final net.minecraft.world.item.crafting.CraftingInput input =
                net.minecraft.world.item.crafting.CraftingInput.of(3, 3, stacks);
        return this.matches(input, null) ? this.assemble(input, provider) : ItemStack.EMPTY;
    }

    public interface MatchResult<I extends GenericIngredient<I, M>, M extends MatchResult<I, M>> {
        I getIngredient();

        ItemStack getInput();

        boolean doesMatch();

        void forMatch(final Set<GenericIngredient<?, ?>> called, final CompoundTag nbt);

        void notifyAbsence(final Set<GenericIngredient<?, ?>> presentCalled,
                final Set<GenericIngredient<?, ?>> absentCalled, final CompoundTag nbt);

        M withParent(final M parent);
    }

    public static class MatchResultRegular implements MatchResult<RegularIngredient, MatchResultRegular> {
        protected final RegularIngredient ingredient;

        protected final ItemStack input;

        protected final boolean doesMatch;

        protected final ImmutableList<MatchResultRegular> supplementaryResults;

        public MatchResultRegular(final RegularIngredient ingredient, final ItemStack input, final boolean doesMatch,
                final List<MatchResultRegular> supplementaryResults) {
            this.ingredient = Objects.requireNonNull(ingredient, "ingredient");
            this.input = input;
            this.doesMatch = doesMatch;
            this.supplementaryResults = ImmutableList.copyOf(supplementaryResults);
        }

        @Override
        public final RegularIngredient getIngredient() {
            return this.ingredient;
        }

        @Override
        public final ItemStack getInput() {
            return this.input;
        }

        @Override
        public final boolean doesMatch() {
            return this.doesMatch;
        }

        @Override
        public void forMatch(final Set<GenericIngredient<?, ?>> called, final CompoundTag nbt) {
            this.ingredient.matched(this.input, nbt);
            if (called.add(this.ingredient)) {
                this.ingredient.present(nbt);
            }
        }

        @Override
        public void notifyAbsence(final Set<GenericIngredient<?, ?>> presentCalled,
                final Set<GenericIngredient<?, ?>> absentCalled, final CompoundTag nbt) {
            if (!presentCalled.contains(this.ingredient) && !absentCalled.contains(this.ingredient)) {
                this.ingredient.absent(nbt);
                absentCalled.add(this.ingredient);
            }
            for (final MatchResultRegular result : this.supplementaryResults) {
                result.notifyAbsence(presentCalled, absentCalled, nbt);
            }
        }

        @Override
        public MatchResultRegular withParent(final MatchResultRegular parent) {
            return new MatchResultParentedRegular(this.ingredient, this.input, this.doesMatch,
                    this.supplementaryResults, parent);
        }
    }

    public static class MatchResultParentedRegular extends MatchResultRegular {
        protected final MatchResultRegular parent;

        public MatchResultParentedRegular(final RegularIngredient ingredient, final ItemStack input,
                final boolean doesMatch, final List<MatchResultRegular> supplementaryResults,
                final MatchResultRegular parent) {
            super(ingredient, input, doesMatch, supplementaryResults);
            this.parent = Objects.requireNonNull(parent, "parent");
        }

        @Override
        public void forMatch(final Set<GenericIngredient<?, ?>> called, final CompoundTag nbt) {
            super.forMatch(called, nbt);
            this.parent.forMatch(called, nbt);
        }

        @Override
        public void notifyAbsence(final Set<GenericIngredient<?, ?>> presentCalled,
                final Set<GenericIngredient<?, ?>> absentCalled, final CompoundTag nbt) {
            super.notifyAbsence(presentCalled, absentCalled, nbt);
            this.parent.notifyAbsence(presentCalled, absentCalled, nbt);
        }

        @Override
        public MatchResultRegular withParent(final MatchResultRegular parent) {
            return this.parent.withParent(new MatchResultParentedRegular(this.ingredient, this.input, this.doesMatch,
                    this.supplementaryResults, parent));
        }
    }

    public static class MatchResultAuxiliary implements MatchResult<AuxiliaryIngredient<?>, MatchResultAuxiliary> {
        protected final AuxiliaryIngredient<?> ingredient;

        protected final ItemStack input;

        protected final boolean doesMatch;

        protected final ImmutableList<MatchResultAuxiliary> supplementaryResults;

        public MatchResultAuxiliary(final AuxiliaryIngredient<?> ingredient, final ItemStack input,
                final boolean doesMatch, final List<MatchResultAuxiliary> supplementaryResults) {
            this.ingredient = Objects.requireNonNull(ingredient, "ingredient");
            this.input = input;
            this.doesMatch = doesMatch;
            this.supplementaryResults = ImmutableList.copyOf(supplementaryResults);
        }

        @Override
        public final AuxiliaryIngredient<?> getIngredient() {
            return this.ingredient;
        }

        @Override
        public final ItemStack getInput() {
            return this.input;
        }

        @Override
        public final boolean doesMatch() {
            return this.doesMatch;
        }

        @Override
        public void forMatch(final Set<GenericIngredient<?, ?>> called, final CompoundTag nbt) {
            if (!called.contains(this.ingredient)) {
                this.ingredient.present(nbt);
                called.add(this.ingredient);
            }
        }

        @Override
        public void notifyAbsence(final Set<GenericIngredient<?, ?>> presentCalled,
                final Set<GenericIngredient<?, ?>> absentCalled, final CompoundTag nbt) {
            if (!presentCalled.contains(this.ingredient) && !absentCalled.contains(this.ingredient)) {
                this.ingredient.absent(nbt);
                absentCalled.add(this.ingredient);
            }
            for (final MatchResultAuxiliary result : this.supplementaryResults) {
                result.notifyAbsence(presentCalled, absentCalled, nbt);
            }
        }

        @Override
        public MatchResultAuxiliary withParent(final MatchResultAuxiliary parent) {
            return new MatchResultParentedAuxiliary(this.ingredient, this.input, this.doesMatch,
                    this.supplementaryResults, parent);
        }

        public boolean isAtLimit(final int count) {
            return count >= this.ingredient.getLimit();
        }

        public void propagate(final Multimap<AuxiliaryIngredient<?>, MatchResultAuxiliary> map) {
            map.put(this.ingredient, this);
        }
    }

    public static class MatchResultParentedAuxiliary extends MatchResultAuxiliary {
        protected final MatchResultAuxiliary parent;

        public MatchResultParentedAuxiliary(final AuxiliaryIngredient<?> ingredient, final ItemStack input,
                final boolean doesMatch, final List<MatchResultAuxiliary> supplementaryResults,
                final MatchResultAuxiliary parent) {
            super(ingredient, input, doesMatch, supplementaryResults);
            this.parent = Objects.requireNonNull(parent, "parent");
        }

        @Override
        public void forMatch(final Set<GenericIngredient<?, ?>> called, final CompoundTag nbt) {
            super.forMatch(called, nbt);
            this.parent.forMatch(called, nbt);
        }

        @Override
        public void notifyAbsence(final Set<GenericIngredient<?, ?>> presentCalled,
                final Set<GenericIngredient<?, ?>> absentCalled, final CompoundTag nbt) {
            super.notifyAbsence(presentCalled, absentCalled, nbt);
            this.parent.notifyAbsence(presentCalled, absentCalled, nbt);
        }

        @Override
        public MatchResultAuxiliary withParent(final MatchResultAuxiliary parent) {
            return this.parent.withParent(new MatchResultParentedAuxiliary(this.ingredient, this.input, this.doesMatch,
                    this.supplementaryResults, parent));
        }

        @Override
        public boolean isAtLimit(final int count) {
            return super.isAtLimit(count) || this.parent.isAtLimit(count);
        }

        @Override
        public void propagate(final Multimap<AuxiliaryIngredient<?>, MatchResultAuxiliary> map) {
            super.propagate(map);
            this.parent.propagate(map);
        }
    }

    private static AuxiliaryIngredient<?>[] checkIngredients(final RegularIngredient[] ingredients,
            final AuxiliaryIngredient<?>[] auxiliaryIngredients) {
        checkForNulls(ingredients);
        checkForNulls(auxiliaryIngredients);
        final boolean ingredientDictator = checkDictatorship(false, ingredients);
        checkDictatorship(ingredientDictator, auxiliaryIngredients);
        return auxiliaryIngredients;
    }

    private static void checkForNulls(final GenericIngredient<?, ?>[] ingredients) {
        for (int i = 0; i < ingredients.length; i++) {
            if (ingredients[i] == null) {
                throw new NullPointerException("Must not have null ingredients, found at index " + i);
            }
        }
    }

    private static boolean checkDictatorship(boolean foundDictator, final GenericIngredient<?, ?>[] ingredients) {
        for (final GenericIngredient<?, ?> ingredient : ingredients) {
            if (ingredient.dictatesOutputType()) {
                if (foundDictator) {
                    throw new IllegalRecipeException("Only one ingredient can dictate output type");
                }
                foundDictator = true;
            }
        }
        return foundDictator;
    }
}
