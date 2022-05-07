package zener.zcomm.commands;

import net.minecraft.text.TranslatableText;

import zener.zcomm.Main;

public class TranslateLib {
    public static TranslatableText MUST_BE_PLAYER = new TranslatableText("command."+Main.ID+".must_be_player");
    public static TranslatableText PERMISSIONS_LOW = new TranslatableText("command."+Main.ID+".permissions_too_low");
    public static TranslatableText VERIFY_SUCCESS = new TranslatableText("command."+Main.ID+".verify.success");
    public static TranslatableText NO_SPACE = new TranslatableText("command."+Main.ID+".no_space");
    public static TranslatableText ADD_TECHNICIAN = new TranslatableText("command."+Main.ID+".added_technician");
}
