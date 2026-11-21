package dev.aurum.economy.block;

import dev.aurum.economy.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CoinPedestalBlockEntity extends BlockEntity {
	private ItemStack stack = ItemStack.EMPTY;

	public CoinPedestalBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.COIN_PEDESTAL, pos, state);
	}

	public ItemStack getStack() {
		return stack;
	}

	public void setStack(ItemStack newStack) {
		this.stack = newStack;
		markDirty();
		if (world != null) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (!stack.isEmpty()) {
			nbt.put("Item", stack.encode(lookup));
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		stack = nbt.contains("Item")
			? ItemStack.fromNbt(lookup, nbt.getCompound("Item")).orElse(ItemStack.EMPTY)
			: ItemStack.EMPTY;
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup lookup) {
		return createNbt(lookup);
	}
}
