package dev.aurum.oceans;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
public class ModItemGroups {
    public static final ItemGroup OCEANS = Registry.register(Registries.ITEM_GROUP, AurumOceans.id("oceans"),
        FabricItemGroup.builder().icon(() -> new ItemStack(AurumOceans.PEARL)).displayName(Text.translatable("itemGroup.aurum_oceans")).entries((ctx, entries) -> {
            entries.add(AurumOceans.FISHING_NET); entries.add(AurumOceans.PEARL_AMULET); entries.add(AurumOceans.SUNKEN_CHEST);
            entries.add(AurumOceans.PEARL); entries.add(AurumOceans.BLACK_PEARL); entries.add(AurumOceans.SHELL); entries.add(AurumOceans.STARFISH);
            entries.add(AurumOceans.CRAB_MEAT); entries.add(AurumOceans.COOKED_CRAB);
            entries.add(AurumOceans.CORAL_LAMP); entries.add(AurumOceans.DOCK_PLANKS); entries.add(AurumOceans.BARNACLE_BLOCK);
        }).build());
    public static void init() {}
}
