package za.co.infernos.fairylights.server.capability;

import za.co.infernos.fairylights.FairyLights;
import za.co.infernos.fairylights.server.block.entity.FastenerBlockEntity;
import za.co.infernos.fairylights.server.fastener.Fastener;
import za.co.infernos.fairylights.server.fastener.PlayerFastener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.WeakHashMap;
import za.co.infernos.fairylights.server.entity.FenceFastenerEntity;

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

    // Helper method to get fastener capability from BlockEntity
    public static Optional<Fastener<?>> getFastenerCapability(BlockEntity entity) {
        if (entity == null) return Optional.empty();

        // NeoForge 1.21.x: our fastener is stored directly on the BE
        if (entity instanceof FastenerBlockEntity fastenerBe) {
            return fastenerBe.getFastener();
        }

        // No other block entity types have fastener capabilities
        return Optional.empty();
    }

    // Helper method to get fastener capability from Entity
    // Creates the capability if it doesn't exist for Players (lazy initialization)
    public static Optional<Fastener<?>> getFastenerCapability(Entity entity) {
        if (entity == null) return Optional.empty();

        if (entity instanceof FenceFastenerEntity fenceFastener) {
            return fenceFastener.getFastener();
        }

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
        
        // No other entity types have fastener capabilities
        return Optional.empty();
    }
}
