package zener.zcomm.recipes;

import java.util.function.Supplier;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import zener.zcomm.Main;

public enum RecipeTypesRegistry {
    HAND_RECIPE_SERIALIZER("hand", HandRecipe.class, HandRecipeSerializer::new);

    private final String pathName;
    private final Class<? extends Recipe<? extends Inventory>> recipeClass;
    private final Supplier<RecipeSerializer<? extends Recipe<? extends Inventory>>> recipeSerializerSupplier;
    private RecipeSerializer<? extends Recipe<? extends Inventory>> serializer;
    private RecipeType<? extends Recipe<? extends Inventory>> type;

    RecipeTypesRegistry(String pathName, Class<? extends Recipe<? extends Inventory>> recipeClass,
            Supplier<RecipeSerializer<? extends Recipe<? extends Inventory>>> recipeSerializerSupplier) {
        this.pathName = pathName;
        this.recipeClass = recipeClass;
        this.recipeSerializerSupplier = recipeSerializerSupplier;
    }

    public static void registerAll() {
        for (RecipeTypesRegistry value : values()) {
            Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(Main.ID, value.pathName), value.serializer());
            value.type = RecipeType.register(value.pathName);
        }
    }

    public RecipeSerializer<? extends Recipe<? extends Inventory>> serializer() {
        if (serializer == null) {
            serializer = recipeSerializerSupplier.get();
        }

        return serializer;
    }

    @SuppressWarnings("unchecked")
    public <T extends Recipe<? extends Inventory>> RecipeType<T> type() {
        return (RecipeType<T>) type(recipeClass);
    }

    @SuppressWarnings({"unchecked"})
    private <T extends Recipe<? extends Inventory>> RecipeType<T> type(Class<T> clazz) {
        if (type == null) {
            type = RecipeType.register(new Identifier(Main.ID, pathName).toString());
        }
        return (RecipeType<T>) type;
    }
    
}
