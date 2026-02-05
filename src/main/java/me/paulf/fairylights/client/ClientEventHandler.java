package me.paulf.fairylights.client;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.paulf.fairylights.server.collision.Collidable;
import me.paulf.fairylights.server.collision.Intersection;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.HangingLightsConnection;
import me.paulf.fairylights.server.connection.PlayerAction;
import me.paulf.fairylights.server.feature.light.Light;
import me.paulf.fairylights.server.entity.FenceFastenerEntity;
import me.paulf.fairylights.server.fastener.CollectFastenersEvent;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.FastenerType;
import me.paulf.fairylights.server.block.entity.FastenerBlockEntity;
import me.paulf.fairylights.server.jingle.Jingle;
import me.paulf.fairylights.util.Curve;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.client.gui.Gui;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.client.renderer.block.entity.FastenerRenderer;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public final class ClientEventHandler {
    private static final float HIGHLIGHT_ALPHA = 0.4F;

    public static final java.util.Set<java.util.UUID> RENDERED_CONNECTIONS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static int lastFrame = -1;

    private static FastenerRenderer fastenerRenderer;

    @Nullable
    public static Connection getHitConnection() {
        final net.minecraft.world.phys.HitResult result = Minecraft.getInstance().hitResult;
        if (result instanceof EntityHitResult) {
            final Entity entity = ((EntityHitResult) result).getEntity();
            if (entity instanceof HitConnection) {
                return ((HitConnection) entity).result.connection;
            }
        }
        return null;
    }

    public void renderOverlay(final Gui gui, final GuiGraphics poseStack, final float partialTick, final int screenWidth, final int screenHeight) {
        final Connection conn = getHitConnection();
        if (!(conn instanceof HangingLightsConnection)) {
            return;
        }
        final Jingle jingle = ((HangingLightsConnection) conn).getPlayingJingle();
        if (jingle == null) {
            return;
        }
        final List<String> lines = List.of(
            "Song: " + jingle.getTitle(),
            "Artist: " + jingle.getArtist());
        final var font = Minecraft.getInstance().font;
        for (int i = 0; i < lines.size(); i++) {
            final String line = lines.get(i);
            if (!Strings.isNullOrEmpty(line)) {
                final int lineHeight = font.lineHeight;
                final int textWidth = font.width(line);
                final int y = 2 + lineHeight * i;
                poseStack.fill(1, y - 1, 2 + textWidth + 1, y + lineHeight - 1, 0x90505050);
                poseStack.drawString(font, line, 2, y, 0xe0e0e0);
            }
        }
    }

    private static HitConnection lastHitConnection = null;
    private static String lastComponentDescription = null;
    
    private static String getComponentDescription(final HitResult result) {
        // Check if it's a CORD feature (rope/wire) by comparing with Connection.CORD_FEATURE
        final boolean isCord = result.intersection.getFeatureType() == me.paulf.fairylights.server.connection.Connection.CORD_FEATURE;
        
        // For FEATURE type, we can detect it by checking if the feature is a Light
        final boolean isFeature = result.intersection.getFeature() instanceof Light;
        
        if (result.connection instanceof HangingLightsConnection) {
            final HangingLightsConnection conn = (HangingLightsConnection) result.connection;
            if (isFeature) {
                // Individual light
                final int lightIndex = result.intersection.getFeature().getId();
                final Light<?>[] lights = conn.getFeatures();
                if (lightIndex >= 0 && lightIndex < lights.length) {
                    final Light<?> light = lights[lightIndex];
                    final ItemStack lightItem = light.getItem();
                    final String lightName = lightItem.isEmpty() ? "Empty" : lightItem.getDisplayName().getString();
                    return String.format("LIGHT[%d]: %s", lightIndex, lightName);
                }
                return String.format("LIGHT[%d]", lightIndex);
            } else if (isCord) {
                return "ROPE/WIRE";
            }
        }
        // Fallback for other connection types
        final String typeName = isCord ? "CORD" : (isFeature ? "FEATURE" : ("TYPE_" + result.intersection.getFeatureType().getId()));
        return typeName + "[" + result.intersection.getFeature().getId() + "]";
    }
    
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static int tickCounter = 0;
    
    public static void updateHitConnection() {
        tickCounter++;
        // Log every 20 ticks (1 second) to confirm method is being called
        if (tickCounter % 20 == 0) {
            LOGGER.info("FL_DEBUG: updateHitConnection() called (tick " + tickCounter + ")");
        }
        
        final Minecraft mc = Minecraft.getInstance();
        final Entity viewer = mc.getCameraEntity();
        if (mc.hitResult != null && mc.level != null && viewer != null) {
            // First check for fastener entities/blocks
            if (mc.hitResult instanceof EntityHitResult entityHit) {
                final Entity entity = entityHit.getEntity();
                if (entity instanceof FenceFastenerEntity) {
                    final String desc = "FASTENER[FENCE]";
                    if (!desc.equals(lastComponentDescription)) {
                        LOGGER.info("FL_DEBUG: MOUSEOVER - " + desc + " at " + entityHit.getLocation());
                        lastComponentDescription = desc;
                        lastHitConnection = null;
                    }
                    return;
                }
            }
            
            // Check for block fasteners
            if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                final var blockState = mc.level.getBlockState(blockHit.getBlockPos());
                if (blockState.getBlock() == me.paulf.fairylights.server.block.FLBlocks.FASTENER.get()) {
                    final String desc = "FASTENER[BLOCK] at " + blockHit.getBlockPos();
                    if (!desc.equals(lastComponentDescription)) {
                        LOGGER.info("FL_DEBUG: MOUSEOVER - " + desc);
                        lastComponentDescription = desc;
                        lastHitConnection = null;
                    }
                    return;
                }
            }
            
            // Check for connection hits
            final HitResult result = getHitConnection(mc.level, viewer);
            if (result != null) {
                final Vec3 eyes = viewer.getEyePosition(1.0F);
                if (result.intersection.getResult().distanceTo(eyes) < mc.hitResult.getLocation().distanceTo(eyes)) {
                    final HitConnection hitConnection = new HitConnection(mc.level, result);
                    mc.hitResult = new EntityHitResult(hitConnection);
                    mc.crosshairPickEntity = null;
                    currentHoveredHitConnection = hitConnection; // Store for click handling
                    
                    // Track mouseover changes with detailed component description
                    final String componentDesc = getComponentDescription(result);
                    if (lastHitConnection == null || !lastHitConnection.result.connection.getUUID().equals(result.connection.getUUID()) || 
                        lastHitConnection.result.intersection.getFeature().getId() != result.intersection.getFeature().getId() ||
                        !componentDesc.equals(lastComponentDescription)) {
                        LOGGER.info("FL_DEBUG: MOUSEOVER - Connection: " + result.connection.getUUID() + 
                            " Component: " + componentDesc +
                            " FeatureType=" + result.intersection.getFeatureType().getId() + 
                            " FeatureId=" + result.intersection.getFeature().getId() +
                            " HitPos=" + result.intersection.getResult());
                        lastHitConnection = hitConnection;
                        lastComponentDescription = componentDesc;
                    }
                } else {
                    // No longer hovering over a connection
                    if (lastHitConnection != null) {
                        LOGGER.info("FL_DEBUG: MOUSEOVER - No longer over connection (block/entity closer)");
                        lastHitConnection = null;
                        lastComponentDescription = null;
                        currentHoveredHitConnection = null;
                    }
                }
            } else {
                // No connection found in raycast
                if (lastHitConnection != null) {
                    LOGGER.info("FL_DEBUG: MOUSEOVER - No connection found in raycast");
                    lastHitConnection = null;
                    lastComponentDescription = null;
                    currentHoveredHitConnection = null;
                }
            }
        }
    }

    @Nullable
    private static HitResult getHitConnection(final Level world, final Entity viewer) {
        final AABB bounds = new AABB(viewer.blockPosition()).inflate(Connection.MAX_LENGTH + 1.0D);
        final Set<Fastener<?>> fasteners = collectFasteners(world, bounds);
        final HitResult result = getHitConnection(viewer, bounds, fasteners);
        if (result == null && fasteners.size() > 0) {
            // Debug: log when we have fasteners but no hit
            System.out.println("FL_DEBUG: Raycast found " + fasteners.size() + " fasteners but no connection hit");
        }
        return result;
    }

    private static Set<Fastener<?>> collectFasteners(final Level world, final AABB bounds) {
        final Set<Fastener<?>> fasteners = Sets.newLinkedHashSet();
        final CollectFastenersEvent event = new CollectFastenersEvent(world, bounds, fasteners);
        world.getEntitiesOfClass(FenceFastenerEntity.class, bounds)
            .forEach(event::accept);
        final int minX = Mth.floor(bounds.minX / 16.0D);
        final int maxX = Mth.ceil(bounds.maxX / 16.0D);
        final int minZ = Mth.floor(bounds.minZ / 16.0D);
        final int maxZ = Mth.ceil(bounds.maxZ / 16.0D);
        final ChunkSource provider = world.getChunkSource();
        int chunkCount = 0;
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                final LevelChunk chunk = provider.getChunk(x, z, false);
                if (chunk != null) {
                    event.accept(chunk);
                    chunkCount++;
                }
            }
        }
        NeoForge.EVENT_BUS.post(event);

        return fasteners;
    }

    @Nullable
    private static HitResult getHitConnection(final Entity viewer, final AABB bounds, final Set<Fastener<?>> fasteners) {
        if (fasteners.isEmpty()) {
            return null;
        }
        final Vec3 origin = viewer.getEyePosition(1);
        final Vec3 look = viewer.getLookAngle();
        // getPickRange() removed in 1.21.1 - using constant reach distance
        final double reach = 6.0; // Default player reach distance
        final Vec3 end = origin.add(look.x * reach, look.y * reach, look.z * reach);
        Connection found = null;
        Intersection rayTrace = null;
        double distance = Double.MAX_VALUE;
        int connectionCount = 0;
        int intersectionCount = 0;
        for (final Fastener<?> fastener : fasteners) {
            for (final Connection connection : fastener.getOwnConnections()) {
                connectionCount++;
                if (connection.getDestination().getType() == FastenerType.PLAYER) {
                    continue;
                }
                final Collidable collision = connection.getCollision();
                final Intersection result = collision.intersect(origin, end);
                if (result != null) {
                    intersectionCount++;
                    final double dist = result.getResult().distanceTo(origin);
                    if (dist < distance) {
                        distance = dist;
                        found = connection;
                        rayTrace = result;
                    }
                }
            }
        }
        // Debug logging for raycast results (only log occasionally to avoid spam)
        if (connectionCount > 0 && found == null && tickCounter % 100 == 0) {
            LOGGER.info("FL_DEBUG: Raycast checked " + connectionCount + " connections, " + intersectionCount + " intersections, but none were closest");
        }
        if (found == null) {
            return null;
        }
        return new HitResult(found, rayTrace);
    }

    @SubscribeEvent
    public void drawBlockHighlight(final net.neoforged.neoforge.client.event.RenderHighlightEvent.Block event) {
        // TODO: Check if Entity event exists or needs different approach
        // For now, using Block event as placeholder
        return;
    }
    
    // TODO: Entity highlight rendering - RenderHighlightEvent.Entity may not exist in NeoForge 1.21.1
    // The original implementation is commented out until we find the correct event
    
    @SubscribeEvent
    public void onPlayerInteract(final net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        // Handle interaction with HitConnection entities directly
        if (event.getTarget() instanceof HitConnection hitConnection) {
            LOGGER.info("FL_DEBUG: EntityInteract event caught for HitConnection");
            hitConnection.processAction(PlayerAction.INTERACT);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    
    @SubscribeEvent
    public void onPlayerAttack(final net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        // Handle attack on HitConnection entities directly
        if (event.getTarget() instanceof HitConnection hitConnection) {
            LOGGER.info("FL_DEBUG: AttackEntityEvent caught for HitConnection");
            hitConnection.processAction(PlayerAction.ATTACK);
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onRightClickEntity(final net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific event) {
        // Alternative event for entity interactions
        if (event.getTarget() instanceof HitConnection hitConnection) {
            LOGGER.info("FL_DEBUG: EntityInteractSpecific event caught for HitConnection");
            hitConnection.processAction(PlayerAction.INTERACT);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    
    // Store the last HitConnection we're hovering over for direct click handling
    private static HitConnection currentHoveredHitConnection = null;
    
    @SubscribeEvent
    public void onRightClickEmpty(final net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickEmpty event) {
        // Check if we're hovering over a HitConnection
        final Minecraft mc = Minecraft.getInstance();
        HitConnection target = null;
        if (mc.hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof HitConnection hitConnection) {
            target = hitConnection;
        } else if (currentHoveredHitConnection != null) {
            target = currentHoveredHitConnection;
        }
        if (target != null) {
            LOGGER.info("FL_DEBUG: RightClickEmpty event - processing HitConnection interaction");
            target.processAction(PlayerAction.INTERACT);
        }
    }
    
    @SubscribeEvent
    public void onRightClickItem(final net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
        // Check if we're hovering over a HitConnection
        final Minecraft mc = Minecraft.getInstance();
        HitConnection target = null;
        if (mc.hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof HitConnection hitConnection) {
            target = hitConnection;
        } else if (currentHoveredHitConnection != null) {
            target = currentHoveredHitConnection;
        }
        if (target != null) {
            LOGGER.info("FL_DEBUG: RightClickItem event - processing HitConnection interaction");
            target.processAction(PlayerAction.INTERACT);
        }
    }
    /*
    @SubscribeEvent
    public void drawEntityHighlight(final RenderHighlightEvent.Entity event) {
        final Entity entity = event.getTarget().getEntity();
        final Vec3 pos = event.getCamera().getPosition();
        final MultiBufferSource buf = event.getMultiBufferSource();
        if (entity instanceof FenceFastenerEntity) {
            this.drawFenceFastenerHighlight((FenceFastenerEntity) entity, event.getPoseStack(), buf.getBuffer(RenderType.lines()), event.getPartialTick(), pos.x, pos.y, pos.z);
        } else if (entity instanceof final HitConnection hit) {
            if (hit.result.intersection.getFeatureType() == Connection.CORD_FEATURE) {
                final PoseStack matrix = event.getPoseStack();
                matrix.pushPose();
                final Vec3 p = hit.result.connection.getFastener().getConnectionPoint();
                matrix.translate(p.x - pos.x, p.y - pos.y, p.z - pos.z);
                this.renderHighlight(hit.result.connection, matrix, buf.getBuffer(RenderType.lines()));
                matrix.popPose();
            } else {
                final AABB bb = hit.result.intersection.getHitBox().move(-pos.x, -pos.y, -pos.z).inflate(0.002D);
                LevelRenderer.renderLineBox(event.getPoseStack(), buf.getBuffer(RenderType.lines()), bb, 0.0F, 0.0F, 0.0F, HIGHLIGHT_ALPHA);
            }
        }
    }
    */

    private void drawFenceFastenerHighlight(final FenceFastenerEntity fence, final PoseStack matrix, final VertexConsumer buf, final float delta, final double dx, final double dy, final double dz) {
        final Player player = Minecraft.getInstance().player;
        // Check if the server will allow interaction
        if (player != null && (player.hasLineOfSight(fence) || player.distanceToSqr(fence) <= 9.0D)) {
            final AABB selection = fence.getBoundingBox().move(-dx, -dy, -dz).inflate(0.002D);
            LevelRenderer.renderLineBox(matrix, buf, selection, 0.0F, 0.0F, 0.0F, HIGHLIGHT_ALPHA);
        }
    }

    private void renderHighlight(final Connection connection, final PoseStack matrix, final VertexConsumer buf) {
        final Curve cat = connection.getCatenary();
        if (cat == null) {
            return;
        }
        final Vector3f p = new Vector3f();
        final Vector3f v1 = new Vector3f();
        final Vector3f v2 = new Vector3f();
        final LineBuilder builder = new LineBuilder(matrix, buf);
        final float r = connection.getRadius() + 0.01F;
        for (int edge = 0; edge < 4; edge++) {
            p.set(cat.getX(0), cat.getY(0), cat.getZ(0));
            v1.set(cat.getDx(0), cat.getDy(0), cat.getDz(0));
            v1.normalize();
            v2.set(-v1.x(), -v1.y(), -v1.z());
            for (int n = 0; edge == 0 && n < 8; n++) {
                this.addVertex(builder, (n + 1) / 2 % 4, p, v1, v2, r);
            }
            this.addVertex(builder, edge, p, v1, v2, r);
            for (int i = 1; i < cat.getCount() - 1; i++) {
                p.set(cat.getX(i), cat.getY(i), cat.getZ(i));
                v2.set(-cat.getDx(i), -cat.getDy(i), -cat.getDz(i));
                v2.normalize();
                this.addVertex(builder, edge, p, v1, v2, r);
                this.addVertex(builder, edge, p, v1, v2, r);
                v1.set(-v2.x(), -v2.y(), -v2.z());
            }
            p.set(cat.getX(), cat.getY(), cat.getZ());
            v2.set(-v1.x(), -v1.y(), -v1.z());
            this.addVertex(builder, edge, p, v1, v2, r);
            for (int n = 0; edge == 0 && n < 8; n++) {
                this.addVertex(builder, (n + 1) / 2 % 4, p, v1, v2, r);
            }
        }
    }

    static class LineBuilder {
        final PoseStack matrix;
        final VertexConsumer buf;
        Vector3f last;

        LineBuilder(PoseStack matrix, VertexConsumer buf) {
            this.matrix = matrix;
            this.buf = buf;
        }

        void accept(Vector3f pos) {
            if (this.last == null) {
                this.last = pos;
            } else {
                Vector3f n = new Vector3f(pos);
                n.sub(this.last);
                n.normalize();
                n = this.matrix.last().normal().transform(n);
                // Apply pose transformation manually - vertex() takes double x, double y, double z
                var pose = this.matrix.last().pose();
                var lastVec3 = new org.joml.Vector3f(this.last.x(), this.last.y(), this.last.z());
                var posVec3 = new org.joml.Vector3f(pos.x(), pos.y(), pos.z());
                pose.transformPosition(lastVec3);
                pose.transformPosition(posVec3);
                // VertexConsumer.vertex() - using helper method to work around API issues
                addVertexToBuffer(this.buf, lastVec3, n, HIGHLIGHT_ALPHA);
                addVertexToBuffer(this.buf, posVec3, n, HIGHLIGHT_ALPHA);
                this.last = null;
            }
        }
    }

    private void addVertex(final LineBuilder builder, final int edge, final Vector3f p, final Vector3f v1, final Vector3f v2, final float r) {
        builder.accept(this.get(edge, p, v1, v2, r));
    }

    // Helper method to add vertex - workaround for vertex() method API changes in 1.21.1
    private static void addVertexToBuffer(VertexConsumer buf, org.joml.Vector3f pos, Vector3f normal, float alpha) {
        try {
            // Try to call vertex() method via reflection
            final java.lang.reflect.Method vertexMethod = VertexConsumer.class.getMethod("vertex", double.class, double.class, double.class);
            final Object v = vertexMethod.invoke(buf, (double)pos.x(), (double)pos.y(), (double)pos.z());
            // Call color, normal, endVertex via reflection
            final java.lang.reflect.Method colorMethod = VertexConsumer.class.getMethod("color", int.class, int.class, int.class, int.class);
            final java.lang.reflect.Method normalMethod = VertexConsumer.class.getMethod("normal", float.class, float.class, float.class);
            final java.lang.reflect.Method endVertexMethod = VertexConsumer.class.getMethod("endVertex");
            colorMethod.invoke(v, (int)(0.0F * 255), (int)(0.0F * 255), (int)(0.0F * 255), (int)(alpha * 255));
            normalMethod.invoke(v, normal.x(), normal.y(), normal.z());
            endVertexMethod.invoke(v);
        } catch (Exception e) {
            // vertex() method not available - skip rendering
            // TODO: Implement alternative for 1.21.1 VertexConsumer API
        }
    }

    private Vector3f get(final int edge, final Vector3f p, final Vector3f v1, final Vector3f v2, final float r) {
        final Vector3f up = new Vector3f();
        final Vector3f side = new Vector3f();
        // if collinear
        if (v1.dot(v2) < -(1.0F - 1.0e-2F)) {
            final float h = Mth.sqrt(v1.x() * v1.x() + v1.z() * v1.z());
            // if vertical
            if (h < 1.0e-2F) {
                up.set(-1.0F, 0.0F, 0.0F);
            } else {
                up.set(-v1.x() / h * -v1.y(), -h, -v1.z() / h * -v1.y());
            }
        } else {
            up.set(v2.x(), v2.y(), v2.z());
            up.lerp(v1, 0.5F);
        }
        up.normalize();
        side.set(v1.x(), v1.y(), v1.z());
        side.cross(up);
        side.normalize();
        side.mul(edge == 0 || edge == 3 ? -r : r);
        up.mul(edge < 2 ? -r : r);
        up.add(side);
        up.add(p);
        return up;
    }

    static class HitConnection extends Entity {
        final ClientEventHandler.HitResult result;

        HitConnection(final Level world, final ClientEventHandler.HitResult result) {
            super(EntityType.ITEM, world);
            this.setId(-1);
            this.result = result;
            this.setPos(result.intersection.getResult());
        }

        @Override
        public boolean hurt(final DamageSource source, final float amount) {
            if (source.getEntity() == Minecraft.getInstance().player) {
                this.processAction(PlayerAction.ATTACK);
                return true;
            }
            return false;
        }

        @Override
        public InteractionResult interact(final Player player, final InteractionHand hand) {
            if (player == Minecraft.getInstance().player) {
                com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: HitConnection.interact() called");
                this.processAction(PlayerAction.INTERACT);
                return InteractionResult.SUCCESS;
            }
            return super.interact(player, hand);
        }

        public void processAction(final PlayerAction action) {
            this.result.connection.processClientAction(Minecraft.getInstance().player, action, this.result.intersection);
        }

        @Override
        public ItemStack getPickedResult(net.minecraft.world.phys.HitResult target) {
            return this.result.connection.getItemStack();
        }

        @Override
        protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        }

        @Override
        protected void addAdditionalSaveData(final CompoundTag compound) {
        }

        @Override
        protected void readAdditionalSaveData(final CompoundTag compound) {
        }

        // getAddEntityPacket() removed in 1.21.1 - entities handle their own packets
        // @Override
        // public Packet<ClientGamePacketListener> getAddEntityPacket() {
        //     return new net.minecraft.network.protocol.game.ClientboundAddEntityPacket(this);
        // }
    }

    @SubscribeEvent
    public void onRenderLevelStage(final RenderLevelStageEvent event) {
        // Clear rendered connections once per frame
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            // getFrameCount() removed in 1.21.1
            // Just clear unconditionally for now, or use a custom counter if needed
            RENDERED_CONNECTIONS.clear();
            // final int frame = Minecraft.getInstance().levelRenderer.getFrameCount();
            // if (frame != lastFrame) {
            //    RENDERED_CONNECTIONS.clear();
            //    lastFrame = frame;
            // }
            return;
        }

        // Render connections during the AFTER_ENTITIES stage to bypass entity frustum culling
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        
        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        
        final PoseStack poseStack = event.getPoseStack();
        final MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        final float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        final Vec3 cameraPos = event.getCamera().getPosition();
        
        // Cache renderer
        if (fastenerRenderer == null) {
            fastenerRenderer = new FastenerRenderer(mc.getEntityModels()::bakeLayer);
        }
        
        // Collect all fasteners in a large radius to ensure connections stay visible
        // Search in a radius around the camera to find all fasteners
        final AABB searchBounds = new AABB(player.blockPosition()).inflate(64.0);
        final Set<Fastener<?>> fasteners = collectFasteners(mc.level, searchBounds);

        // Include player fastener
        CapabilityHandler.getFastenerCapability(player).ifPresent(fasteners::add);



        for (final Fastener<?> fastener : fasteners) {
            if (!fastener.hasNoConnections()) {
                poseStack.pushPose();
                // Translate to fastener's connection point relative to camera
                final Vec3 connPoint = fastener.getConnectionPoint();
                poseStack.translate(connPoint.x - cameraPos.x, connPoint.y - cameraPos.y, connPoint.z - cameraPos.z);
                
                final int packedLight = net.minecraft.client.renderer.LightTexture.pack(
                    mc.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, fastener.getPos()),
                    mc.level.getBrightness(net.minecraft.world.level.LightLayer.SKY, fastener.getPos())
                );
                
                fastenerRenderer.render(fastener, partialTick, poseStack, bufferSource, 
                    packedLight, 
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
                
                poseStack.popPose();
            }
        }
        
        bufferSource.endBatch();
    }

    private static final class HitResult {
        private final Connection connection;

        private final Intersection intersection;

        public HitResult(final Connection connection, final Intersection intersection) {
            this.connection = connection;
            this.intersection = intersection;
        }
    }
}
