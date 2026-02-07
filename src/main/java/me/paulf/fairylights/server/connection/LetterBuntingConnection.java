package me.paulf.fairylights.server.connection;

import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import me.paulf.fairylights.server.collision.Intersection;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.feature.Letter;
import me.paulf.fairylights.server.net.clientbound.OpenEditLetteredConnectionScreenMessage;
import me.paulf.fairylights.util.Catenary;
import me.paulf.fairylights.util.Curve;
import me.paulf.fairylights.util.styledstring.StyledString;
import me.paulf.fairylights.util.styledstring.StylingPresence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
// PacketDistributor removed in NeoForge 1.21.1 - using PayloadRegistrar instead

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LetterBuntingConnection extends Connection implements Lettered {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final SymbolSet SYMBOLS = new SymbolSet.Builder(7, "0-9, A-Z, &, !, ?")
            .add(" 0123456789ABCDEFGHJKLMNOPQRSTUVWXYZ&?", 6)
            .add("I", 4)
            .add("!", 2)
            .build();

    private static final float TRACKING = 1.0F / 16.0F;

    private static final StylingPresence SUPPORTED_STYLING = new StylingPresence(true, false, false, false, false,
            false);

    private StyledString text;

    private Letter[] letters = new Letter[0];

    public LetterBuntingConnection(final ConnectionType<? extends LetterBuntingConnection> type, final Level world,
            final Fastener<?> fastener, final UUID uuid) {
        super(type, world, fastener, uuid);
        this.text = new StyledString();
    }

    @Override
    public float getRadius() {
        return 0.9F / 32;
    }

    public Letter[] getLetters() {
        return this.letters;
    }

    @Override
    public void processClientAction(final Player player, final PlayerAction action, final Intersection intersection) {
        if (this.openTextGui(player, action, intersection)) {
            super.processClientAction(player, action, intersection);
        }
    }

    @Override
    public void onConnect(final Level world, final Player user, final ItemStack heldStack) {
        if (this.text.isEmpty()) {
            if (user instanceof ServerPlayer serverPlayer) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer,
                        new OpenEditLetteredConnectionScreenMessage<>(this));
            }
        }
    }

    @Override
    protected void onUpdate() {
        for (final Letter letter : this.letters) {
            letter.tick(this.world);
        }
    }

    @Override
    protected void onCalculateCatenary(final boolean relocated) {
        // Only update letters if text is not empty
        // If text is empty, don't clear letters here - let deserializeLogic handle it
        // This prevents clearing letters when catenary is computed before text sync completes
        if (!this.text.isEmpty()) {
            this.updateLetters();
        } else {
            LOGGER.info("LetterBuntingConnection: onCalculateCatenary - skipping updateLetters because text is empty");
        }
    }

    private void updateLetters() {
        final boolean isClient = this.world != null && this.world.isClientSide();
        LOGGER.info("LetterBuntingConnection: updateLetters called ({} side) - text='{}' length={} isEmpty={}", 
                isClient ? "CLIENT" : "SERVER", this.text, this.text.length(), this.text.isEmpty());
        
        if (this.text.isEmpty()) {
            this.letters = new Letter[0];
            LOGGER.info("LetterBuntingConnection: updateLetters - text is empty, cleared letters");
            return;
        }
        
        final Curve catenary = this.getCatenary();
        if (catenary == null) {
            LOGGER.warn("LetterBuntingConnection: updateLetters - catenary is null, cannot create letters");
            this.letters = new Letter[0];
            return;
        }
        
        float textWidth = 0;
        int textLen = 0;
        final float[] pointOffsets = new float[this.text.length()];
        final float catLength = catenary.getLength();
        LOGGER.info("LetterBuntingConnection: updateLetters - catenary length={}", catLength);
        
        for (int i = 0; i < this.text.length(); i++) {
            final float w = SYMBOLS.getWidth(this.text.charAt(i));
            pointOffsets[i] = textWidth + w / 2.0F;
            textWidth += w + TRACKING;
            if (textWidth > catLength) {
                break;
            }
            textLen++;
        }
        
        LOGGER.info("LetterBuntingConnection: updateLetters - textWidth={} textLen={}", textWidth, textLen);
        
        final float offset = catLength / 2 - textWidth / 2;
        for (int i = 0; i < textLen; i++) {
            pointOffsets[i] += offset;
        }
        int pointIdx = 0;
        final Letter[] prevLetters = this.letters;
        final List<Letter> letters = new ArrayList<>(this.text.length());
        final Catenary.SegmentIterator it = catenary.iterator();
        float distance = 0;
        while (it.next()) {
            final float length = it.getLength();
            for (int n = pointIdx; n < textLen; n++) {
                final float pointOffset = pointOffsets[n];
                if (pointOffset < distance + length) {
                    final float t = (pointOffset - distance) / length;
                    final Vec3 point = new Vec3(it.getX(t), it.getY(t), it.getZ(t));
                    final Letter letter;
                    if (prevLetters != null && pointIdx < prevLetters.length) {
                        letter = prevLetters[pointIdx];
                        letter.set(point, it.getYaw(), it.getPitch());
                        letter.set(this.text.charAt(pointIdx), this.text.styleAt(pointIdx));
                    } else {
                        letter = new Letter(pointIdx, point, it.getYaw(), it.getPitch(), SYMBOLS,
                                this.text.charAt(pointIdx), this.text.styleAt(pointIdx));
                    }
                    letters.add(letter);
                    pointIdx++;
                } else {
                    break;
                }
            }
            if (pointIdx == textLen) {
                break;
            }
            distance += length;
        }
        this.letters = letters.toArray(new Letter[0]);
        LOGGER.info("LetterBuntingConnection: updateLetters - created {} letters, text='{}' length={} conn={}", 
                this.letters.length, this.text, this.text.length(), System.identityHashCode(this));
    }

    @Override
    public StylingPresence getSupportedStyling() {
        return SUPPORTED_STYLING;
    }

    @Override
    public boolean isSupportedCharacter(final int chr) {
        return SYMBOLS.contains(chr);
    }

    @Override
    public boolean isSupportedText(final StyledString text) {
        float len = 0;
        final float available = this.getCatenary().getLength();
        for (int i = 0; i < text.length(); i++) {
            final float w = SYMBOLS.getWidth(text.charAt(i));
            len += w + TRACKING;
            if (len > available) {
                return false;
            }
            if (!text.styleAt(i).isPlain()) {
                return false;
            }
        }
        return Lettered.super.isSupportedText(text);
    }

    @Override
    public void setText(final StyledString text) {
        this.text = text;
        LOGGER.info("LetterBuntingConnection: setText called with '{}' length={}", text, text.length());
        this.computeCatenary();
        // Mark fastener dirty to sync text change to clients
        if (!this.world.isClientSide()) {
            this.fastener.setDirty();
            this.getDestination().get(this.world, false).ifPresent(Fastener::setDirty);
            LOGGER.info("LetterBuntingConnection: setText - marked fastener dirty for sync");
            
            // Force immediate block entity update to sync state to clients
            // This ensures deserializeLogic() is called on the client with the new text value
            if (this.fastener instanceof me.paulf.fairylights.server.fastener.BlockFastener blockFastener) {
                final net.minecraft.core.BlockPos pos = blockFastener.getPos();
                final net.minecraft.world.level.block.entity.BlockEntity be = this.world.getBlockEntity(pos);
                if (be instanceof me.paulf.fairylights.server.block.entity.FastenerBlockEntity fastenerBE) {
                    fastenerBE.setChanged();
                    final net.minecraft.world.level.block.state.BlockState state = this.world.getBlockState(pos);
                    this.world.sendBlockUpdated(pos, state, state, 3);
                    LOGGER.info("LetterBuntingConnection: setText - forced block entity update at {}", pos);
                }
            }
        }
    }

    @Override
    public StyledString getText() {
        return this.text;
    }

    @Override
    public Function<String, String> getInputTransformer() {
        return str -> Normalizer.normalize(str, Normalizer.Form.NFKD).replaceAll("[\\p{Mn}\\p{Sk}]", "")
                .toUpperCase(Locale.ROOT);
    }

    @Override
    public String getAllowedDescription() {
        return SYMBOLS.getDescription();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen createTextGUI() {
        return new EditLetteredConnectionScreen<>(this);
    }

    @Override
    public CompoundTag serialize() {
        final CompoundTag compound = super.serialize();
        LOGGER.info("LetterBuntingConnection: serialize() text='{}' length={}", this.text, this.text.length());
        return compound;
    }

    @Override
    public void deserialize(final CompoundTag compound) {
        throw new UnsupportedOperationException("Use deserialize(CompoundTag, HolderLookup.Provider)");
    }

    @Override
    public void deserialize(final CompoundTag compound, final net.minecraft.core.HolderLookup.Provider provider) {
        LOGGER.info("LetterBuntingConnection: deserialize(CompoundTag, Provider) ENTRY - world={} text='{}' length={}", 
                this.world != null ? (this.world.isClientSide() ? "CLIENT" : "SERVER") : "NULL",
                this.text, this.text.length());
        
        // Call parent to handle destination, slack, drop, etc.
        super.deserialize(compound, provider);
        
        // The text should be handled in deserializeLogic(), but let's also check here
        LOGGER.info("LetterBuntingConnection: deserialize() EXIT - text='{}' length={} letters.length={}", 
                this.text, this.text.length(), this.letters.length);
    }

    @Override
    public CompoundTag serializeLogic() {
        final CompoundTag compound = super.serializeLogic();
        compound.put("text", StyledString.serialize(this.text));
        LOGGER.info("LetterBuntingConnection: serializeLogic ({}) text='{}' length={} tag.hasText={}", 
                this.world != null && !this.world.isClientSide() ? "SERVER" : "CLIENT",
                this.text, this.text.length(), compound.contains("text"));
        return compound;
    }

    @Override
    public void deserializeLogic(final CompoundTag compound, final net.minecraft.core.HolderLookup.Provider provider) {
        super.deserializeLogic(compound, provider);
        // Check if "text" compound exists before deserializing
        final StyledString oldText = this.text;
        if (compound.contains("text", Tag.TAG_COMPOUND)) {
            this.text = StyledString.deserialize(compound.getCompound("text"));
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) text='{}' length={}", 
                    this.world != null && this.world.isClientSide() ? "CLIENT" : "SERVER", 
                    this.text, this.text.length());
        } else {
            this.text = new StyledString();
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - no text compound found, using empty StyledString",
                    this.world != null && this.world.isClientSide() ? "CLIENT" : "SERVER");
        }
        
        // If text changed or catenary exists, ensure letters are updated
        // Don't call computeCatenary() here - it will be called naturally by the update loop
        // Just update letters if catenary already exists
        final boolean textChanged = oldText == null || !oldText.equals(this.text);
        if (this.getCatenary() != null && !this.text.isEmpty()) {
            // Catenary exists and text is not empty, update letters immediately
            this.updateLetters();
            LOGGER.info("LetterBuntingConnection: deserializeLogic - catenary exists, updated letters. letters.length={} textChanged={}", 
                    this.letters.length, textChanged);
        } else if (this.text.isEmpty()) {
            // Text is empty, clear letters
            this.letters = new Letter[0];
            LOGGER.info("LetterBuntingConnection: deserializeLogic - text is empty, cleared letters");
        } else if (textChanged && !this.text.isEmpty()) {
            // Text changed but catenary not computed yet - mark for update
            // The update loop will compute catenary and call onCalculateCatenary() -> updateLetters()
            this.computeCatenary();
            LOGGER.info("LetterBuntingConnection: deserializeLogic - text changed but catenary not ready, marked for recomputation");
        } else {
            LOGGER.info("LetterBuntingConnection: deserializeLogic - catenary not yet computed, letters will be updated when catenary is computed");
        }
    }
}
