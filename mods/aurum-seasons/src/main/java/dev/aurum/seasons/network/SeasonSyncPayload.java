package dev.aurum.seasons.network;

import dev.aurum.seasons.AurumSeasons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SeasonSyncPayload(int seasonIndex, int dayOfSeason) implements CustomPayload {
    public static final CustomPayload.Id<SeasonSyncPayload> ID = new CustomPayload.Id<>(AurumSeasons.id("season_sync"));
    public static final PacketCodec<RegistryByteBuf, SeasonSyncPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, SeasonSyncPayload::seasonIndex,
        PacketCodecs.INTEGER, SeasonSyncPayload::dayOfSeason,
        SeasonSyncPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
