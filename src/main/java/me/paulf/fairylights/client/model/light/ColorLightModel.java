package me.paulf.fairylights.client.model.light;

import me.paulf.fairylights.server.feature.light.Light;
import me.paulf.fairylights.server.feature.light.StandardLightBehavior;
import net.minecraft.client.model.geom.ModelPart;

public class ColorLightModel extends LightModel<StandardLightBehavior> {

    public ColorLightModel(final ModelPart root) {
        super(root);
    }

    private static int debugCounter = 0;
    @Override
    public void animate(final Light<?> light, final StandardLightBehavior behavior, final float delta) {
        super.animate(light, behavior, delta);
        this.brightness = behavior.getBrightness(delta);
        this.red = behavior.getRed(delta);
        this.green = behavior.getGreen(delta);
        this.blue = behavior.getBlue(delta);
        if (debugCounter++ < 100) {
             com.mojang.logging.LogUtils.getLogger().info("FL_DEBUG: ColorLightModel.animate: R=" + this.red + " G=" + this.green + " B=" + this.blue);
        }
    }
}
