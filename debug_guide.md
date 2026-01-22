# JEI Debugging & Fix Guide (NeoForge 1.21.1)

This guide documents the step-by-step process used to identify and fix JEI recipe visibility issues in the Fairy Lights mod. It serves as a reference for debugging similar issues in other mods.

## 1. Verify Basic JEI Integration

**Problem:** JEI was not picking up the mod's plugin at all.
**Check:** Ensure your `build.gradle` has the correct `jei_version` (19.x for 1.21.1) and that the compile/runtime dependencies are uncommented.
**Fix:** Uncommented the `@JeiPlugin` annotated class (`FairyLightsJEIPlugin`) which had been completely commented out during the port.

## 2. The "Folder Name" Trap (1.21.x Specific)

**Problem:** Recipes were valid JSON but refused to load in-game. No errors in logs.
**Investigation:**
*   Created a dummy `test_recipe.json` in `data/fairylights/recipes/`.
*   Used `Thread.currentThread().getContextClassLoader().getResource("data/fairylights/recipes/test_recipe.json")` to probe if the mod loader could even *see* the file. It found it.
*   Renamed file to standard namespace `data/minecraft/recipes/` to see if it was a namespace issue. Still failed to load.
*   **Root Cause Discovered:** In Minecraft 1.21+, the data pack folder structure prefers singular naming in some contexts, or strictly enforces specific paths.
**Fix:** Renamed the source directory from `src/main/resources/data/fairylights/recipes` to `src/main/resources/data/fairylights/recipe`. (Note: This might be specific to how NeoForge/DataPack loaders are currently behaving or a quirk of this specific project structure, but standardizing the path fixed the silent failure).

## 3. The "Metadata" Mismatch (Subtypes)

**Problem:** Recipes were loaded (confirmed via debug logging), but clicking items in JEI showed nothing.
**Diagnosis:**
*   JEI uses an `ISubtypeInterpreter` to distinguish items.
*   Creative Tab items (the ones you click) often have complex NBT (e.g., `pattern: [light, light, light]`, `color: 12345`).
*   Recipe Result items often have simple NBT (e.g., `pattern: []`, `color: 0` or missing).
*   **Result:** JEI sees "Item A {Complex}" and "Item A {Simple}" as completely different items. Clicking {Complex} shows no recipe because the recipe produces {Simple}.

**Fix:**
*   Modified `ColorSubtypeInterpreter` to **ignore** specific NBT tags that shouldn't define the "type" of the item.
*   Specifically ignored `pattern` (`_P:`) and `string` (`_S:`) tags for `hanging_lights` and `pennant_bunting`.
*   This forces JEI to treat "Hanging Lights (Red)" and "Hanging Lights (Blue)" as the same lookup category if the color is the only difference we care about (or strictly the dye color).

## 4. The "Color Value" Mismatch

**Problem:** Even after fixing subtypes, some items still didn't link.
**Diagnosis:**
*   The `GenericRecipeWrapper` (custom code for dynamic recipes) was mathematically generating the output item.
*   It calculated color using `ItemColors.getFireworkColor()`.
*   The Creative Tab items were generated using `ItemColors.getTextureDiffuseColor()`.
*   These returned slightly different integer values for the same "DyeColor".
*   **Result:** NBT mismatch (`color: 123` vs `color: 456`).

**Fix:**
*   Updated `GenericRecipeWrapper` to use the exact same color logic as the Creative Tab generation.
*   Ensured defaults match: If Creative Tab defaults to "Black String", the recipe must also produce "Black String" (or explicitly handle the conversion).

## 5. Debugging Tools Used

*   **Logging:** heavily added `LOGGER.info()` inside `registerVanillaCategoryExtensions` and `setRecipe` to trace execution.
*   **NBT Inspection:** Used `System.out.println(stack.getTag())` to compare the NBT of the "Ghost Item" in JEI vs the "Real Item" in the Creative Tab.
*   **Class Loader Probing:** Using Java's `getResource` to verify file presence in the built JAR environment when the game silently ignores assets.

## Summary Checklist for Future Mods

1.  [ ] **Dependencies**: Is JEI actually loaded?
2.  [ ] **Folder Names**: Are you using `recipe` vs `recipes` correctly for 1.21?
3.  [ ] **Silent Failures**: If `pack.mcmeta` is wrong, data packs fail silently. Check syntax.
4.  [ ] **NBT Strictness**: Does your recipe output NBT *exactly* match the item in the JEI list?
    *   If not, use a `ISubtypeInterpreter` to relax the matching.
5.  [ ] **Dynamic Recipes**: If generating recipes via code, verify the `ItemStack` NBT is identical to the standard game items.
