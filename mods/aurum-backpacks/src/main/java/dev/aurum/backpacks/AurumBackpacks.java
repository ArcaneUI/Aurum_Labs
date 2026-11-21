package dev.aurum.backpacks;

import net.fabricmc.api.ModInitializer;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AurumBackpacks implements ModInitializer {
    public static final String MOD_ID = "aurum_backpacks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }

    public static final ComponentType<Boolean> AUTO_PICKUP = Registry.register(
        Registries.DATA_COMPONENT_TYPE, id("auto_pickup"),
        ComponentType.<Boolean>builder().codec(com.mojang.serialization.Codec.BOOL).packetCodec(net.minecraft.network.codec.PacketCodecs.BOOL).build()
    );

    // 5 уровней рюкзаков
    public static final Item LEATHER_BACKPACK = register("leather_backpack",
        new BackpackItem(new Item.Settings().maxCount(1), 9, "leather"));
    public static final Item IRON_BACKPACK = register("iron_backpack",
        new BackpackItem(new Item.Settings().maxCount(1), 18, "iron"));
    public static final Item GOLD_BACKPACK = register("gold_backpack",
        new BackpackItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON), 27, "gold"));
    public static final Item DIAMOND_BACKPACK = register("diamond_backpack",
        new BackpackItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE), 36, "diamond"));
    public static final Item NETHERITE_BACKPACK = register("netherite_backpack",
        new BackpackItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof(), 45, "netherite"));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, id(name), item);
    }

    @Override
    public void onInitialize() {
        ModItemGroups.init();
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(dev.aurum.backpacks.network.OpenBackpackPayload.ID, dev.aurum.backpacks.network.OpenBackpackPayload.CODEC);
        
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(dev.aurum.backpacks.network.OpenBackpackPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                net.minecraft.server.network.ServerPlayerEntity player = context.player();
                for (int i = 0; i < player.getInventory().size(); i++) {
                    net.minecraft.item.ItemStack s = player.getInventory().getStack(i);
                    if (s.getItem() instanceof BackpackItem bp) {
                        bp.openBackpack(player, s);
                        break;
                    }
                }
            });
        });

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(world -> {
            for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
                java.util.List<net.minecraft.item.ItemStack> backpacks = new java.util.ArrayList<>();
                for (int i = 0; i < player.getInventory().size(); i++) {
                    net.minecraft.item.ItemStack s = player.getInventory().getStack(i);
                    if (s.getItem() instanceof BackpackItem bp && s.getOrDefault(AUTO_PICKUP, false)) {
                        backpacks.add(s);
                    }
                }
                if (backpacks.isEmpty()) continue;
                
                world.getEntitiesByClass(net.minecraft.entity.ItemEntity.class, player.getBoundingBox().expand(2.0, 1.0, 2.0), e -> !e.cannotPickup() && e.isAlive()).forEach(itemEntity -> {
                    net.minecraft.item.ItemStack stack = itemEntity.getStack();
                    for (net.minecraft.item.ItemStack bpStack : backpacks) {
                        net.minecraft.component.type.ContainerComponent comp = bpStack.getOrDefault(net.minecraft.component.DataComponentTypes.CONTAINER, net.minecraft.component.type.ContainerComponent.DEFAULT);
                        net.minecraft.inventory.SimpleInventory inv = new net.minecraft.inventory.SimpleInventory(((BackpackItem)bpStack.getItem()).getSlots());
                        comp.copyTo(inv.getHeldStacks());
                        
                        boolean hasSimilar = false;
                        for (int i=0; i<inv.size(); i++) {
                            if (!inv.getStack(i).isEmpty() && net.minecraft.item.ItemStack.areItemsAndComponentsEqual(inv.getStack(i), stack)) {
                                hasSimilar = true; break;
                            }
                        }
                        
                        if (hasSimilar) {
                            net.minecraft.item.ItemStack leftover = inv.addStack(stack);
                            bpStack.set(net.minecraft.component.DataComponentTypes.CONTAINER, net.minecraft.component.type.ContainerComponent.fromStacks(inv.getHeldStacks()));
                            if (leftover.isEmpty()) {
                                itemEntity.discard();
                                world.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ENTITY_ITEM_PICKUP, net.minecraft.sound.SoundCategory.PLAYERS, 0.2f, (world.random.nextFloat() - world.random.nextFloat()) * 1.4f + 2.0f);
                                break;
                            } else {
                                itemEntity.setStack(leftover);
                            }
                        }
                    }
                });
            }
        });

        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.TOOLS).register(entries -> {
            entries.add(LEATHER_BACKPACK); entries.add(IRON_BACKPACK); entries.add(GOLD_BACKPACK); entries.add(DIAMOND_BACKPACK); entries.add(NETHERITE_BACKPACK);
        });
        LOGGER.info("Aurum: Backpacks загружен — 5 уровней рюкзаков готовы!");
    }
}
