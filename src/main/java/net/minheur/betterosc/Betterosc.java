package net.minheur.betterosc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minheur.betterosc.crafts.DefaultRecipesHandler;
import net.minheur.betterosc.recipes.RecipesHandler;
import net.minheur.betterosc.tank.TankCommands;

import java.nio.file.Path;

public final class Betterosc implements ModInitializer {

    public static final Path resourceRoot = FabricLoader.getInstance()
            .getModContainer("betterosc").orElseThrow().findPath("").orElseThrow();

    @Override
    public void onInitialize() {
        ConfigHandler.load();
        UsedItemsHandler.load();
        RecipesHandler.loadRecipes();
        DefaultRecipesHandler.checkAndCreate();

        ExecutorHandler.registerUsage();

        ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> {
            ConfigHandler.save();
            UsedItemsHandler.save();
        });

        CommandRegister.register();
        TankCommands.register();
    }

    public static void breakOrbitalCallItem(ServerPlayerEntity player, Hand hand, ItemStack stack) {
        player.sendEquipmentBreakStatus(stack.getItem(), hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        if (hand == Hand.MAIN_HAND) {
            player.getInventory().setStack(player.getInventory().getSlotWithStack(stack), ItemStack.EMPTY);
        } else {
            player.getInventory().setStack(player.getInventory().size() - 1, ItemStack.EMPTY);
        }

    }
}
