package me.paulf.fairylights.server.capability;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.block.entity.FastenerBlockEntity;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.PlayerFastener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.WeakHashMap;

public final class CapabilityHandler {
    private CapabilityHandler() {}

    public static final ResourceLocation FASTENER_ID = ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "fastener");
    
    // Temporary storage for player fasteners until proper data attachments are implemented
    private static final WeakHashMap<Player, PlayerFastener> playerFasteners = new WeakHashMap<>();

    // In NeoForge 1.21.1, capabilities are accessed via ResourceLocation directly
    // The FASTENER_ID is used with AttachCapabilitiesEvent and getCapability

    public static void register() {
        // Capabilities are registered via AttachCapabilitiesEvent, not here
    }

    // Helper method to get capability from BlockEntity using reflection (for compatibility)
    @SuppressWarnings("unchecked")
    public static Optional<Fastener<?>> getFastenerCapability(BlockEntity entity) {
        if (entity == null) return Optional.empty();

        // NeoForge 1.21.x: our fastener is stored directly on the BE
        if (entity instanceof FastenerBlockEntity fastenerBe) {
            return fastenerBe.getFastener();
        }

        // Legacy / fallback: try old-style reflection (kept for compatibility with other entities)
        try {
            final java.lang.reflect.Method getCapability = entity.getClass().getMethod("getCapability", ResourceLocation.class);
            final Optional<Fastener<?>> cap = (Optional<Fastener<?>>) getCapability.invoke(entity, FASTENER_ID);
            return cap != null ? cap : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Helper method to get capability from Entity using NeoForge 1.21.1 data attachments
    // Creates the capability if it doesn't exist (lazy initialization)
    @SuppressWarnings("unchecked")
    public static Optional<Fastener<?>> getFastenerCapability(Entity entity) {
        if (entity == null) return Optional.empty();

        // IMPORTANT: The "placing" flow relies on a persistent PlayerFastener instance.
        // If we create a new PlayerFastener on each lookup, the player will never "remember" the first placement,
        // reconnect will never happen, and the player remains tethered to every placed fastener.
        //
        // Until proper data attachments are implemented, always use the WeakHashMap-backed instance for players.
        if (entity instanceof Player player) {
            final PlayerFastener f = playerFasteners.computeIfAbsent(player, PlayerFastener::new);
            // Keep world set (important for resolving incoming/outgoing connections)
            f.setWorld(player.level());
            return Optional.of(f);
        }
        
        // Try multiple methods in order of likelihood
        // 1. Try getData(ResourceLocation) - some NeoForge versions use this
        try {
            final java.lang.reflect.Method getData = entity.getClass().getMethod("getData", ResourceLocation.class);
            Object data = getData.invoke(entity, FASTENER_ID);
            if (data instanceof Fastener) {
                return Optional.of((Fastener<?>) data);
            }
            // If data is null and entity is a Player, create and set it
            if (data == null && entity instanceof Player) {
                final PlayerFastener fastener = new PlayerFastener((Player) entity);
                // Try to set the data
                try {
                    final java.lang.reflect.Method setData = entity.getClass().getMethod("setData", ResourceLocation.class, Object.class);
                    setData.invoke(entity, FASTENER_ID, fastener);
                    return Optional.of(fastener);
                } catch (Exception e2) {
                    // setData doesn't exist, fall through
                }
            }
        } catch (Exception e) {
            // Method doesn't exist or failed, try next
        }
        // 2. Try getCapability(ResourceLocation) - old API
        try {
            final java.lang.reflect.Method getCapability = entity.getClass().getMethod("getCapability", ResourceLocation.class);
            Optional<Fastener<?>> cap = (Optional<Fastener<?>>) getCapability.invoke(entity, FASTENER_ID);
            if (cap != null && cap.isPresent()) {
                return cap;
            }
            // If capability is empty and entity is a Player, create and attach it
            if ((cap == null || cap.isEmpty()) && entity instanceof Player) {
                final PlayerFastener fastener = new PlayerFastener((Player) entity);
                // Try to attach the capability using addCapability or similar
                try {
                    final java.lang.reflect.Method addCapability = entity.getClass().getMethod("addCapability", ResourceLocation.class, Object.class);
                    addCapability.invoke(entity, FASTENER_ID, fastener);
                    return Optional.of(fastener);
                } catch (Exception e2) {
                    // addCapability doesn't exist, try getOrCreate
                    try {
                        final java.lang.reflect.Method getOrCreate = entity.getClass().getMethod("getOrCreateCapability", ResourceLocation.class, java.util.function.Supplier.class);
                        cap = (Optional<Fastener<?>>) getOrCreate.invoke(entity, FASTENER_ID, (java.util.function.Supplier<Fastener<?>>) () -> fastener);
                        return cap != null ? cap : Optional.of(fastener);
                    } catch (Exception e3) {
                        // All attachment methods failed
                    }
                }
            }
        } catch (Exception e) {
            // Method doesn't exist or failed
        }
        return Optional.empty();
    }
}
