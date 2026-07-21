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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

public final class LetterBuntingConnection extends Connection implements Lettered {
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
        this.updateLetters();
    }

    private void updateLetters() {
        if (this.text.isEmpty()) {
            this.letters = new Letter[0];
            return;
        }

        final Curve catenary = this.getCatenary();
        if (catenary == null) {
            this.letters = new Letter[0];
            return;
        }

        float textWidth = 0;
        int textLen = 0;
        final float[] pointOffsets = new float[this.text.length()];
        final float catLength = catenary.getLength();

        for (int i = 0; i < this.text.length(); i++) {
            final char ch = this.text.charAt(i);
            final float w = SYMBOLS.getWidth(ch);
            pointOffsets[i] = textWidth + w / 2.0F;
            textWidth += w + TRACKING;
            if (textWidth > catLength) {
                break;
            }
            textLen++;
        }

        if (textLen == 0) {
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
        this.computeCatenary();
        if (!this.world.isClientSide()) {
            this.fastener.setDirty();
            this.getDestination().get(this.world, false).ifPresent(Fastener::setDirty);

            if (this.fastener instanceof za.co.infernos.fairylights.server.fastener.BlockFastener blockFastener) {
                final net.minecraft.core.BlockPos pos = blockFastener.getPos();
                final net.minecraft.world.level.block.entity.BlockEntity be = this.world.getBlockEntity(pos);
                if (be instanceof za.co.infernos.fairylights.server.block.entity.FastenerBlockEntity fastenerBE) {
                    fastenerBE.setChanged();
                    final net.minecraft.world.level.block.state.BlockState state = this.world.getBlockState(pos);
                    this.world.sendBlockUpdated(pos, state, state, 3);
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
        return super.serialize();
    }

    @Override
    public void deserialize(final CompoundTag compound) {
        throw new UnsupportedOperationException("Use deserialize(CompoundTag, HolderLookup.Provider)");
    }

    @Override
    public void deserialize(final CompoundTag compound, final net.minecraft.core.HolderLookup.Provider provider) {
        super.deserialize(compound, provider);
    }

    @Override
    public CompoundTag serializeLogic() {
        final CompoundTag compound = super.serializeLogic();
        compound.put("text", StyledString.serialize(this.text));
        return compound;
    }

    @Override
    public void deserializeLogic(final CompoundTag compound, final net.minecraft.core.HolderLookup.Provider provider) {
        final StyledString oldText = this.text;
        super.deserializeLogic(compound, provider);

        if (compound.contains("text", Tag.TAG_COMPOUND)) {
            this.text = StyledString.deserialize(compound.getCompound("text"));
        } else {
            this.text = new StyledString();
        }

        final boolean textChanged = oldText == null || !oldText.equals(this.text);
        final Curve currentCatenary = this.getCatenary();

        if (this.text.isEmpty()) {
            this.letters = new Letter[0];
        } else if (currentCatenary != null) {
            this.updateLetters();
        } else if (textChanged) {
            this.computeCatenary();
        }
    }
}
