package me.paulf.fairylights.server.fastener;

import me.paulf.fairylights.server.capability.CapabilityHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
// ICapabilityProvider removed in NeoForge 1.21.1 - using direct capability access
import net.neoforged.bus.api.Event;

import java.util.ConcurrentModificationException;
import java.util.Set;

public class CollectFastenersEvent extends Event {
    private final Level world;

    private final AABB region;

    private final Set<Fastener<?>> fasteners;

    public CollectFastenersEvent(final Level world, final AABB region, final Set<Fastener<?>> fasteners) {
        this.world = world;
        this.region = region;
        this.fasteners = fasteners;
    }

    public Level getWorld() {
        return this.world;
    }

    public AABB getRegion() {
        return this.region;
    }

    public void accept(final LevelChunk chunk) {
        try {
            for (final BlockEntity entity : chunk.getBlockEntities().values()) {
                this.accept(entity);
            }
        } catch (final ConcurrentModificationException e) {
            // RenderChunk's may find an invalid block entity while building and trigger a remove not on main thread
        }
    }

    public void accept(final net.minecraft.world.entity.Entity entity) {
        CapabilityHandler.getFastenerCapability(entity).ifPresent(this::accept);
    }
    
    public void accept(final net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        CapabilityHandler.getFastenerCapability(blockEntity).ifPresent(this::accept);
    }

    public void accept(final Fastener<?> fastener) {
        if (this.region.contains(fastener.getConnectionPoint())) {
            this.fasteners.add(fastener);
        }
    }
}
