package za.co.infernos.fairylights;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import java.lang.reflect.Method;

public class TestRegistry {
    public static void main(String[] args) {
        System.out.println("Methods in Registry/DefaultedRegistry:");
        for (Method m : Registry.class.getMethods()) {
            if (m.getName().toLowerCase().contains("tag")) {
                System.out.println("Registry: " + m.toString());
            }
        }
    }
}
