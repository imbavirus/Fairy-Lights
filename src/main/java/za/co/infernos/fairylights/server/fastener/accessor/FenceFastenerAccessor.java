package za.co.infernos.fairylights.server.fastener.accessor;

import za.co.infernos.fairylights.server.entity.FenceFastenerEntity;
import za.co.infernos.fairylights.server.fastener.EntityFastener;
import za.co.infernos.fairylights.server.fastener.FastenerType;

public final class FenceFastenerAccessor extends EntityFastenerAccessor<FenceFastenerEntity> {
    public FenceFastenerAccessor() {
        super(FenceFastenerEntity.class);
    }

    public FenceFastenerAccessor(final EntityFastener<FenceFastenerEntity> fastener) {
        super(FenceFastenerEntity.class, fastener);
    }

    @Override
    public FastenerType getType() {
        return FastenerType.FENCE;
    }
}
