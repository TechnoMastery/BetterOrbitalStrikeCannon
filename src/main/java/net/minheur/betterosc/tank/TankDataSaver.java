package net.minheur.betterosc.tank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TankDataSaver extends PersistentState {
    private static final String KEY = "osc_tanks";

    private final Map<UUID, TankContent> tanks;

    public TankDataSaver() {
        this(new ConcurrentHashMap<>());
    }
    public TankDataSaver(Map<UUID, TankContent> tanks) {
        this.tanks = tanks;
    }

    public static final Codec<TankDataSaver> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(
                    Uuids.CODEC, TankContent.CODEC
            ).fieldOf("tanks").forGetter(state -> state.tanks)
    ).apply(instance, TankDataSaver::new));

    private static final PersistentStateType<TankDataSaver> TYPE =
            new PersistentStateType<>(
                    KEY, TankDataSaver::new, CODEC, null
            );

    public static TankDataSaver get(@NonNull ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        return manager.getOrCreate(TYPE);
    }

    @Contract(pure = true)
    public @NonNull @UnmodifiableView Map<UUID, TankContent> getTanks() {
        return Collections.unmodifiableMap(tanks);
    }

    public void setTank(UUID uuid, TankContent content) {
        tanks.put(uuid, content);
        markDirty();
    }
    public void removeTank(UUID uuid) {
        if (tanks.remove(uuid) != null) {
            markDirty();
        }
    }

}
