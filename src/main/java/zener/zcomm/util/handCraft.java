package zener.zcomm.util;

import java.util.List;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.StringNbtReader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import zener.zcomm.Main;
import zener.zcomm.recipes.HandRecipe;
import zener.zcomm.recipes.RecipeTypesRegistry;
import zener.zcomm.util.listIterator.EnumeratedItem;

public class handCraft {


    public static NbtCompound readTags(JsonObject json) {

        String jsonString = json.toString();
        try {
            NbtCompound tag = StringNbtReader.parse(jsonString);
            return tag;
        } catch (CommandSyntaxException e) {
            throw new JsonParseException(e.getMessage());
        }
    }
    
    public static boolean craft(World world, PlayerEntity user) {
        List<? extends HandRecipe> recipeList = Objects.requireNonNull(world).getRecipeManager()
                    .getAllMatches(RecipeTypesRegistry.HAND_RECIPE_SERIALIZER.type(), user.getInventory(), world);

        if (recipeList == null) {
            user.sendMessage(new TranslatableText("crafting."+Main.identifier+".no_recipe"), false);
            return false;
        }

        if (recipeList.isEmpty()) {
            user.sendMessage(new TranslatableText("crafting."+Main.identifier+".no_recipe"), false);
            return false;
        }

        if (recipeList.size() > 1) {
            user.sendMessage(new TranslatableText("crafting."+Main.identifier+".multiple_recipe_conflict"), false);
            return false;
        }

        if (!recipeList.get(0).matchCatalyst(user.getInventory(), world)) {
            user.sendMessage(new TranslatableText("crafting."+Main.identifier+".no_catalyst"), false);
            return false;
        }

        boolean success = true;
        PlayerInventory inv = user.getInventory();
        for (EnumeratedItem<Ingredient> ingredient : listIterator.enumerate(recipeList.get(0).getIngredients())) {
            boolean found = false;
            for (int i = 0; i < 9; i++) {
                if (ingredient.item.test(inv.getStack(i)) && RecipeMatcher.checkNbt(recipeList.get(0).getData().get(ingredient.index), inv.getStack(i).getOrCreateNbt())){
                    
                    found = true;
                    ItemStack item = inv.getStack(i);
                    if (item.getCount() > 1) {
                        item.setCount(item.getCount()-1);
                    } else {
                        inv.removeStack(i);
                    }
                    break;
                }
            }
            if (!found) {
                if (ingredient.item.test(inv.getStack(40))) {
                    found = true;
                    ItemStack item = inv.getStack(40);
                    if (item.getCount() > 1) {
                        item.setCount(item.getCount()-1);
                    } else {
                        inv.removeStack(40);
                    }
                }
            }
            if (!found) {
                user.sendMessage(new TranslatableText("crafting."+Main.identifier+".recipe_item_not_found"), false);
                success = false;
            }
        }
        if (success) {
            ItemStack output = recipeList.get(0).craft(inv);
            inv.insertStack(output);
        }
        inv.markDirty();

        return true;
    }

}
