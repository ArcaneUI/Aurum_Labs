package dev.aurum.backpacks.network;

import dev.aurum.backpacks.AurumBackpacks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record OpenBackpackPayload(boolean ignored) implements CustomPayload {
    public static final CustomPayload.Id<OpenBackpackPayload> ID = new CustomPayload.Id<>(AurumBackpacks.id("open_backpack"));
    public static final PacketCodec<RegistryByteBuf, OpenBackpackPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOL, OpenBackpackPayload::ignored,
        OpenBackpackPayload::new
    );
    @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
