package za.co.infernos.fairylights.server.connection;

import za.co.infernos.fairylights.client.gui.EditLetteredConnectionScreen;
import za.co.infernos.fairylights.server.collision.Intersection;
import za.co.infernos.fairylights.server.fastener.Fastener;
import za.co.infernos.fairylights.server.feature.Letter;
import za.co.infernos.fairylights.server.net.clientbound.OpenEditLetteredConnectionScreenMessage;
import za.co.infernos.fairylights.util.Catenary;
import za.co.infernos.fairylights.util.Curve;
import za.co.infernos.fairylights.util.styledstring.StyledString;
import za.co.infernos.fairylights.util.styledstring.StylingPresence;
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
        final boolean isClient = this.world != null && this.world.isClientSide();
        LOGGER.info("LetterBuntingConnection: getLetters() called ({} side) - letters.length={} text='{}' length={} conn={}", 
                isClient ? "CLIENT" : "SERVER", this.letters.length, this.text, this.text.length(), System.identityHashCode(this));
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
        final boolean isClient = this.world != null && this.world.isClientSide();
        final Curve catenary = this.getCatenary();
        LOGGER.info("LetterBuntingConnection: onCalculateCatenary called ({} side) - relocated={} catenary={} text='{}' length={} isEmpty={} conn={}", 
                isClient ? "CLIENT" : "SERVER", relocated, catenary != null ? "exists" : "null", 
                this.text, this.text.length(), this.text.isEmpty(), System.identityHashCode(this));
        // Always update letters when catenary is calculated
        // This ensures letters are generated/updated whenever the catenary changes
        // If text is empty, updateLetters() will clear the letters array
        this.updateLetters();
        LOGGER.info("LetterBuntingConnection: onCalculateCatenary - updated letters. text='{}' length={} letters.length={} conn={}", 
                this.text, this.text.length(), this.letters.length, System.identityHashCode(this));
    }

    private void updateLetters() {
        final boolean isClient = this.world != null && this.world.isClientSide();
        final int oldLettersLength = this.letters.length;
        LOGGER.info("LetterBuntingConnection: updateLetters ENTRY ({} side) - text='{}' length={} isEmpty={} oldLetters.length={} conn={}", 
                isClient ? "CLIENT" : "SERVER", this.text, this.text.length(), this.text.isEmpty(), 
                oldLettersLength, System.identityHashCode(this));
        
        if (this.text.isEmpty()) {
            this.letters = new Letter[0];
            LOGGER.info("LetterBuntingConnection: updateLetters ({} side) - text is empty, cleared letters", 
                    isClient ? "CLIENT" : "SERVER");
            return;
        }
        
        final Curve catenary = this.getCatenary();
        if (catenary == null) {
            LOGGER.warn("LetterBuntingConnection: updateLetters ({} side) - catenary is null, cannot create letters. text='{}' length={} conn={}", 
                    isClient ? "CLIENT" : "SERVER", this.text, this.text.length(), System.identityHashCode(this));
            this.letters = new Letter[0];
            return;
        }
        
        float textWidth = 0;
        int textLen = 0;
        final float[] pointOffsets = new float[this.text.length()];
        final float catLength = catenary.getLength();
        LOGGER.info("LetterBuntingConnection: updateLetters ({} side) - catenary exists, length={} text.length={}", 
                isClient ? "CLIENT" : "SERVER", catLength, this.text.length());
        
        for (int i = 0; i < this.text.length(); i++) {
            final char ch = this.text.charAt(i);
            final float w = SYMBOLS.getWidth(ch);
            pointOffsets[i] = textWidth + w / 2.0F;
            textWidth += w + TRACKING;
            if (textWidth > catLength) {
                LOGGER.info("LetterBuntingConnection: updateLetters ({} side) - text too long, stopping at char {} (width={} > catLength={})", 
                        isClient ? "CLIENT" : "SERVER", i, textWidth, catLength);
                break;
            }
            textLen++;
        }
        
        LOGGER.info("LetterBuntingConnection: updateLetters ({} side) - calculated textWidth={} textLen={} (will create {} letters)", 
                isClient ? "CLIENT" : "SERVER", textWidth, textLen, textLen);
        
        if (textLen == 0) {
            LOGGER.warn("LetterBuntingConnection: updateLetters ({} side) - textLen is 0, no letters to create!", 
                    isClient ? "CLIENT" : "SERVER");
            this.letters = new Letter[0];
            return;
        }
        
        final float offset = catLength / 2 - textWidth / 2;
        for (int i = 0; i < textLen; i++) {
            pointOffsets[i] += offset;
        }
        int pointIdx = 0;
        final Letter[] prevLetters = this.letters;
        final List<Letter> letters = new ArrayList<>(this.text.length());
        final Catenary.SegmentIterator it = catenary.iterator();
        float distance = 0;
        int segmentsProcessed = 0;
        while (it.next()) {
            segmentsProcessed++;
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
                        LOGGER.debug("LetterBuntingConnection: updateLetters ({} side) - reused letter {} at point {}", 
                                isClient ? "CLIENT" : "SERVER", pointIdx, point);
                    } else {
                        letter = new Letter(pointIdx, point, it.getYaw(), it.getPitch(), SYMBOLS,
                                this.text.charAt(pointIdx), this.text.styleAt(pointIdx));
                        LOGGER.debug("LetterBuntingConnection: updateLetters ({} side) - created new letter {} ('{}') at point {}", 
                                isClient ? "CLIENT" : "SERVER", pointIdx, this.text.charAt(pointIdx), point);
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
        LOGGER.info("LetterBuntingConnection: updateLetters EXIT ({} side) - created {} letters (expected {}), processed {} segments, text='{}' length={} conn={}", 
                isClient ? "CLIENT" : "SERVER", this.letters.length, textLen, segmentsProcessed, 
                this.text, this.text.length(), System.identityHashCode(this));
        
        if (this.letters.length != textLen) {
            LOGGER.warn("LetterBuntingConnection: updateLetters ({} side) - MISMATCH! Created {} letters but expected {}! text='{}' conn={}", 
                    isClient ? "CLIENT" : "SERVER", this.letters.length, textLen, this.text, System.identityHashCode(this));
        }
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
        final boolean isClient = this.world != null && this.world.isClientSide();
        final StyledString oldText = this.text;
        LOGGER.info("LetterBuntingConnection: setText called ({} side) - oldText='{}' newText='{}' oldLength={} newLength={} conn={} catenary={}", 
                isClient ? "CLIENT" : "SERVER", oldText, text, oldText.length(), text.length(), 
                System.identityHashCode(this), this.getCatenary() != null ? "exists" : "null");
        this.text = text;
        LOGGER.info("LetterBuntingConnection: setText - text set, calling computeCatenary()");
        this.computeCatenary();
        LOGGER.info("LetterBuntingConnection: setText - computeCatenary() called, catenary={} letters.length={}", 
                this.getCatenary() != null ? "exists" : "null", this.letters.length);
        // Mark fastener dirty to sync text change to clients
        if (!this.world.isClientSide()) {
            this.fastener.setDirty();
            this.getDestination().get(this.world, false).ifPresent(Fastener::setDirty);
            LOGGER.info("LetterBuntingConnection: setText - marked fastener dirty for sync");
            
            // Force immediate block entity update to sync state to clients
            // This ensures deserializeLogic() is called on the client with the new text value
            if (this.fastener instanceof za.co.infernos.fairylights.server.fastener.BlockFastener blockFastener) {
                final net.minecraft.core.BlockPos pos = blockFastener.getPos();
                final net.minecraft.world.level.block.entity.BlockEntity be = this.world.getBlockEntity(pos);
                if (be instanceof za.co.infernos.fairylights.server.block.entity.FastenerBlockEntity fastenerBE) {
                    fastenerBE.setChanged();
                    final net.minecraft.world.level.block.state.BlockState state = this.world.getBlockState(pos);
                    this.world.sendBlockUpdated(pos, state, state, 3);
                    LOGGER.info("LetterBuntingConnection: setText - forced block entity update at {} (SERVER)", pos);
                }
            }
        } else {
            LOGGER.info("LetterBuntingConnection: setText - CLIENT side, not syncing");
        }
    }

    @Override
    public StyledString getText() {
        final boolean isClient = this.world != null && this.world.isClientSide();
        LOGGER.info("LetterBuntingConnection: getText() called ({} side) - text='{}' length={} isEmpty={} conn={}", 
                isClient ? "CLIENT" : "SERVER", this.text, this.text.length(), this.text.isEmpty(), System.identityHashCode(this));
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
        final boolean isClient = this.world != null && this.world.isClientSide();
        final StyledString oldText = this.text;
        final Curve oldCatenary = this.getCatenary();
        final int oldLettersLength = this.letters.length;
        LOGGER.info("LetterBuntingConnection: deserializeLogic ENTRY ({} side) - oldText='{}' oldLength={} oldCatenary={} oldLetters.length={} conn={} compound.hasText={}", 
                isClient ? "CLIENT" : "SERVER", oldText, oldText.length(), 
                oldCatenary != null ? "exists" : "null", oldLettersLength, 
                System.identityHashCode(this), compound.contains("text", Tag.TAG_COMPOUND));
        
        super.deserializeLogic(compound, provider);
        
        // Check if "text" compound exists before deserializing
        if (compound.contains("text", Tag.TAG_COMPOUND)) {
            this.text = StyledString.deserialize(compound.getCompound("text"));
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - deserialized text='{}' length={} isEmpty={}", 
                    isClient ? "CLIENT" : "SERVER", this.text, this.text.length(), this.text.isEmpty());
        } else {
            this.text = new StyledString();
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - no text compound found, using empty StyledString",
                    isClient ? "CLIENT" : "SERVER");
        }
        
        // Always ensure letters are updated after text changes
        // The catenary might be computed in the same update cycle, so we need to handle both cases:
        // 1. Catenary already exists - update letters immediately
        // 2. Catenary doesn't exist yet - it will be computed and onCalculateCatenary() will call updateLetters()
        final boolean textChanged = oldText == null || !oldText.equals(this.text);
        final Curve currentCatenary = this.getCatenary();
        LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - textChanged={} currentCatenary={} text.isEmpty={}", 
                isClient ? "CLIENT" : "SERVER", textChanged, 
                currentCatenary != null ? "exists" : "null", this.text.isEmpty());
        
        if (this.text.isEmpty()) {
            // Text is empty, clear letters
            this.letters = new Letter[0];
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - text is empty, cleared letters", 
                    isClient ? "CLIENT" : "SERVER");
        } else if (currentCatenary != null) {
            // Catenary exists and text is not empty, update letters immediately
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - catenary exists, calling updateLetters()", 
                    isClient ? "CLIENT" : "SERVER");
            this.updateLetters();
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - updateLetters() completed. letters.length={} textChanged={}", 
                    isClient ? "CLIENT" : "SERVER", this.letters.length, textChanged);
        } else if (textChanged) {
            // Text changed but catenary not computed yet - mark for update
            // The update loop will compute catenary and call onCalculateCatenary() -> updateLetters()
            // But also ensure updateCatenary flag is set so catenary gets recomputed
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - text changed but catenary not ready, calling computeCatenary()", 
                    isClient ? "CLIENT" : "SERVER");
            this.computeCatenary();
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - computeCatenary() called, catenary will be computed in next update", 
                    isClient ? "CLIENT" : "SERVER");
        } else {
            LOGGER.info("LetterBuntingConnection: deserializeLogic ({}) - catenary not yet computed, letters will be updated when catenary is computed", 
                    isClient ? "CLIENT" : "SERVER");
        }
        
        LOGGER.info("LetterBuntingConnection: deserializeLogic EXIT ({} side) - text='{}' length={} letters.length={} conn={}", 
                isClient ? "CLIENT" : "SERVER", this.text, this.text.length(), this.letters.length, System.identityHashCode(this));
    }
}
