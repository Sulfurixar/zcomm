package zener.zcomm.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import zener.zcomm.util.handCraft;

public class HandRecipeSerializer implements RecipeSerializer<HandRecipe>{
    
    private static DefaultedList<Ingredient> readIngredients(JsonArray ingredientArray) {
        DefaultedList<Ingredient> ingredientList = DefaultedList.of();

        for (int i = 0; i < ingredientArray.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i));
            if (ingredient.getMatchingStacks() != null && ingredient.getMatchingStacks().length > 0) {
                ingredientList.add(ingredient);
            }
        }

        return ingredientList;
    }

    private static DefaultedList<NbtCompound> readIngredientData(JsonArray ingredientArray) {
        DefaultedList<NbtCompound> ingredientData = DefaultedList.of();

        for (int i = 0; i < ingredientArray.size(); i++) {
            Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i));
            if (ingredient.getMatchingStacks() != null && ingredient.getMatchingStacks().length > 0) {
                NbtCompound data = handCraft.readTags(JsonHelper.getObject(((JsonObject)ingredientArray.get(i)), "data", new JsonObject()));
                ingredientData.add(data);
            }
        }

        return ingredientData;
    }

    @Override
    public HandRecipe read(Identifier id, JsonObject json) {
        final String groupIn = JsonHelper.getString(json, "group", "");
        JsonArray ingredients = JsonHelper.getArray(json, "ingredients");
        final DefaultedList<Ingredient> inputItemsIn = readIngredients(ingredients);
        final DefaultedList<NbtCompound> inputItemsData = readIngredientData(ingredients);
        if (inputItemsIn.isEmpty()) {
            throw new JsonParseException("No ingredients for zcomm recipe");
        } else if (inputItemsIn.size() > HandRecipe.INPUT_SLOTS) {
            throw new JsonParseException("Too many ingredients for zcomm recipe! The max is " + HandRecipe.INPUT_SLOTS);
        } else {
            final JsonObject jsonCatalyst = JsonHelper.getObject(json, "catalyst");
            final JsonObject jsonResult = JsonHelper.getObject(json, "result");
            
            final ItemStack catalyst = new ItemStack(JsonHelper.getItem(jsonCatalyst, "item"), JsonHelper.getInt(jsonCatalyst, "count", 1));
            catalyst.setNbt(handCraft.readTags(JsonHelper.getObject(jsonCatalyst, "data", new JsonObject())));
            final ItemStack outputIn = new ItemStack(JsonHelper.getItem(jsonResult, "item"), JsonHelper.getInt(jsonResult, "count", 1));
            outputIn.setNbt(handCraft.readTags(JsonHelper.getObject(jsonResult, "data", new JsonObject())));
            
            return new HandRecipe(id, groupIn, inputItemsIn, outputIn, catalyst, inputItemsData);
        }
    }

    @Override
    public HandRecipe read(Identifier id, PacketByteBuf buf) {
        String groupIn = buf.readString(32767);

        int ingredientSize = buf.readVarInt();
        DefaultedList<Ingredient> ingredientList = DefaultedList.ofSize(ingredientSize, Ingredient.EMPTY);
        for (int j = 0; j < ingredientList.size(); ++j) {
            ingredientList.set(j, Ingredient.fromPacket(buf));
        }

        ItemStack outputIn = buf.readItemStack();
        ItemStack catalyst = buf.readItemStack();

        int dataSize = buf.readVarInt();
        DefaultedList<NbtCompound> data = DefaultedList.ofSize(dataSize, new NbtCompound());
        for (int j = 0; j < data.size(); j++) {
            data.set(j, buf.readNbt());
        }
        return new HandRecipe(id, groupIn, ingredientList, outputIn, catalyst, data);
    }

    @Override
    public void write(PacketByteBuf buf, HandRecipe recipe) {
        buf.writeString(recipe.getGroup());

        buf.writeVarInt(recipe.getIngredients().size());
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.write(buf);
        }

        buf.writeItemStack(recipe.getOutput());
        buf.writeItemStack(recipe.getCatalyst());

        buf.writeVarInt(recipe.getData().size());
        for (NbtCompound nbt : recipe.getData()) {
            buf.writeNbt(nbt);
        }
    }
}
