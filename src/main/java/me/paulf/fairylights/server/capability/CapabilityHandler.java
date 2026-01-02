package me.paulf.fairylights.server.capability;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.fastener.Fastener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public final class CapabilityHandler {
    private CapabilityHandler() {}

    public static final ResourceLocation FASTENER_ID = ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "fastener");

    // In NeoForge 1.21.1, capabilities are accessed via ResourceLocation directly
    // The FASTENER_ID is used with AttachCapabilitiesEvent and getCapability

    public static void register() {
        // Capabilities are registered via AttachCapabilitiesEvent, not here
    }

    // Helper method to get capability from BlockEntity using reflection (for compatibility)
    @SuppressWarnings("unchecked")
    public static Optional<Fastener<?>> getFastenerCapability(BlockEntity entity) {
        if (entity == null) return Optional.empty();
        try {
            final java.lang.reflect.Method getCapability = entity.getClass().getMethod("getCapability", ResourceLocation.class);
            final Optional<Fastener<?>> cap = (Optional<Fastener<?>>) getCapability.invoke(entity, FASTENER_ID);
            return cap != null ? cap : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Helper method to get capability from Entity using reflection (for compatibility)
    @SuppressWarnings("unchecked")
    public static Optional<Fastener<?>> getFastenerCapability(Entity entity) {
        if (entity == null) return Optional.empty();
        try {
            final java.lang.reflect.Method getCapability = entity.getClass().getMethod("getCapability", ResourceLocation.class);
            final Optional<Fastener<?>> cap = (Optional<Fastener<?>>) getCapability.invoke(entity, FASTENER_ID);
            return cap != null ? cap : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
