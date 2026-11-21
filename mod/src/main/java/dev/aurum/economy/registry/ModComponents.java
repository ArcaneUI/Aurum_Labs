package dev.aurum.economy.registry;

import com.mojang.serialization.Codec;
import dev.aurum.economy.AurumMod;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModComponents {
	/** Баланс кошелька в AU. Сохраняется в NBT и синхронизируется с клиентом. */
	public static final ComponentType<Long> BALANCE = Registry.register(
		Registries.DATA_COMPONENT_TYPE,
		AurumMod.id("balance"),
		ComponentType.<Long>builder().codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG).build()
	);

	private ModComponents() {
	}

	public static void init() {
	}
}
