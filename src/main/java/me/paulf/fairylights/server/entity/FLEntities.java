package me.paulf.fairylights.server.entity;

import me.paulf.fairylights.FairyLights;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class FLEntities {
    private FLEntities() {}

    public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(Registries.ENTITY_TYPE, FairyLights.ID);

    public static final DeferredHolder<EntityType<?>, EntityType<FenceFastenerEntity>> FASTENER = REG.register("fastener", () ->
        EntityType.Builder.<FenceFastenerEntity>of(FenceFastenerEntity::new, MobCategory.MISC)
            .sized(1.15F, 2.8F)
            .setTrackingRange(10)
            .setUpdateInterval(Integer.MAX_VALUE)
            .setShouldReceiveVelocityUpdates(false)
            // setCustomClientFactory() removed in NeoForge 1.21.1
            .build(FairyLights.ID + ":fastener")
    );
}
