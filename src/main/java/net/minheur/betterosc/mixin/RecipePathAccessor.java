package net.minheur.betterosc.mixin;

import net.minheur.commandcrafter.crafts.DefaultRecipesHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(DefaultRecipesHandler.class)
public interface RecipePathAccessor {
    @Accessor("RECIPE_FOLDER")
    static Path osc$RECIPE_FOLDER() { // TODO: on update 1.3 of CommandCrafter, field will be public
        throw new AssertionError();
    }

}
