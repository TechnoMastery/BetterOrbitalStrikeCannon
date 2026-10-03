package net.minheur.betterosc.crafts;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

public final class CraftHandler {

    public static int handleCraft(CommandContext<ServerCommandSource> source) {
        return 1;
    }

    public static int seeRecipe(CommandContext<ServerCommandSource> source) {
        return 1;
    }

}
