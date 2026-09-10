package net.minheur.betterosc.mixin;

import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minheur.betterosc.tank.TankHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {
    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void betterosc$rejectTanks(CallbackInfo ci) {
        ScreenHandler handler = (ScreenHandler) (Object) this;
        if (!TankHandler.isTank(handler.getSlot(0).getStack())
                && !TankHandler.isTank(handler.getSlot(1).getStack())) return;

        handler.getSlot(2).setStack(ItemStack.EMPTY);
        ci.cancel();
    }
}
