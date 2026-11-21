package dev.aurum.seasons;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
public class ModItemGroups {
    public static final ItemGroup SEASONS = Registry.register(Registries.ITEM_GROUP, AurumSeasons.id("seasons"),
        FabricItemGroup.builder().icon(() -> new ItemStack(AurumSeasons.SEASON_CALENDAR)).displayName(Text.translatable("itemGroup.aurum_seasons")).entries((ctx, entries) -> {
            entries.add(AurumSeasons.SEASON_CALENDAR);
        }).build());
    public static void init() {}
}
