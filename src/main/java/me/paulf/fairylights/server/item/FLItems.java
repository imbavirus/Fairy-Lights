package me.paulf.fairylights.server.item;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.block.LightBlock;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class FLItems {
    private FLItems() {}

    public static final DeferredRegister<Item> REG = DeferredRegister.create(Registries.ITEM, FairyLights.ID);

    public static final DeferredHolder<Item,ConnectionItem> HANGING_LIGHTS = REG.register("hanging_lights", () -> new HangingLightsConnectionItem(defaultProperties()));

    public static final DeferredHolder<Item,ConnectionItem> PENNANT_BUNTING = REG.register("pennant_bunting", () -> new PennantBuntingConnectionItem(defaultProperties()));

    public static final DeferredHolder<Item,ConnectionItem> TINSEL = REG.register("tinsel", () -> new TinselConnectionItem(defaultProperties()));

    public static final DeferredHolder<Item,ConnectionItem> LETTER_BUNTING = REG.register("letter_bunting", () -> new LetterBuntingConnectionItem(defaultProperties()));

    public static final DeferredHolder<Item,ConnectionItem> GARLAND = REG.register("garland", () -> new GarlandConnectionItem(defaultProperties()));

    public static final DeferredHolder<Item,LightItem> FAIRY_LIGHT = REG.register("fairy_light", FLItems.createColorLight(FLBlocks.FAIRY_LIGHT));

    public static final DeferredHolder<Item,LightItem> PAPER_LANTERN = REG.register("paper_lantern", FLItems.createColorLight(FLBlocks.PAPER_LANTERN));

    public static final DeferredHolder<Item,LightItem> ORB_LANTERN = REG.register("orb_lantern", FLItems.createColorLight(FLBlocks.ORB_LANTERN));

    public static final DeferredHolder<Item,LightItem> FLOWER_LIGHT = REG.register("flower_light", FLItems.createColorLight(FLBlocks.FLOWER_LIGHT));

    public static final DeferredHolder<Item,LightItem> CANDLE_LANTERN_LIGHT = REG.register("candle_lantern_light", FLItems.createColorLight(FLBlocks.CANDLE_LANTERN_LIGHT));

    public static final DeferredHolder<Item,LightItem> OIL_LANTERN_LIGHT = REG.register("oil_lantern_light", FLItems.createColorLight(FLBlocks.OIL_LANTERN_LIGHT));

    public static final DeferredHolder<Item,LightItem> JACK_O_LANTERN = REG.register("jack_o_lantern", FLItems.createColorLight(FLBlocks.JACK_O_LANTERN));

    public static final DeferredHolder<Item,LightItem> SKULL_LIGHT = REG.register("skull_light", FLItems.createColorLight(FLBlocks.SKULL_LIGHT));

    public static final DeferredHolder<Item,LightItem> GHOST_LIGHT = REG.register("ghost_light", FLItems.createColorLight(FLBlocks.GHOST_LIGHT));

    public static final DeferredHolder<Item,LightItem> SPIDER_LIGHT = REG.register("spider_light", FLItems.createColorLight(FLBlocks.SPIDER_LIGHT));

    public static final DeferredHolder<Item,LightItem> WITCH_LIGHT = REG.register("witch_light", FLItems.createColorLight(FLBlocks.WITCH_LIGHT));

    public static final DeferredHolder<Item,LightItem> SNOWFLAKE_LIGHT = REG.register("snowflake_light", FLItems.createColorLight(FLBlocks.SNOWFLAKE_LIGHT));

    public static final DeferredHolder<Item,LightItem> HEART_LIGHT = REG.register("heart_light", FLItems.createColorLight(FLBlocks.HEART_LIGHT));

    public static final DeferredHolder<Item,LightItem> MOON_LIGHT = REG.register("moon_light", FLItems.createColorLight(FLBlocks.MOON_LIGHT));

    public static final DeferredHolder<Item,LightItem> STAR_LIGHT = REG.register("star_light", FLItems.createColorLight(FLBlocks.STAR_LIGHT));

    public static final DeferredHolder<Item,LightItem> ICICLE_LIGHTS = REG.register("icicle_lights", FLItems.createColorLight(FLBlocks.ICICLE_LIGHTS));

    public static final DeferredHolder<Item,LightItem> METEOR_LIGHT = REG.register("meteor_light", FLItems.createColorLight(FLBlocks.METEOR_LIGHT));

    public static final DeferredHolder<Item,LightItem> OIL_LANTERN = REG.register("oil_lantern", FLItems.createLight(FLBlocks.OIL_LANTERN, LightItem::new));

    public static final DeferredHolder<Item,LightItem> CANDLE_LANTERN = REG.register("candle_lantern", FLItems.createLight(FLBlocks.CANDLE_LANTERN, LightItem::new));

    public static final DeferredHolder<Item,LightItem> INCANDESCENT_LIGHT = REG.register("incandescent_light", FLItems.createLight(FLBlocks.INCANDESCENT_LIGHT, LightItem::new));

    public static final DeferredHolder<Item,Item> TRIANGLE_PENNANT = REG.register("triangle_pennant", () -> new PennantItem(defaultProperties()));

    public static final DeferredHolder<Item,Item> SPEARHEAD_PENNANT = REG.register("spearhead_pennant", () -> new PennantItem(defaultProperties()));

    public static final DeferredHolder<Item,Item> SWALLOWTAIL_PENNANT = REG.register("swallowtail_pennant", () -> new PennantItem(defaultProperties()));

    public static final DeferredHolder<Item,Item> SQUARE_PENNANT = REG.register("square_pennant", () -> new PennantItem(defaultProperties()));

    private static Item.Properties defaultProperties() {
        return new Item.Properties();
    }

    // createLight() signature changed - FLBlocks returns DeferredHolder<Block,LightBlock>, not DeferredHolder<Item,LightBlock>
    private static Supplier<LightItem> createLight(final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.block.Block, ? extends LightBlock> block, final BiFunction<LightBlock, Item.Properties, LightItem> factory) {
        return () -> {
            final LightBlock lightBlock = block.get();
            return factory.apply(lightBlock, defaultProperties().stacksTo(16));
        };
    }

    private static Supplier<LightItem> createColorLight(final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.block.Block, ? extends LightBlock> block) {
        return () -> {
            final LightBlock lightBlock = block.get();
            return new ColorLightItem(lightBlock, defaultProperties().stacksTo(16));
        };
    }

    public static Stream<LightItem> lights() {
        return REG.getEntries().stream()
            .map(DeferredHolder::get)
            .filter(LightItem.class::isInstance)
            .map(LightItem.class::cast);
    }
}
