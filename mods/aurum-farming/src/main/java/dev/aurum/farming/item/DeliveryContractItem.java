package dev.aurum.farming.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class DeliveryContractItem extends Item {
    public DeliveryContractItem(Settings settings) { super(settings); }

    public static void setTarget(ItemStack stack, String item, int count) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("target_item", item);
        nbt.putInt("target_count", count);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null && !customData.isEmpty()) {
            NbtCompound nbt = customData.copyNbt();
            String targetItemStr = nbt.getString("target_item");
            int targetCount = nbt.getInt("target_count");
            Item targetItem = Registries.ITEM.get(Identifier.of(targetItemStr));
            
            if (targetItem != net.minecraft.item.Items.AIR) {
                int count = 0;
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack invStack = player.getInventory().getStack(i);
                    if (invStack.isOf(targetItem)) count += invStack.getCount();
                }
                
                if (count >= targetCount) {
                    if (!world.isClient) {
                        int removed = 0;
                        for (int i = 0; i < player.getInventory().size() && removed < targetCount; i++) {
                            ItemStack invStack = player.getInventory().getStack(i);
                            if (invStack.isOf(targetItem)) {
                                int take = Math.min(invStack.getCount(), targetCount - removed);
                                invStack.decrement(take);
                                removed += take;
                            }
                        }
                        
                        stack.decrement(1);
                        Item reward = Registries.ITEM.get(Identifier.of("aurum:gold_coin"));
                        if (reward == net.minecraft.item.Items.AIR) reward = net.minecraft.item.Items.EMERALD;
                        int rewardCount = reward == net.minecraft.item.Items.EMERALD ? 4 : 2;
                        
                        player.getInventory().offerOrDrop(new ItemStack(reward, rewardCount));
                        
                        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.PLAYERS, 1f, 1f);
                        ((ServerWorld)world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY()+1, player.getZ(), 10, 0.5, 0.5, 0.5, 0.05);
                    }
                    return TypedActionResult.success(stack, world.isClient());
                } else {
                    if (!world.isClient) player.sendMessage(Text.literal("Недостаточно предметов: " + count + "/" + targetCount).formatted(Formatting.RED), true);
                }
            }
        }
        return TypedActionResult.pass(stack);
    }
}
