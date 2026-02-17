package za.co.infernos.fairylights.server.fastener;

import za.co.infernos.fairylights.util.matrix.Matrix;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface BlockView {
    boolean isMoving(final Level world, final BlockPos source);

    Vec3 getPosition(final Level world, final BlockPos source, final Vec3 pos);

    void unrotate(final Level world, final BlockPos source, final Matrix matrix, final float delta);
}
