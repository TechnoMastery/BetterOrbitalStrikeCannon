package net.minheur.betterosc.tank;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TankHandler {
    private static final String TANK_TYPE = "tank";
    private static final String TYPE_KEY = "oc_type";
    private static final String UUID_KEY = "tank_uuid";

    private static final Map<UUID, TankContent> TANKS = new ConcurrentHashMap<>();

    public static Map<UUID, TankContent> getTanks() {
        return TANKS;
    }

    public static boolean isTank(ItemStack stack) {
        if (!stack.isOf(Items.NETHER_STAR)) return false;

        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return false;

        NbtCompound nbt = customData.copyNbt();
        return TANK_TYPE.equals(nbt.getString(TYPE_KEY).orElse(null))
                && nbt.getString(UUID_KEY)
                .map(TankHandler::isValidUuid)
                .orElse(false);
    }

    public static @Nullable ItemStack createTank(ItemStack content) {
        if (content.isEmpty()) return null;
        if (!IllegalTankItems.isItemAllowed(content)) return null;

        UUID tankUuid = UUID.randomUUID();
        ItemStack storedStack = content.copyWithCount(1);
        TANKS.put(tankUuid, new TankContent(storedStack, content.getCount()));

        return buildTankFromUUID(tankUuid);
    }

    public static ItemStack buildTankFromUUID(UUID uuid) {
        ItemStack tank = new ItemStack(Items.NETHER_STAR);
        NbtCompound nbt = new NbtCompound();

        nbt.putString(TYPE_KEY, TANK_TYPE);
        nbt.putString(UUID_KEY, uuid.toString());

        tank.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        tank.set(DataComponentTypes.MAX_STACK_SIZE, 1);

        updateTankDisplay(tank);
        return tank;
    }

    public static void updateTankDisplay(ItemStack tank) {
        if (!isTank(tank)) throw new IllegalArgumentException("Can't update non-tank's UI!");

        UUID tankUUID = getTankUUID(tank);

        TankContent content = TANKS.get(tankUUID);
        Item stored = content.item().getItem();
        long amount = content.amount();

        tank.set(DataComponentTypes.CUSTOM_NAME, Text.empty()
                .append(Text.literal("Tank of ").formatted(Formatting.AQUA))
                .append(Text.translatable(stored.getTranslationKey()).formatted(Formatting.AQUA, Formatting.BOLD)));
        tank.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("Amount stored:").formatted(Formatting.DARK_GRAY),
                Text.literal(String.valueOf(amount)).formatted(Formatting.UNDERLINE, Formatting.GOLD)
        )));
    }

    public static UUID getTankUUID(ItemStack tank) {
        NbtCompound nbt = getTankNbt(tank);

        String tankUUID = nbt.getString(UUID_KEY).orElse(null);
        if (tankUUID == null || !isValidUuid(tankUUID)) throw new IllegalStateException("Tank data doesn't have a UUID!");

        return UUID.fromString(tankUUID);
    }
    public static NbtCompound getTankNbt(ItemStack tank) {
        if (!isTank(tank)) throw new IllegalArgumentException("Can't update non-tank's UI!");

        NbtComponent nbtComponent = tank.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null || nbtComponent.isEmpty()) throw new IllegalStateException("No custom data on tank!");
        return nbtComponent.copyNbt();
    }

    private static boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
