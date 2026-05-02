package za.co.infernos.test_mc;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.Util;

public class TestMC {
    public static void check() {
        System.out.println(Minecraft.class);
        System.out.println(ResourceLocation.class);
        System.out.println(RenderType.class);
        System.out.println(Util.class);
    }
}
