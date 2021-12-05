package zener.zcomm.recipes;

import java.util.List;

import lombok.Getter;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import zener.zcomm.util.RecipeMatcher;
import zener.zcomm.util.inventoryUtils;

public class HandRecipe implements Recipe<Inventory> {
    public static final int INPUT_SLOTS = 10;

    private final Identifier id;
    private final String group;
    private final DefaultedList<Ingredient> ingredientList;
    private final ItemStack output;
    @Getter final ItemStack catalyst;
    @Getter private final DefaultedList<NbtCompound> data;

    public HandRecipe(Identifier id, String group, DefaultedList<Ingredient> ingredientList, ItemStack output, ItemStack catalyst, DefaultedList<NbtCompound> data) {
        System.out.println(output.getOrCreateNbt().asString());
        this.id = id; this.group = group; this.ingredientList = ingredientList; this.output = output; this.catalyst = catalyst; this.data = data;
    }

    @Override
    public boolean matches(Inventory inv, World world) {
        if (inv.size() < 10) return false;

        List<ItemStack> inputList = inventoryUtils.getHandCraftingItems(inv);

        boolean success = RecipeMatcher.findMatches(inputList, ingredientList, data) != null;
        return success;
    }

    public boolean matchCatalyst(Inventory Inventory, World world) {
        ItemStack inventoryCatalyst = Inventory.getStack(40);
        NbtCompound nbt = catalyst.getOrCreateNbt();
        NbtCompound tag = inventoryCatalyst.getOrCreateNbt();
        
        for (String key : nbt.getKeys()) {
            byte type = nbt.get(key).getType();
            if (type == NbtByte.COMPOUND_TYPE || type == NbtByte.BYTE_ARRAY_TYPE || type == NbtByte.INT_ARRAY_TYPE || type == NbtByte.LIST_TYPE || type == NbtByte.LONG_ARRAY_TYPE) {
                continue;
            }
            if (!tag.contains(key, type)) {
                return false;
            }
            if (tag.get(key) != nbt.get(key)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(Inventory inv) {
        ItemStack ret = new ItemStack(output.getItem(), output.getCount());
        ret.setNbt(output.getOrCreateNbt());
        System.out.println(ret.getOrCreateNbt().asString());
        return ret;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= ingredientList.size();
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeTypesRegistry.HAND_RECIPE_SERIALIZER.serializer();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypesRegistry.HAND_RECIPE_SERIALIZER.type();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return ingredientList;
    }

    @Override
    public String getGroup() {
        return group;
    }

}
