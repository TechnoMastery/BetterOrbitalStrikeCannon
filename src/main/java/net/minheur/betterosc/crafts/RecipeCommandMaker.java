package net.minheur.betterosc.crafts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minheur.betterosc.recipes.RecipesHandler;
import org.jspecify.annotations.NonNull;

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
                    literal("commandCrafter")

                            .then(literal("craft").then(argument("recipe", IdentifierArgumentType.identifier())
                                            .suggests(RECIPE_SUGGESTIONS).executes(CraftHandler::handleCraft)))

                            .then(literal("see").then(argument("recipe", IdentifierArgumentType.identifier())
                                    .suggests(RECIPE_SUGGESTIONS).executes(CraftHandler::seeRecipe)))

            );

            dispatcher.register(literal("c").redirect(getCraftNode(dispatcher)));
            dispatcher.register(literal("craft").redirect(getCraftNode(dispatcher)));

            dispatcher.register(literal("seeRecipe").redirect(getSeeNode(dispatcher)));
            dispatcher.register(literal("recipeResources").redirect(getSeeNode(dispatcher)));
            dispatcher.register(literal("resourcesFor").redirect(getSeeNode(dispatcher)));
            dispatcher.register(literal("sr").redirect(getSeeNode(dispatcher)));

        }));
    }

    private static CommandNode<ServerCommandSource> getCraftNode(@NonNull CommandDispatcher<ServerCommandSource> dispatcher) {
        return dispatcher.getRoot()
                .getChild("commandCrafter")
                .getChild("craft");
    }
    private static CommandNode<ServerCommandSource> getSeeNode(@NonNull CommandDispatcher<ServerCommandSource> dispatcher) {
        return dispatcher.getRoot()
                .getChild("commandCraft")
                .getChild("see");
    }

}
