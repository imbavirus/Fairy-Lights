package me.paulf.fairylights.server.sound;

import me.paulf.fairylights.FairyLights;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class FLSounds {
    private FLSounds() {}

    public static final DeferredRegister<SoundEvent> REG = DeferredRegister.create(Registries.SOUND_EVENT, FairyLights.ID);

    public static final DeferredHolder<SoundEvent,SoundEvent> CORD_STRETCH = create("cord.stretch");

    public static final DeferredHolder<SoundEvent,SoundEvent> CORD_CONNECT = create("cord.connect");

    public static final DeferredHolder<SoundEvent,SoundEvent> CORD_DISCONNECT = create("cord.disconnect");

    public static final DeferredHolder<SoundEvent,SoundEvent> CORD_SNAP = create("cord.snap");

    public static final DeferredHolder<SoundEvent,SoundEvent> JINGLE_BELL = create("jingle_bell");

    public static final DeferredHolder<SoundEvent,SoundEvent> FEATURE_COLOR_CHANGE = create("feature.color_change");

    public static final DeferredHolder<SoundEvent,SoundEvent> FEATURE_LIGHT_TURNON = create("feature.light_turnon");

    public static final DeferredHolder<SoundEvent,SoundEvent> FEATURE_LIGHT_TURNOFF = create("feature.light_turnoff");

    private static DeferredHolder<SoundEvent,SoundEvent> create(final String name) {
        return REG.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FairyLights.ID, name)));
    }
}
