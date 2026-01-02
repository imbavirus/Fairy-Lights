package me.paulf.fairylights.server.string;

import me.paulf.fairylights.FairyLights;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class StringTypes {
    private StringTypes() {}

    public static final DeferredRegister<StringType> REG = DeferredRegister.create(FairyLights.STRING_TYPE, FairyLights.ID);
    
    static {
        REG.makeRegistry(builder -> {
            // builder.disableSaving() removed in NeoForge 1.21.1
            builder.defaultKey(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "black_string"));
        });
    }

    public static final DeferredHolder<StringType,StringType> BLACK_STRING = REG.register("black_string", () -> new StringType(0x323232));

    public static final DeferredHolder<StringType,StringType> WHITE_STRING = REG.register("white_string", () -> new StringType(0xF0F0F0));
}
