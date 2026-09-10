package net.minheur.betterosc.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minheur.betterosc.UsedItemsHandler;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;
import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract @Nullable LivingEntity getEntity();

    @Inject(
            method = "tryUseDeathProtector",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;sendEntityStatus(Lnet/minecraft/entity/Entity;B)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void betterosc$afterTotemPop(
            DamageSource source,
            CallbackInfoReturnable<Boolean> cir,
            ItemStack totemStack
    ) {
        NbtComponent compo = totemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (compo == null) return;
        NbtCompound nbt = compo.copyNbt();
        String uuidString = nbt.getString("uuid").orElse(null);

        if (uuidString == null) return;
        UUID uuid = UUID.fromString(uuidString);

        if (UsedItemsHandler.getUsedItems().contains(uuid)) return;
        UsedItemsHandler.getUsedItems().add(uuid);

        Integer x = nbt.getInt("x").orElse(null);
        Integer y = nbt.getInt("y").orElse(null);
        Integer z = nbt.getInt("z").orElse(null);

        if (x == null || y == null || z == null) return;

        String targetUUID = nbt.getString("target").orElse(null);
        LivingEntity target = targetUUID == null ? (LivingEntity) (Object) this : this.getEntity().getEntityWorld().getPlayerByUuid(UUID.fromString(targetUUID));
        if (target == null) return;
        target.requestTeleport(x, y, z);
    }
}
