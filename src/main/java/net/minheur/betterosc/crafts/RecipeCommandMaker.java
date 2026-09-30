package net.minheur.betterosc.crafts;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minheur.betterosc.recipes.RecipesHandler;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RecipeCommandMaker {

    private static final SuggestionProvider<ServerCommandSource> RECIPE_SUGGESTIONS =
            (context, builder) -> {
                RecipesHandler.mapToIdentifiers().stream()
                        .map(Identifier::toString)
                        .filter(id -> id.startsWith(builder.getRemaining()))
                        .forEach(builder::suggest);

                return builder.buildFuture();
            };


    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {

            dispatcher.register(

                    literal("craft")
                            .then(argument("recipe", IdentifierArgumentType.identifier())
                                    .suggests(RECIPE_SUGGESTIONS)
                            )

            );

        }));
    }

}
