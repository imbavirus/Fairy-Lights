package za.co.infernos.fairylights.server.capability;

import za.co.infernos.fairylights.FairyLights;
import za.co.infernos.fairylights.server.fastener.PlayerFastener;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Identity-stable player fastener storage. Placing connections require the same
 * PlayerFastener instance across lookups; WeakHashMap was fragile under GC/reload.
 */
public final class FLAttachments {
    private FLAttachments() {
    }

    public static final DeferredRegister<AttachmentType<?>> REG =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, FairyLights.ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerFastener>> PLAYER_FASTENER =
            REG.register("player_fastener", () -> AttachmentType.builder(holder -> {
                if (!(holder instanceof Player player)) {
                    throw new IllegalStateException("PlayerFastener attachment is only valid on players");
                }
                final PlayerFastener fastener = new PlayerFastener(player);
                fastener.setWorld(player.level());
                return fastener;
            }).build());
}
