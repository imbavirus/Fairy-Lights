package me.paulf.fairylights.server.item;

import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.block.FastenerBlock;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.ConnectionType;
import me.paulf.fairylights.server.entity.FenceFastenerEntity;
import me.paulf.fairylights.server.fastener.BlockFastener;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.sound.FLSounds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;

public abstract class ConnectionItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();
    private final DeferredHolder<ConnectionType<?>, ? extends ConnectionType<?>> type;

    public ConnectionItem(final Properties properties, final DeferredHolder<ConnectionType<?>, ? extends ConnectionType<?>> type) {
        super(properties);
        this.type = type;
    }

    public final ConnectionType<?> getConnectionType() {
        return (ConnectionType<?>) this.type.get();
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Player user = context.getPlayer();
        if (user == null) {
            return super.useOn(context);
        }
        final Level world = context.getLevel();
        final Direction side = context.getClickedFace();
        final BlockPos clickPos = context.getClickedPos();
        final Block fastener = FLBlocks.FASTENER.get();
        final ItemStack stack = context.getItemInHand();
        if (this.isConnectionInOtherHand(world, user, stack)) {
            return InteractionResult.PASS;
        }
        final BlockState fastenerState = fastener.defaultBlockState().setValue(FastenerBlock.FACING, side);
        final BlockState currentBlockState = world.getBlockState(clickPos);
        final BlockPlaceContext blockContext = new BlockPlaceContext(context);
        final BlockPos placePos = blockContext.getClickedPos();
        if (currentBlockState.getBlock() == fastener) {
            if (!world.isClientSide()) {
                this.connect(stack, user, world, clickPos);
            }
            return InteractionResult.SUCCESS;
        } else if (blockContext.canPlace() && fastenerState.canSurvive(world, placePos)) {
            if (!world.isClientSide()) {
                this.connect(stack, user, world, placePos, fastenerState);
            }
            return InteractionResult.SUCCESS;
        } else if (isFence(currentBlockState)) {
            final HangingEntity entity = FenceFastenerEntity.findHanging(world, clickPos);
            if (entity == null || entity instanceof FenceFastenerEntity) {
                if (!world.isClientSide()) {
                    this.connectFence(stack, user, world, clickPos, (FenceFastenerEntity) entity);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private boolean isConnectionInOtherHand(final Level world, final Player user, final ItemStack stack) {
        // Capability might not be available - handle gracefully
        final Optional<Fastener<?>> attacherOpt = CapabilityHandler.getFastenerCapability(user);
        if (attacherOpt.isEmpty()) {
            return false;
        }
        final Fastener<?> attacher = attacherOpt.get();
        final Optional<Connection> connOpt = attacher.getFirstConnection();
        if (connOpt.isEmpty()) {
            return false;
        }
        final Connection connection = connOpt.get();

        // Port of upstream 1.12 behavior:
        // - If the placing connection has no logic NBT, then only block if the stack has *some* custom tag
        //   (meaning you're trying to use a mismatched connection variant).
        // - If it has logic, block unless the stack tag matches the logic.
        final CompoundTag logic = connection.serializeLogic();
        final CompoundTag stackTag = tryGetStackTag(stack);

        if (logic.isEmpty()) {
            return stackTag != null && !stackTag.isEmpty();
        }

        // If we can't read stack tag, assume mismatch (conservative).
        if (stackTag == null) {
            return true;
        }

        // Equality check using impliesNbt both ways (closest equivalent to old areNBTEquals(..., true))
        return !(me.paulf.fairylights.util.Utils.impliesNbt(logic, stackTag) && me.paulf.fairylights.util.Utils.impliesNbt(stackTag, logic));
    }

    private static CompoundTag tryGetStackTag(final ItemStack stack) {
        try {
            final java.lang.reflect.Method getTag = stack.getClass().getMethod("getTag");
            final Object tag = getTag.invoke(stack);
            return tag instanceof CompoundTag ? (CompoundTag) tag : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void connect(final ItemStack stack, final Player user, final Level world, final BlockPos pos) {
        final BlockEntity entity = world.getBlockEntity(pos);
        if (entity != null) {
            CapabilityHandler.getFastenerCapability(entity).ifPresent(fastener -> this.connect(stack, user, world, fastener));
        }
    }

    private void connect(final ItemStack stack, final Player user, final Level world, final BlockPos pos, final BlockState state) {
        if (world.setBlock(pos, state, 3)) {
            state.getBlock().setPlacedBy(world, pos, state, user, stack);
            final SoundType sound = state.getBlock().getSoundType(state, world, pos, user);
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                sound.getPlaceSound(),
                SoundSource.BLOCKS,
                (sound.getVolume() + 1) / 2,
                sound.getPitch() * 0.8F
            );
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity != null) {
                CapabilityHandler.getFastenerCapability(entity).ifPresent(destination -> this.connect(stack, user, world, destination, false));
            }
        }
    }

    public void connect(final ItemStack stack, final Player user, final Level world, final Fastener<?> fastener) {
        this.connect(stack, user, world, fastener, true);
    }

    public void connect(final ItemStack stack, final Player user, final Level world, final Fastener<?> fastener, final boolean playConnectSound) {
        CapabilityHandler.getFastenerCapability(user).ifPresent(attacher -> {
            boolean playSound = playConnectSound;
            final Optional<Connection> placing = attacher.getFirstConnection();
            if (placing.isPresent()) {
                final Connection conn = placing.get();
                final var oldDestType = conn.getDestination().getType();
                final boolean ok = conn.reconnect(fastener);
                final var newDestType = conn.getDestination().getType();
                LOGGER.info("[FairyLights] connect 2nd: user={}, ok={}, oldDest={}, newDest={}, originPos={}, newPos={}",
                    user.getGameProfile().getName(),
                    ok,
                    oldDestType,
                    newDestType,
                    conn.getFastener() != null ? conn.getFastener().getPos() : null,
                    fastener.getPos()
                );
                if (ok) {
                    conn.onConnect(world, user, stack);
                    stack.shrink(1);
                    // Ensure both ends update client-side immediately (rope rendering)
                    syncFastenerBlock(world, conn.getFastener());
                    syncFastenerBlock(world, fastener);
                } else {
                    playSound = false;
                }
            } else {
                LOGGER.info("[FairyLights] connect 1st: user={}, destPos={}", user.getGameProfile().getName(), fastener.getPos());
                // getTag() removed in 1.21.1 - use data components instead
                final CompoundTag data = new CompoundTag();
                /*
                 * Correct placement behavior:
                 * - Store the outgoing connection on the FIRST placed fastener (block/fence).
                 * - Destination is the player (so the rope appears attached to the first anchor and to your hand).
                 * - The player tracks "currently placing" via an INCOMING reference, so the 2nd click can reconnect
                 *   the SAME connection to another fastener (block-to-block).
                 */
                fastener.connect(world, attacher, this.getConnectionType(), data == null ? new CompoundTag() : data, false);
                syncFastenerBlock(world, fastener);
            }
            if (playSound) {
                final Vec3 pos = fastener.getConnectionPoint();
                world.playSound(null, pos.x, pos.y, pos.z, FLSounds.CORD_CONNECT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        });
    }

    private static void syncFastenerBlock(final Level world, final Fastener<?> fastener) {
        if (world == null || world.isClientSide() || fastener == null) {
            return;
        }
        // Only block fasteners have a BE we can block-update.
        if (!(fastener instanceof BlockFastener)) {
            return;
        }
        final BlockPos pos = fastener.getPos();
        final BlockState state = world.getBlockState(pos);
        final BlockEntity be = world.getBlockEntity(pos);
        if (be != null) {
            be.setChanged();
        }
        world.sendBlockUpdated(pos, state, state, 3);
    }

    private void connectFence(final ItemStack stack, final Player user, final Level world, final BlockPos pos, FenceFastenerEntity fastener) {
        final boolean playConnectSound;
        if (fastener == null) {
            fastener = FenceFastenerEntity.create(world, pos);
            playConnectSound = false;
        } else {
            playConnectSound = true;
        }
        this.connect(stack, user, world, CapabilityHandler.getFastenerCapability(fastener).orElseThrow(IllegalStateException::new), playConnectSound);
    }

    public static boolean isFence(final BlockState state) {
        return state.isSolid() && state.is(BlockTags.FENCES);
    }
}
