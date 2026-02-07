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
    private final DeferredHolder<ConnectionType<?>, ? extends ConnectionType<?>> type;

    public ConnectionItem(final Properties properties,
            final DeferredHolder<ConnectionType<?>, ? extends ConnectionType<?>> type) {
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
        org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: useOn called at {} with stack {}", clickPos, stack.getItem());
        if (this.isConnectionInOtherHand(world, user, stack)) {
            org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: useOn - blocked by isConnectionInOtherHand, returning PASS");
            return InteractionResult.PASS;
        }
        org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: useOn - not blocked, proceeding with placement");
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
            org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: isConnectionInOtherHand - no attacher capability");
            return false;
        }
        final Fastener<?> attacher = attacherOpt.get();
        final Optional<Connection> connOpt = attacher.getFirstConnection();
        if (connOpt.isEmpty()) {
            org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: isConnectionInOtherHand - no connection in attacher");
            return false;
        }
        final Connection connection = connOpt.get();
        
        org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: isConnectionInOtherHand - found connection {}, isRemoved={}", 
                connection.getUUID(), connection.isRemoved());
        
        // If the connection is removed (e.g., fastener was broken), ignore it and allow placement
        if (connection.isRemoved()) {
            org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: isConnectionInOtherHand - connection is removed, allowing placement");
            return false;
        }
        
        // Also check if the connection's destination fastener is still valid
        // If the destination was a block fastener that was broken, the connection should be invalid
        final Optional<Fastener<?>> destFastenerOpt = connection.getDestination().get(world, false);
        if (destFastenerOpt.isEmpty()) {
            // Destination fastener no longer exists (e.g., block was broken), ignore this connection
            org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: isConnectionInOtherHand - destination fastener missing, allowing placement");
            return false;
        }
        
        org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: isConnectionInOtherHand - connection and destination valid, checking match");

        // First check if connection types match - if they do, allow reconnecting to new destination
        // This allows placing at different distances even if there's an existing connection
        if (connection.getType() == this.getConnectionType()) {
            org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: isConnectionInOtherHand - connection types match, allowing reconnection");
            return false; // Allow placement - connect() will handle reconnecting
        }

        // Port of upstream 1.12 behavior:
        // - If the placing connection has no logic NBT, then only block if the stack
        // has *some* custom tag
        // (meaning you're trying to use a mismatched connection variant).
        // - If it has logic, block unless the stack tag matches the logic.
        final CompoundTag logic = connection.serializeLogic();
        this.removeBackups(logic);
        final CompoundTag stackTag = getData(stack);

        if (stackTag == null) {
            // Should not happen if getData returns null for empty, but for safety
        }

        // Fix: If logic doesn't track color, ignore stack's color to prevent mismatch
        if (stackTag != null && !logic.contains("color") && stackTag.contains("color")) {
            stackTag.remove("color");
        }

        if (logic.isEmpty()) {
            return stackTag != null && !stackTag.isEmpty();
        }

        // If we can't read stack tag, check if connection has only default values
        if (stackTag == null || stackTag.isEmpty()) {
            // Check if connection has only default values by examining the serialized logic
            // For tinsel: default color is LIGHT_GRAY (0xC8C8C8) - check if logic only has
            // default color
            if (connection instanceof me.paulf.fairylights.server.connection.GarlandTinselConnection) {
                final int defaultColor = me.paulf.fairylights.server.item.DyeableItem
                        .getColor(net.minecraft.world.item.DyeColor.LIGHT_GRAY);
                if (logic.contains("color", net.minecraft.nbt.Tag.TAG_INT) && logic.getInt("color") == defaultColor
                        && logic.size() == 1) {
                    // Connection has only default color, empty stack should match
                    return false;
                }
            } else if (connection instanceof me.paulf.fairylights.server.connection.PennantBuntingConnection) {
                // Default pattern is empty list, default text is empty
                final boolean hasEmptyPattern = !logic.contains("pattern", net.minecraft.nbt.Tag.TAG_LIST) ||
                        (logic.contains("pattern", net.minecraft.nbt.Tag.TAG_LIST)
                                && logic.getList("pattern", net.minecraft.nbt.Tag.TAG_COMPOUND).isEmpty());
                // Relaxed text check: StyledString might verify as empty if value is empty
                // string
                boolean hasEmptyText = true;
                if (logic.contains("text", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                    final CompoundTag textTag = logic.getCompound("text");
                    // If it has "value", check if that is empty string
                    if (textTag.contains("value", net.minecraft.nbt.Tag.TAG_STRING)) {
                        hasEmptyText = textTag.getString("value").isEmpty();
                    }
                    // If it doesn't have "value", it might be invalid/empty structure, so assume
                    // empty/default
                }

                if (hasEmptyPattern && hasEmptyText) {
                    // Connection has only default values, empty stack should match
                    return false;
                }
            } else if (connection instanceof me.paulf.fairylights.server.connection.HangingLightsConnection) {
                // Default string is BLACK_STRING, default pattern is empty list
                final boolean hasDefaultString = logic.contains("string", net.minecraft.nbt.Tag.TAG_STRING) &&
                        logic.getString("string").equals("fairylights:black_string");
                final boolean hasEmptyPattern = !logic.contains("pattern", net.minecraft.nbt.Tag.TAG_LIST) ||
                        (logic.contains("pattern", net.minecraft.nbt.Tag.TAG_LIST)
                                && logic.getList("pattern", net.minecraft.nbt.Tag.TAG_COMPOUND).isEmpty());
                if (hasDefaultString && hasEmptyPattern && logic.size() == 2) {
                    // Connection has only default values, empty stack should match
                    return false;
                }
            } else if (connection instanceof me.paulf.fairylights.server.connection.LetterBuntingConnection) {
                // Check if text has no characters (StyledString serializes to value="",
                // styling=[])
                final boolean hasEmptyText = !logic.contains("text", net.minecraft.nbt.Tag.TAG_COMPOUND) ||
                        (logic.contains("text", net.minecraft.nbt.Tag.TAG_COMPOUND) &&
                                (logic.getCompound("text").isEmpty() ||
                                        (logic.getCompound("text").contains("value", net.minecraft.nbt.Tag.TAG_STRING)
                                                &&
                                                logic.getCompound("text").getString("value").isEmpty())));

                if (hasEmptyText) {
                    // Connection has only default values, empty stack should match
                    return false;
                }
            } else if (connection instanceof me.paulf.fairylights.server.connection.GarlandVineConnection) {
                // Vine garland has no custom logic - always match with empty stack
                if (logic.isEmpty()) {
                    return false;
                }
            }
            // Connection has non-default values, but stack is empty - mismatch
            return true;
        }

        // Equality check using impliesNbt both ways (closest equivalent to old
        // areNBTEquals(..., true))
        final boolean logicImpliesStack = me.paulf.fairylights.util.Utils.impliesNbt(logic, stackTag);
        final boolean stackImpliesLogic = me.paulf.fairylights.util.Utils.impliesNbt(stackTag, logic);
        final boolean matches = logicImpliesStack && stackImpliesLogic;
        return !matches;
    }

    private static CompoundTag getData(final ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(FLDataComponents.CONNECTION_LOGIC.get(), new CompoundTag()).copy();
        if (stack.has(FLDataComponents.COLOR.get()) && !tag.contains("color")) {
            tag.putInt("color", stack.get(FLDataComponents.COLOR.get()));
        }
        if (stack.has(FLDataComponents.STYLED_STRING.get()) && !tag.contains("text")) {
            tag.put("text", stack.get(FLDataComponents.STYLED_STRING.get()));
        }
        return tag.isEmpty() ? null : tag;
    }

    private void removeBackups(final net.minecraft.nbt.Tag tag) {
        if (tag instanceof CompoundTag compound) {
            compound.remove("fl_backup_color");
            for (final String key : compound.getAllKeys()) {
                removeBackups(compound.get(key));
            }
        } else if (tag instanceof net.minecraft.nbt.ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                removeBackups(list.get(i));
            }
        }
    }

    private void connect(final ItemStack stack, final Player user, final Level world, final BlockPos pos) {
        final BlockEntity entity = world.getBlockEntity(pos);
        if (entity != null) {
            CapabilityHandler.getFastenerCapability(entity)
                    .ifPresent(fastener -> this.connect(stack, user, world, fastener));
        }
    }

    private void connect(final ItemStack stack, final Player user, final Level world, final BlockPos pos,
            final BlockState state) {
        if (world.setBlock(pos, state, 3)) {
            state.getBlock().setPlacedBy(world, pos, state, user, stack);
            final SoundType sound = state.getBlock().getSoundType(state, world, pos, user);
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    sound.getPlaceSound(),
                    SoundSource.BLOCKS,
                    (sound.getVolume() + 1) / 2,
                    sound.getPitch() * 0.8F);
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity != null) {
                CapabilityHandler.getFastenerCapability(entity)
                        .ifPresent(destination -> this.connect(stack, user, world, destination, false));
            }
        }
    }

    public void connect(final ItemStack stack, final Player user, final Level world, final Fastener<?> fastener) {
        this.connect(stack, user, world, fastener, true);
    }

    public void connect(final ItemStack stack, final Player user, final Level world, final Fastener<?> fastener,
            final boolean playConnectSound) {
        CapabilityHandler.getFastenerCapability(user).ifPresent(attacher -> {
            boolean playSound = playConnectSound;
            final Optional<Connection> placing = attacher.getFirstConnection();
            if (placing.isPresent()) {
                final Connection conn = placing.get();
                // Check if the connection's old destination still exists
                final Optional<Fastener<?>> oldDestOpt = conn.getDestination().get(world, false);
                if (oldDestOpt.isPresent()) {
                    // Old destination exists, try to reconnect
                    final boolean ok = conn.reconnect(fastener);
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
                    // Old destination no longer exists (e.g., block was broken)
                    // Remove the old connection and create a new one
                    org.apache.logging.log4j.LogManager.getLogger().info("FL_DEBUG: connect - old destination gone, removing old connection and creating new");
                    attacher.removeConnection(conn);
                    // Fall through to create new connection
                    final CompoundTag data = getData(stack);
                    final ConnectionType<? extends Connection> type = this.getConnectionType();
                    final Connection newConn = attacher.connect(world, fastener, type, data == null ? new CompoundTag() : data, true);
                    newConn.onConnect(world, user, stack);
                    stack.shrink(1);
                    syncFastenerBlock(world, attacher);
                    syncFastenerBlock(world, fastener);
                }
            } else {
                // Get stack NBT so connection logic matches on second placement
                // For connections with default values (tinsel color, pennant pattern), we need
                // to pass the stack's NBT
                final CompoundTag data = getData(stack);
                if (data == null) {
                    // Create empty tag if stack has no NBT
                    final CompoundTag empty = new CompoundTag();
                    fastener.connect(world, attacher, this.getConnectionType(), empty, false);
                } else {
                    // Copy stack NBT so connection can deserialize it
                    final CompoundTag dataCopy = data.copy();
                    fastener.connect(world, attacher, this.getConnectionType(), dataCopy, false);
                }
                /*
                 * Correct placement behavior:
                 * - Store the outgoing connection on the FIRST placed fastener (block/fence).
                 * - Destination is the player (so the rope appears attached to the first anchor
                 * and to your hand).
                 * - The player tracks "currently placing" via an INCOMING reference, so the 2nd
                 * click can reconnect
                 * the SAME connection to another fastener (block-to-block).
                 */
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

    private void connectFence(final ItemStack stack, final Player user, final Level world, final BlockPos pos,
            FenceFastenerEntity fastener) {
        final boolean playConnectSound;
        if (fastener == null) {
            fastener = FenceFastenerEntity.create(world, pos);
            playConnectSound = false;
        } else {
            playConnectSound = true;
        }
        this.connect(stack, user, world,
                CapabilityHandler.getFastenerCapability(fastener).orElseThrow(IllegalStateException::new),
                playConnectSound);
    }

    public static boolean isFence(final BlockState state) {
        return state.isSolid() && state.is(BlockTags.FENCES);
    }
}
