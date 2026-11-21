package dev.aurum.oceans.block;

import dev.aurum.oceans.AurumOceans;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SunkenChestBlock extends Block {
    public SunkenChestBlock(Settings settings) { super(settings); }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            world.breakBlock(pos, false);
            Item gold = Registries.ITEM.get(Identifier.of("aurum:gold_coin"));
            if (gold == net.minecraft.item.Items.AIR) gold = net.minecraft.item.Items.GOLD_INGOT;
            
            player.getInventory().offerOrDrop(new ItemStack(AurumOceans.PEARL, 1 + world.random.nextInt(3)));
            player.getInventory().offerOrDrop(new ItemStack(AurumOceans.SHELL, 1 + world.random.nextInt(2)));
            player.getInventory().offerOrDrop(new ItemStack(gold, 1 + world.random.nextInt(2)));
            
            world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 1f, 1f);
            ((ServerWorld)world).spawnParticles(ParticleTypes.BUBBLE_POP, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 10, 0.2, 0.2, 0.2, 0.1);
        }
        return ActionResult.success(world.isClient);
    }
}
