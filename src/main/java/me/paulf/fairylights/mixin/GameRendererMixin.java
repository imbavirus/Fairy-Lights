package me.paulf.fairylights.mixin;

import me.paulf.fairylights.client.ClientEventHandler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // TODO: Fix mixin - method signature changed in 1.21.1
    // Need to check obfuscation mappings for correct method name
    // @Inject(at = @At("RETURN"), method = "pick(F)V")
    // private void pick(float delta, CallbackInfo ci) {
    //     ClientEventHandler.updateHitConnection();
    // }
}
