package net.minheur.betterosc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minheur.betterosc.plugin.DefaultRecipesHandler;
import net.minheur.betterosc.plugin.CommandCrafterPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public final class Betterosc implements ModInitializer {

    public static final String MODID = "betterosc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final AtomicBoolean isCommandCrafterModPresent = new AtomicBoolean(false);

    @Override
    public void onInitialize() {
        ConfigHandler.load();
        UsedItemsHandler.load();

        if (FabricLoader.getInstance().isModLoaded("craftbycommands")) isCommandCrafterModPresent.set(true);

        if (isCommandCrafterModPresent.get()) {
            DefaultRecipesHandler.checkAndCreate();
            new CommandCrafterPlugin();
        }

        ExecutorHandler.registerUsage();
        ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> {
            ConfigHandler.save();
            UsedItemsHandler.save();
        });

        CommandRegister.register();
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
