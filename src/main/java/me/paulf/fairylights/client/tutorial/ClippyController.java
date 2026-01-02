package me.paulf.fairylights.client.tutorial;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.paulf.fairylights.client.FLClientConfig;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import me.paulf.fairylights.util.LazyItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
// TickEvent removed in NeoForge 1.21.1 - TODO: Use alternative event system
// import net.neoforged.neoforge.event.tick.TickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ClippyController {
    private final ImmutableMap<String, Supplier<State>> states = Stream.<Supplier<State>>of(
            NoProgressState::new,
            CraftHangingLightsState::new,
            CompleteState::new
        ).collect(ImmutableMap.toImmutableMap(s -> s.get().name(), Function.identity()));

    private State state = new NoProgressState();

    public void init(final IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener((final LevelEvent.Load event) -> {
            if (event.getLevel() instanceof ClientLevel) {
                this.reload();
            }
        });
        // NeoForge 1.21.1 uses different event system - use EntityTickEvent or similar
        NeoForge.EVENT_BUS.addListener((final net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) -> {
            if (event.getEntity() instanceof net.minecraft.client.player.LocalPlayer player) {
                final Minecraft mc = Minecraft.getInstance();
                if (!mc.isPaused() && mc.player != null) {
                    this.state.tick(player, this);
                }
            }
        });
        modBus.<ModConfigEvent.Loading>addListener(e -> {
            if (e.getConfig().getSpec() == FLClientConfig.SPEC && Minecraft.getInstance().player != null) {
                this.reload();
            }
        });
        NeoForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingIn>addListener(e -> {
            this.reload();
            this.state.tick(e.getPlayer(), this);
        });
    }

    private void reload() {
        // Check if config is loaded before accessing it
        try {
            String progressValue = FLClientConfig.TUTORIAL.progress.get();
            this.setState(this.states.getOrDefault(progressValue, NoProgressState::new).get());
        } catch (IllegalStateException | NullPointerException e) {
            // Config not loaded yet, use default state
            this.setState(new NoProgressState());
        }
    }

    private void setState(final State state) {
        this.state.stop();
        this.state = state;
        this.state.start();
        // Only save config if it's loaded
        try {
            FLClientConfig.TUTORIAL.progress.set(this.state.name());
            FLClientConfig.TUTORIAL.progress.save();
        } catch (IllegalStateException | NullPointerException e) {
            // Config not loaded yet, skip saving
        }
    }

    interface State {
        String name();

        default void start() {}

        default void tick(final LocalPlayer player, final ClippyController controller) {}

        default void stop() {}
    }

    static class NoProgressState implements State {
        @Override
        public String name() {
            return "none";
        }

        @Override
        public void tick(final LocalPlayer player, final ClippyController controller) {
            if (player.getInventory().contains(FLCraftingRecipes.LIGHTS)) {
                controller.setState(new CraftHangingLightsState());
            }
        }
    }

    static class CraftHangingLightsState implements State {
        final Balloon balloon;

        CraftHangingLightsState() {
            this.balloon = new Balloon(new LazyItemStack(FLItems.HANGING_LIGHTS, Item::getDefaultInstance),
                Component.translatable("tutorial.fairylights.craft_hanging_lights.title"),
                Component.translatable("tutorial.fairylights.craft_hanging_lights.description")
            );
        }

        @Override
        public String name() {
            return "hanging_lights";
        }

        @Override
        public void start() {
            Minecraft.getInstance().getToasts().addToast(this.balloon);
        }

        @Override
        public void tick(final LocalPlayer player, final ClippyController controller) {
            if (!player.getInventory().contains(FLCraftingRecipes.LIGHTS) &&
                    !player.getInventory().getSelected().is(FLCraftingRecipes.LIGHTS)) {
                controller.setState(new NoProgressState());
            } else if (FLItems.HANGING_LIGHTS.value() != null && (
                    player.getInventory().getSelected().getItem() == FLItems.HANGING_LIGHTS.value() ||
                    player.getInventory().contains(new ItemStack(FLItems.HANGING_LIGHTS.value())) ||
                    player.getStats().getValue(Stats.ITEM_CRAFTED.get(FLItems.HANGING_LIGHTS.value())) > 0)) {
                controller.setState(new CompleteState());
            }
        }

        @Override
        public void stop() {
            this.balloon.hide();
        }
    }

    static class CompleteState implements State {
        @Override
        public String name() {
            return "complete";
        }
    }

    static class Balloon implements Toast {
        final LazyItemStack stack;
        final Component title;
        @Nullable
        final Component subtitle;
        Toast.Visibility visibility;

        Balloon(final LazyItemStack stack, final Component title, @Nullable final Component subtitle) {
            this.stack = stack;
            this.title = title;
            this.subtitle = subtitle;
            this.visibility = Visibility.SHOW;
        }

        void hide() {
            this.visibility = Toast.Visibility.HIDE;
        }

        @Override
        public Visibility render(final GuiGraphics stack, final ToastComponent toastGui, final long delta) {
            // Toast texture location in 1.21.1
            final net.minecraft.resources.ResourceLocation TEXTURE = net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/gui/toasts.png");
            stack.blit(TEXTURE, 0, 0, 0, 96, 160, 32);
            stack.renderFakeItem(this.stack.get(), 6 + 2, 6 + 2);
            if (this.subtitle == null) {
                stack.drawString(toastGui.getMinecraft().font, this.title, 30, 12, 0xFF500050);
            } else {
                stack.drawString(toastGui.getMinecraft().font, this.title, 30, 7, 0xFF500050);
                stack.drawString(toastGui.getMinecraft().font, this.subtitle, 30, 18, 0xFF000000);
            }
            return this.visibility;
        }
    }
}
