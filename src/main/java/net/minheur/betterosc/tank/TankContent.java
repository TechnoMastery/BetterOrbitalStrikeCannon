package net.minheur.betterosc.tank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

public record TankContent(ItemStack item, long amount) {
    public static final Codec<TankContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(TankContent::item),
            Codec.LONG.fieldOf("amount").forGetter(TankContent::amount)
    ).apply(instance, TankContent::new));
}
