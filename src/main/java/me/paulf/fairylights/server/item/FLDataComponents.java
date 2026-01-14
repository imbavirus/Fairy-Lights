package me.paulf.fairylights.server.item;

import me.paulf.fairylights.FairyLights;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.mojang.serialization.Codec;

public class FLDataComponents {
        public static final DeferredRegister<DataComponentType<?>> REG = DeferredRegister
                        .create(Registries.DATA_COMPONENT_TYPE, FairyLights.ID);

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COLOR = REG
                        .register("color", () -> DataComponentType.<Integer>builder().persistent(Codec.INT)
                                        .networkSynchronized(ByteBufCodecs.INT).build());

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.minecraft.nbt.CompoundTag>> CONNECTION_LOGIC = REG
                        .register("connection_logic",
                                        () -> DataComponentType.<net.minecraft.nbt.CompoundTag>builder()
                                                        .persistent(net.minecraft.nbt.CompoundTag.CODEC)
                                                        .networkSynchronized(ByteBufCodecs.COMPOUND_TAG).build());

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.minecraft.nbt.CompoundTag>> STYLED_STRING = REG
                        .register("styled_string",
                                        () -> DataComponentType.<net.minecraft.nbt.CompoundTag>builder()
                                                        .persistent(net.minecraft.nbt.CompoundTag.CODEC)
                                                        .networkSynchronized(ByteBufCodecs.COMPOUND_TAG).build());

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TWINKLE = REG
                        .register("twinkle", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL)
                                        .networkSynchronized(ByteBufCodecs.BOOL).build());
}
