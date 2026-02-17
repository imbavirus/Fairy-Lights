package za.co.infernos.fairylights.server.feature.light;

public interface BrightnessLightBehavior extends LightBehavior {
    float getBrightness(final float delta);
}
