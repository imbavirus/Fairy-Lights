import os
base = r'src\main\java\za\co\infernos\fairylights'
checks = [
    (r'client\model\light\BowModel.java', 'renderToBuffer'),
    (r'client\renderer\block\entity\ConnectionRenderer.java', 'renderToBuffer'),
    (r'client\renderer\block\entity\GarlandVineRenderer.java', 'renderToBuffer'),
    (r'client\renderer\block\entity\GarlandTinselRenderer.java', 'renderToBuffer'),
    (r'client\model\light\LightModel.java', 'renderToBuffer'),
    (r'server\block\entity\FLBlockEntities.java', 'build(null)'),
    (r'server\entity\FenceFastenerEntity.java', 'public boolean hurt('),
    (r'server\block\FastenerBlock.java', 'final boolean isMoving'),
    (r'util\crafting\GenericRecipe.java', 'GenericRecipe is not abstract'),
    (r'server\item\crafting\CopyColorRecipe.java', 'CopyColorRecipe is not abstract'),
    (r'server\integration\jei\GenericRecipeWrapper.java', 'GenericRecipeWrapper'),
    (r'server\integration\jei\ColorSubtypeInterpreter.java', 'ISubtypeInterpreter'),
    (r'data\DataGatherer.java', 'RecipeProvider'),
    (r'client\tutorial\ClippyController.java', 'ToastComponent'),
    (r'server\entity\FLEntities.java', 'build(FairyLights.ID'),
]
for path, search in checks:
    full = os.path.join(base, path)
    if not os.path.exists(full):
        print(f'MISSING: {os.path.basename(full)}')
        continue
    with open(full, 'r', encoding='utf-8') as f:
        content = f.read()
    found = search in content
    status = 'NEEDS FIX' if found else 'OK'
    print(f'{status}: {os.path.basename(full)} ("{search}" found={found}, size={len(content)})')
