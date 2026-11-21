package dev.aurum.economy;

import dev.aurum.economy.event.CoinDrops;
import dev.aurum.economy.registry.ModBlockEntities;
import dev.aurum.economy.registry.ModBlocks;
import dev.aurum.economy.registry.ModComponents;
import dev.aurum.economy.registry.ModItemGroups;
import dev.aurum.economy.registry.ModItems;
import dev.aurum.economy.registry.ModParticles;
import dev.aurum.economy.registry.ModVillagers;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aurum: Economy — точка входа мода.
 * Порядок регистрации важен: компоненты -> предметы -> блоки -> всё остальное.
 */
public class AurumMod implements ModInitializer {
	public static final String MOD_ID = "aurum";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModComponents.init();
		ModParticles.init();
		ModItems.init();
		ModBlocks.init();
		ModBlockEntities.init();
		ModVillagers.init();
		ModItemGroups.init();
		CoinDrops.init();
		LOGGER.info("Aurum: Economy загружен — жители теперь принимают монеты вместо изумрудов!");
	}
}
