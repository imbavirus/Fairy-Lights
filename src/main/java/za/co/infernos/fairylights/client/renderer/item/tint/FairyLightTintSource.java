package za.co.infernos.fairylights.client.renderer.item.tint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import za.co.infernos.fairylights.server.feature.light.ColorChangingBehavior;
import za.co.infernos.fairylights.server.item.DyeableItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FairyLightTintSource implements ItemTintSource {
    public static final MapCodec<FairyLightTintSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("index", 1).forGetter(s -> s.index)
    ).apply(instance, FairyLightTintSource::new));

    private final int index;

    public FairyLightTintSource(int index) {
        this.index = index;
    }

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        if (index > 0) {
            if (ColorChangingBehavior.exists(stack)) {
                return ColorChangingBehavior.animate(stack);
            }
            return DyeableItem.getColor(stack);
        }
        return 0xFFFFFF;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
