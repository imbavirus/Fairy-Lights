package za.co.infernos.fairylights.server.fastener;

import za.co.infernos.fairylights.server.entity.FenceFastenerEntity;
import za.co.infernos.fairylights.server.fastener.accessor.EntityFastenerAccessor;
import za.co.infernos.fairylights.server.fastener.accessor.FenceFastenerAccessor;
import net.minecraft.core.BlockPos;

public final class FenceFastener extends EntityFastener<FenceFastenerEntity> {
    public FenceFastener(final FenceFastenerEntity entity) {
        super(entity);
    }

    @Override
    public EntityFastenerAccessor<FenceFastenerEntity> createAccessor() {
        return new FenceFastenerAccessor(this);
    }

    @Override
    public BlockPos getPos() {
        return this.entity.getPos();
    }

    @Override
    public boolean isMoving() {
        return false;
    }
}
