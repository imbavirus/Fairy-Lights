package za.co.infernos.fairylights;

import net.minecraft.world.item.crafting.Ingredient;
import java.lang.reflect.Method;

public class TestIngredient {
    public static void main(String[] args) {
        System.out.println("Methods in Ingredient:");
        for (Method m : Ingredient.class.getMethods()) {
            System.out.println(m.toString());
        }
    }
}
