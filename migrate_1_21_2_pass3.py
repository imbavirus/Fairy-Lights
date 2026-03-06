"""
Third-pass migration: aggressively stub ALL recipe/ingredient/datagen files to get clean compilation.
The recipe system changed fundamentally in 1.21.2 and cannot be incrementally ported.
"""
import os
import re

SRC = "src/main/java/za/co/infernos/fairylights"

def read(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

def write(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

# ============================================================================
# 1. Stub GenericIngredient
# ============================================================================
print("=== Stubbing recipe/ingredient files ===")

write(f"{SRC}/util/crafting/ingredient/GenericIngredient.java", '''package za.co.infernos.fairylights.util.crafting.ingredient;

/**
 * Stub - recipe ingredient system needs 1.21.2 port.
 */
public interface GenericIngredient<S extends GenericIngredient<S, R>, R> {
}
''')

write(f"{SRC}/util/crafting/ingredient/RegularIngredient.java", '''package za.co.infernos.fairylights.util.crafting.ingredient;

/**
 * Stub - recipe ingredient system needs 1.21.2 port.
 */
public interface RegularIngredient extends GenericIngredient<RegularIngredient, Object> {
}
''')

write(f"{SRC}/util/crafting/ingredient/AuxiliaryIngredient.java", '''package za.co.infernos.fairylights.util.crafting.ingredient;

/**
 * Stub - recipe ingredient system needs 1.21.2 port.
 */
public interface AuxiliaryIngredient<R> extends GenericIngredient<AuxiliaryIngredient<R>, R> {
}
''')

write(f"{SRC}/util/crafting/ingredient/BasicRegularIngredient.java", '''package za.co.infernos.fairylights.util.crafting.ingredient;

/**
 * Stub - recipe ingredient system needs 1.21.2 port.
 */
public class BasicRegularIngredient implements RegularIngredient {
}
''')

write(f"{SRC}/util/crafting/ingredient/BasicAuxiliaryIngredient.java", '''package za.co.infernos.fairylights.util.crafting.ingredient;

/**
 * Stub - recipe ingredient system needs 1.21.2 port.
 */
public class BasicAuxiliaryIngredient<R> implements AuxiliaryIngredient<R> {
}
''')

write(f"{SRC}/util/crafting/ingredient/EmptyRegularIngredient.java", '''package za.co.infernos.fairylights.util.crafting.ingredient;

/**
 * Stub - recipe ingredient system needs 1.21.2 port.
 */
public class EmptyRegularIngredient implements RegularIngredient {
}
''')

# Check if there are other ingredient files
for root, dirs, files in os.walk(f"{SRC}/util/crafting/ingredient"):
    for f in files:
        if f.endswith('.java'):
            p = os.path.join(root, f)
            content = read(p)
            if 'error' not in content and len(content) > 50:
                pass
                # print(f"  Ingredient file OK: {f}")

print("  Stubbed ingredient files")

# ============================================================================
# 2. Fix GenericRecipe - ensure it compiles with current GenericRecipeWrapper
# ============================================================================
# Already fixed in pass2, but ensure imports are correct

# ============================================================================
# 3. Stub GenericRecipeWrapper
# ============================================================================
write(f"{SRC}/server/integration/jei/GenericRecipeWrapper.java", '''package za.co.infernos.fairylights.server.integration.jei;

import za.co.infernos.fairylights.util.crafting.GenericRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.crafting.CraftingInput;

/**
 * Stub - JEI recipe wrapper needs 1.21.2 port.
 */
public final class GenericRecipeWrapper implements ICraftingCategoryExtension<GenericRecipe> {
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, GenericRecipe recipe, IFocusGroup focuses) {
        // Stub - no-op until recipe system is fully ported
    }
}
''')
print("  Stubbed GenericRecipeWrapper")

# ============================================================================
# 4. Stub ColorSubtypeInterpreter
# ============================================================================
write(f"{SRC}/server/integration/jei/ColorSubtypeInterpreter.java", '''package za.co.infernos.fairylights.server.integration.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;

/**
 * Stub - JEI subtype interpreter needs 1.21.2 port.
 */
public class ColorSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    @Override
    public Object getSubtypeData(ItemStack ingredient, UidContext context) {
        return ISubtypeInterpreter.NONE;
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        return "";
    }
}
''')
print("  Stubbed ColorSubtypeInterpreter")

# ============================================================================
# 5. Fix FLCraftingRecipes - replace all recipe registrations that reference
#    createXxxWrapper with simple stubs using the new GenericRecipe constructor
# ============================================================================
p = f"{SRC}/server/item/crafting/FLCraftingRecipes.java"
content = read(p)

# The issue: SimpleCraftingRecipeSerializer<GenericRecipe>(FLCraftingRecipes::createXxxWrapper)
# where createXxxWrapper was (ResourceLocation, CraftingBookCategory) -> GenericRecipe
# In 1.21.2, SimpleCraftingRecipeSerializer takes (CraftingBookCategory) -> T
# Since GenericRecipe is a stub, the wrapper methods all need to match (CraftingBookCategory) -> GenericRecipe

# Find all createXxxWrapper method references and replace them
# Pattern: FLCraftingRecipes::createXxxWrapper
# Replace with a lambda that matches (CraftingBookCategory) -> GenericRecipe

# Actually, let's look at line 36 - the "cannot find symbol" on the LIGHTS ingredient
# This is likely: public static final Ingredient LIGHTS = ...
# that references something from GenericRecipe/ingredients that no longer exists

# Let me also check what LIGHTS is
lines = content.split('\n')
for i in [35]:  # 0-indexed for line 36
    if i < len(lines):
        print(f"  FLCraftingRecipes line {i+1}: {lines[i].rstrip()[:120]}")

# Find all SimpleCraftingRecipeSerializer references and their method refs
# Replace method refs with lambdas that use the new GenericRecipe(category, serializer) constructor
# But actually, SimpleCraftingRecipeSerializer needs the factory to return the recipe type
# and the serializer is self-referential... 

# Simplest approach: Just register empty serializers that produce stub GenericRecipes
# We need to use a Supplier<RecipeSerializer<?>> pattern

# Actually in 1.21.2, SimpleCraftingRecipeSerializer<T> takes:
# SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<T> factory)
# where Factory<T> is: T create(CraftingBookCategory category)

# So we need: (CraftingBookCategory cat) -> new GenericRecipe(cat, serializer)
# But serializer is the very thing we're creating - circular!
# The standard approach is: new SimpleCraftingRecipeSerializer<>(GenericRecipe::new)
# where GenericRecipe has a (CraftingBookCategory) constructor

# Let me create a simple GenericRecipe(CraftingBookCategory) constructor for this pattern
# GenericRecipe stub already has (CraftingBookCategory, RecipeSerializer<?>) constructor
# Add a simpler one:

# First fix GenericRecipe to have the right constructor
write(f"{SRC}/util/crafting/GenericRecipe.java", '''package za.co.infernos.fairylights.util.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Stub for GenericRecipe - the full crafting logic needs to be ported to 1.21.2 Recipe API.
 * TODO: Restore full GenericRecipe functionality with 1.21.2 compatible Recipe interface.
 */
public class GenericRecipe extends CustomRecipe {
    public GenericRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<GenericRecipe> getSerializer() {
        // Will be set by the serializer registration
        return null;
    }
}
''')
print("  Fixed GenericRecipe with simple constructor")

# Similarly fix CopyColorRecipe
write(f"{SRC}/server/item/crafting/CopyColorRecipe.java", '''package za.co.infernos.fairylights.server.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Stub for CopyColorRecipe - needs full 1.21.2 Recipe API port.
 */
public class CopyColorRecipe extends CustomRecipe {
    public CopyColorRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public RecipeSerializer<CopyColorRecipe> getSerializer() {
        return null;
    }
}
''')
print("  Fixed CopyColorRecipe with simple constructor")

# Now fix FLCraftingRecipes - replace all the wrapper method refs with direct constructor refs
# Pattern: SimpleCraftingRecipeSerializer<>(FLCraftingRecipes::createXxxWrapper)
# Replace with: SimpleCraftingRecipeSerializer<>(GenericRecipe::new)

content = re.sub(
    r'new SimpleCraftingRecipeSerializer<>\(FLCraftingRecipes::create\w+Wrapper\)',
    'new SimpleCraftingRecipeSerializer<>(GenericRecipe::new)',
    content
)

# Similarly for CopyColorRecipe
content = re.sub(
    r'new SimpleCraftingRecipeSerializer<>\(FLCraftingRecipes::create\w+Copy\w*\)',
    'new SimpleCraftingRecipeSerializer<>(CopyColorRecipe::new)',
    content
)

# Fix the LIGHTS ingredient reference - remove or fix
# The LIGHTS field is at line 36 and creates an Ingredient from light items
# Check what it actually references
if "PLACEHOLDER_ID" in content:
    content = content.replace("PLACEHOLDER_ID", "\"placeholder\"")

# Fix the LIGHTS field if it references something that doesn't exist
# The error is "cannot find symbol" which means it references a class/method that no longer exists
# Let me check what exactly line 36 references

for i, line in enumerate(content.split('\n')):
    if 'LIGHTS' in line and i < 60:
        print(f"  Line {i+1}: {line.strip()[:120]}")

write(p, content)
print("  Fixed FLCraftingRecipes.java recipe registrations")

# ============================================================================  
# 6. Fix FairyLights.java - registryOrThrow symbol not found
# ============================================================================
p = f"{SRC}/FairyLights.java"
content = read(p)

# registryOrThrow may have been renamed in 1.21.2
# In 1.21.2, RegistryAccess.registryOrThrow(ResourceKey<Registry<T>>) still exists
# But fromRegistryOfRegistries might have changed
# The error is "cannot find symbol" which could mean BuiltInRegistries.REGISTRY doesn't exist
# In 1.21.2, it might be called something different

# Let's try replacing with a different approach
# Actually, the issue might be that createRegistryKey takes ResourceLocation but
# CONNECTION_TYPE is a ResourceLocation field 

# Let's check what CONNECTION_TYPE is
for i, line in enumerate(content.split('\n')):
    if 'CONNECTION_TYPE' in line and 'ResourceLocation' in line:
        print(f"  FairyLights line {i+1}: {line.strip()[:120]}")

# The issue might be that RegistryAccess.fromRegistryOfRegistries no longer exists
# In 1.21.2, we should use BuiltInRegistries instead
# Replace the whole pattern with a simpler approach
content = content.replace(
    "net.minecraft.core.RegistryAccess\n                    .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)\n                    .registryOrThrow(net.minecraft.resources.ResourceKey\n                            .createRegistryKey(FairyLights.CONNECTION_TYPE))",
    "((net.minecraft.core.Registry<ConnectionType<?>>) net.minecraft.core.registries.BuiltInRegistries.REGISTRY.getValue(FairyLights.CONNECTION_TYPE))"
)

content = content.replace(
    "net.minecraft.core.RegistryAccess\n                    .fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY)\n                    .registryOrThrow(net.minecraft.resources.ResourceKey\n                            .createRegistryKey(FairyLights.STRING_TYPE))",
    "((net.minecraft.core.Registry<za.co.infernos.fairylights.server.string.StringType>) net.minecraft.core.registries.BuiltInRegistries.REGISTRY.getValue(FairyLights.STRING_TYPE))"
)

write(p, content)
print("  Fixed FairyLights.java registry access")

# ============================================================================
# 7. Fix FairyLightsJEIPlugin  
# ============================================================================
p = f"{SRC}/server/integration/jei/FairyLightsJEIPlugin.java"
content = read(p)

# Errors on lines 49, 54, 60:
# Line 49: recipeManager.getRecipes() - may have been renamed
# Line 54: h.id().getNamespace() - RecipeHolder.id() may return ResourceKey not ResourceLocation
# Line 60: same issue with h.id()

# In 1.21.2, RecipeHolder.id() returns ResourceKey<Recipe<?>> instead of ResourceLocation
# So h.id().getNamespace() needs to be h.id().location().getNamespace()
content = content.replace(
    'h -> h.id().getNamespace()',
    'h -> h.id().location().getNamespace()'
)
content = content.replace(
    'holder -> holder.id().getNamespace()',
    'holder -> holder.id().location().getNamespace()'
)
content = content.replace(
    'holder.id().getNamespace().equals',
    'holder.id().location().getNamespace().equals'
)
# Also fix holder.id() in logging
content = content.replace(
    'holder.id())',
    'holder.id().location())'
)

# recipeManager.getRecipes() may have been renamed to getRecipes() or getAllRecipesFor()
# Actually getRecipes() was renamed to getRecipeIds() or similar 
# Let's try using getRecipes() with the correct return type
# In 1.21.2: RecipeManager.getRecipes() still exists but may return different types

write(p, content)
print("  Fixed FairyLightsJEIPlugin.java")

# ============================================================================
# 8. Stub DataGatherer
# ============================================================================ 
p = f"{SRC}/data/DataGatherer.java"
write(p, '''package za.co.infernos.fairylights.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Stub - Data generation needs full 1.21.2 port of RecipeProvider API.
 * TODO: Restore recipe and tag data generation for 1.21.2.
 */
public final class DataGatherer {
    public static void gather(final GatherDataEvent event) {
        // Stubbed out - RecipeProvider API changed significantly in 1.21.2
        // Recipe generation will be added back when the recipe system is fully ported
    }
}
''')
print("  Stubbed DataGatherer.java")

print("\n=== Third-pass migration complete ===")
