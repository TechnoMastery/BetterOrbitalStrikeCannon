package net.minheur.betterosc.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;
import net.minheur.betterosc.tank.TankHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {
    @Shadow @Final ItemStack result;

    @Inject(method = "matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z", at = @At("HEAD"), cancellable = true)
    private void betterosc$rejectTankForBeacon(
            CraftingRecipeInput input,
            World world,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!result.isOf(Items.BEACON)) return;

        for (ItemStack stack : input.getStacks()) {
            if (TankHandler.isTank(stack)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
