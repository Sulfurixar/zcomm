package zener.zcomm.util;

import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;

public class RecipeMatcher {

    public static boolean checkNbt(NbtCompound ingredientNbt, NbtCompound itemNbt) {

        if (ingredientNbt.isEmpty()) {
            return true;
        }

        for (String key : ingredientNbt.getKeys()) {
            byte type = ingredientNbt.get(key).getType();
            if (type == NbtByte.COMPOUND_TYPE || type == NbtByte.BYTE_ARRAY_TYPE || type == NbtByte.INT_ARRAY_TYPE || type == NbtByte.LIST_TYPE || type == NbtByte.LONG_ARRAY_TYPE) {
                continue;
            }
            if (!itemNbt.contains(key, type)) {
                continue;
            }
            if (itemNbt.get(key) == ingredientNbt.get(key)) {
                return true;
            }
        }

        return false;

    }
    
    public static <T> int[] findMatches(List<T> inputs, List<? extends Predicate<T>> tests, List<NbtCompound> IngredientData) {

        int elements = inputs.size();
        int test_count = tests.size();
        
        int[] ret = new int[test_count];
        for (int x = 0; x < test_count; x++) ret[x] = -1;

        BitSet data = new BitSet(test_count);
        for (int x = 0; x < test_count; x++) {
            int matched = 0;
            Predicate<T> test = tests.get(x);

            for (int y = 0; y < elements; y++) {
                if (data.get(y)) continue;

                if (test.test(inputs.get(y))) {
                    NbtCompound nbt = IngredientData.get(x);
                    if (nbt.isEmpty()) {
                            data.set(y);
                            matched++;
                            break;
                    }
                    NbtCompound tag = ((ItemStack)inputs.get(y)).getOrCreateNbt();
                    if (checkNbt(nbt, tag)) {
                        data.set(y);
                        matched++;
                        break;
                    }
                }
            }

            if (matched == 0) {
                return null; //We have a test that matched none of the inputs
            }

        }

        byte[] dat = data.toByteArray();
        for (int i = 0; i < dat.length; i++) {
            ret[i] = dat[i];
        }

        return ret;
    }

}