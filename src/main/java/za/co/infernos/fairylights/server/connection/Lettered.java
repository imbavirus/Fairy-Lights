package za.co.infernos.fairylights.server.connection;

import za.co.infernos.fairylights.server.collision.Intersection;
import za.co.infernos.fairylights.util.styledstring.StyledString;
import za.co.infernos.fairylights.util.styledstring.StylingPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public interface Lettered {
    default StylingPresence getSupportedStyling() {
        return StylingPresence.ALL;
    }

    default boolean isSupportedCharacter(final int chr) {
        return Character.isValidCodePoint(chr) && ((((1 << Character.NON_SPACING_MARK | 1 << Character.MODIFIER_SYMBOL) >> Character.getType(chr)) & 1) == 0);
    }

    default boolean isSupportedText(final StyledString text) {
        for (int i = 0; i < text.length(); i++) {
            if (!this.isSupportedCharacter(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    void setText(StyledString text);

    StyledString getText();

    default String getAllowedDescription() {
        return "";
    }

    default Function<String, String> getInputTransformer() {
        return Function.identity();
    }

    Screen createTextGUI();

    default boolean openTextGui(final Player player, final PlayerAction action, final Intersection intersection) {
        if (action == PlayerAction.INTERACT && player.isSecondaryUseActive()) {
            Minecraft.getInstance().setScreen(this.createTextGUI());
            return false;
        }
        return true;
    }
}
