package dev.aurum.machines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

public class PressBlock extends Block {
    public PressBlock(Settings settings) { super(settings); }

    private void press(World world, BlockPos pos, ItemStack stack, PlayerEntity player, ItemEntity entity) {
        Item in = stack.getItem();
        Item out = null;

        if (in == Items.IRON_INGOT) out = Registries.ITEM.get(Identifier.of("aurum_machines:iron_plate"));
        else if (in == Items.GOLD_INGOT) out = Registries.ITEM.get(Identifier.of("aurum_machines:gold_plate"));
        else if (in == Items.COPPER_INGOT) out = Registries.ITEM.get(Identifier.of("aurum_machines:copper_plate"));

        if (out != null && out != Items.AIR) {
            stack.decrement(1);
            ItemStack res = new ItemStack(out);
            if (player != null) player.getInventory().offerOrDrop(res);
            else if (entity != null) world.spawnEntity(new ItemEntity(world, pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, res));

            if (!world.isClient) {
                ((ServerWorld)world).spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, 5, 0.1, 0.1, 0.1, 0.05);
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.5f, 1.5f);
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            press(world, pos, stack, player, null);
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof ItemEntity ie) {
            press(world, pos, ie.getStack(), null, ie);
        }
    }
}
