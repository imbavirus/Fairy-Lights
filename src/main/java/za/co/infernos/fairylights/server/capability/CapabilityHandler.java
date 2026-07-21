package za.co.infernos.fairylights.server.capability;

import za.co.infernos.fairylights.FairyLights;
import za.co.infernos.fairylights.server.block.entity.FastenerBlockEntity;
import za.co.infernos.fairylights.server.entity.FenceFastenerEntity;
import za.co.infernos.fairylights.server.fastener.Fastener;
import za.co.infernos.fairylights.server.fastener.PlayerFastener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public final class CapabilityHandler {
    private CapabilityHandler() {
    }

    public static final ResourceLocation FASTENER_ID = ResourceLocation.fromNamespaceAndPath(FairyLights.ID, "fastener");

    public static void register() {
        // Attachments are registered via FLAttachments.REG on the mod bus.
    }

    public static Optional<Fastener<?>> getFastenerCapability(BlockEntity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        if (entity instanceof FastenerBlockEntity fastenerBe) {
            return fastenerBe.getFastener();
        }
        return Optional.empty();
    }

    public static Optional<Fastener<?>> getFastenerCapability(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }

        if (entity instanceof FenceFastenerEntity fenceFastener) {
            return fenceFastener.getFastener();
        }

        // Placing requires a stable PlayerFastener identity across lookups.
        if (entity instanceof Player player) {
            final PlayerFastener f = player.getData(FLAttachments.PLAYER_FASTENER.get());
            f.setWorld(player.level());
            return Optional.of(f);
        }

        return Optional.empty();
    }
}
