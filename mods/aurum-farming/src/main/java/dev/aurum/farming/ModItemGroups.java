package dev.aurum.farming;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
public class ModItemGroups {
    public static final ItemGroup FARMING = Registry.register(Registries.ITEM_GROUP, AurumFarming.id("farming"),
        FabricItemGroup.builder().icon(() -> new ItemStack(AurumFarming.TOMATO)).displayName(Text.translatable("itemGroup.aurum_farming")).entries((ctx, entries) -> {
            entries.add(AurumFarming.KITCHEN_TABLE_ITEM); entries.add(AurumFarming.FERTILIZER); entries.add(AurumFarming.DELIVERY_CONTRACT);
            entries.add(AurumFarming.TOMATO_SEEDS); entries.add(AurumFarming.CORN_SEEDS); entries.add(AurumFarming.STRAWBERRY_SEEDS); entries.add(AurumFarming.ONION_SEEDS); entries.add(AurumFarming.RICE_SEEDS);
            entries.add(AurumFarming.TOMATO); entries.add(AurumFarming.CORN); entries.add(AurumFarming.STRAWBERRY); entries.add(AurumFarming.ONION); entries.add(AurumFarming.RICE);
            entries.add(AurumFarming.TOMATO_SOUP); entries.add(AurumFarming.ROASTED_CORN); entries.add(AurumFarming.STRAWBERRY_TART); entries.add(AurumFarming.ONION_RINGS); entries.add(AurumFarming.FARMERS_PAELLA);
        }).build());
    public static void init() {}
}
