package net.minheur.betterosc.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
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

    @Shadow
    public abstract float getHeadYaw();

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
        String targetWorldSt = nbt.getString("dim", null);

        if (x == null || y == null || z == null || targetWorldSt == null) return;

        if (getEntity() == null || getEntity().getEntityWorld() == null) return;
        ServerWorld world = (ServerWorld) getEntity().getEntityWorld();
        MinecraftServer server = world.getServer();
        if (server == null) return;

        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(targetWorldSt));
        ServerWorld targetWorld = server.getWorld(worldKey);
        if (targetWorld == null) return;

        String targetUUID = nbt.getString("target").orElse(null);
        LivingEntity target = targetUUID == null ? (LivingEntity) (Object) this : this.getEntity().getEntityWorld().getPlayerByUuid(UUID.fromString(targetUUID));
        if (target == null) return;

        final double preX = getEntity().getX(), preY = getEntity().getY(), preZ = getEntity().getZ();

        world.playSound(null, preX, preY, preZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        world.spawnParticles(ParticleTypes.PORTAL, preX, preY + (double) 1.0F, preZ, 50, 0.5F, 0.5F, 0.5F, 0.5F);
        target.teleport(targetWorld, (double)x + (double)0.5F, y, (double)z + (double)0.5F, Set.of(), getHeadYaw(), 0.0f, true);
        world.playSound(null, (double)x + (double)0.5F, y, (double)z + (double)0.5F, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        world.spawnParticles(ParticleTypes.PORTAL, (double)x + (double)0.5F, y + 1, (double)z + (double)0.5F, 50, 0.5F, 0.5F, 0.5F, 0.5F);
    }
}
