package dev.aurum.seasons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AurumSeasons implements ModInitializer {
    public static final String MOD_ID = "aurum_seasons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final int DAYS_PER_SEASON = 7;
    public static final int TICKS_PER_DAY = 24000;

    public enum Season {
        SPRING("Весна", Formatting.GREEN, 0.15f),
        SUMMER("Лето", Formatting.YELLOW, 0.3f),
        AUTUMN("Осень", Formatting.GOLD, -0.1f),
        WINTER("Зима", Formatting.AQUA, -0.4f);

        public final String displayName;
        public final Formatting color;
        public final float tempModifier;

        Season(String d, Formatting c, float t) { displayName = d; color = c; tempModifier = t; }
    }

    private static Season currentSeason = Season.SPRING;
    private static int lastAnnouncedDay = -1;

    public static Season getCurrentSeason() { return currentSeason; }

    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }

    public static Season getSeasonForDay(long day) {
        return Season.values()[(int) ((day / DAYS_PER_SEASON) % 4)];
    }

    public static final net.minecraft.item.Item SEASON_CALENDAR = net.minecraft.registry.Registry.register(
        net.minecraft.registry.Registries.ITEM, id("season_calendar"),
        new dev.aurum.seasons.item.SeasonCalendarItem(new net.minecraft.item.Item.Settings())
    );

    @Override
    public void onInitialize() {
        ModItemGroups.init();
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(dev.aurum.seasons.network.SeasonSyncPayload.ID, dev.aurum.seasons.network.SeasonSyncPayload.CODEC);
        
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerWorld world = handler.getPlayer().getServerWorld();
            long day = world.getTimeOfDay() / TICKS_PER_DAY;
            Season season = getSeasonForDay(day);
            int dayOfSeason = (int) (day % DAYS_PER_SEASON) + 1;
            sender.sendPacket(new dev.aurum.seasons.network.SeasonSyncPayload(season.ordinal(), dayOfSeason));
        });

        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.TOOLS).register(entries -> {
            entries.add(SEASON_CALENDAR);
        });

        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
        LOGGER.info("Aurum: Seasons загружен — мир теперь живёт по временам года!");
    }

    private void onWorldTick(ServerWorld world) {
        if (!world.getRegistryKey().equals(World.OVERWORLD)) return;

        long day = world.getTimeOfDay() / TICKS_PER_DAY;
        Season newSeason = getSeasonForDay(day);

        int dayOfSeason = (int) (day % DAYS_PER_SEASON) + 1;
        boolean syncNeeded = false;

        if (newSeason != currentSeason) {
            currentSeason = newSeason;
            syncNeeded = true;
            int dayHash = (int) (day / DAYS_PER_SEASON);
            if (dayHash != lastAnnouncedDay) {
                lastAnnouncedDay = dayHash;
                MinecraftServer server = world.getServer();
                server.getPlayerManager().broadcast(
                    Text.literal("☀ Наступил сезон: " + newSeason.displayName).formatted(newSeason.color, Formatting.BOLD), false);
            }
        }

        // Синхронизация раз в 100 тиков или при смене сезона
        if (syncNeeded || world.getTime() % 100 == 0) {
            dev.aurum.seasons.network.SeasonSyncPayload payload = new dev.aurum.seasons.network.SeasonSyncPayload(currentSeason.ordinal(), dayOfSeason);
            world.getPlayers().forEach(p -> net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p, payload));
        }

        if (world.getTime() % 5 == 0) {
            world.getPlayers().forEach(player -> {
                BlockPos pos = player.getBlockPos();
                net.minecraft.particle.ParticleEffect particle = switch (currentSeason) {
                    case SPRING -> net.minecraft.particle.ParticleTypes.CHERRY_LEAVES;
                    case SUMMER -> net.minecraft.particle.ParticleTypes.END_ROD;
                    case AUTUMN -> net.minecraft.particle.ParticleTypes.FALLING_SPORE_BLOSSOM;
                    case WINTER -> net.minecraft.particle.ParticleTypes.SNOWFLAKE;
                };
                if (world.random.nextFloat() < 0.3f) {
                    double x = pos.getX() + (world.random.nextDouble() - 0.5) * 16;
                    double y = pos.getY() + world.random.nextDouble() * 10;
                    double z = pos.getZ() + (world.random.nextDouble() - 0.5) * 16;
                    world.spawnParticles(particle, x, y, z, 1, 0, 0, 0, 0.0);
                }
            });
        }

        // Зимой: снегопад
        if (currentSeason == Season.WINTER && world.getTime() % 200 == 0) {
            if (!world.isRaining()) world.setWeather(0, 6000, true, false);
        }

        // Летом/Весной: бонус роста, весной цветы
        if ((currentSeason == Season.SUMMER || currentSeason == Season.SPRING) && world.getTime() % 100 == 0) {
            world.getPlayers().forEach(player -> {
                BlockPos pos = player.getBlockPos();
                for (int dx = -4; dx <= 4; dx++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        BlockPos p = pos.add(dx, 0, dz);
                        BlockState state = world.getBlockState(p);
                        if (state.getBlock() instanceof CropBlock crop && !crop.isMature(state) && world.random.nextFloat() < (currentSeason == Season.SUMMER ? 0.15f : 0.05f)) {
                            crop.applyGrowth(world, p, state);
                        }
                        if (currentSeason == Season.SPRING && state.getBlock() == net.minecraft.block.Blocks.GRASS_BLOCK && world.isAir(p.up()) && world.random.nextFloat() < 0.01f) {
                            world.setBlockState(p.up(), net.minecraft.block.Blocks.DANDELION.getDefaultState());
                        }
                    }
                }
            });
        }
    }
}
