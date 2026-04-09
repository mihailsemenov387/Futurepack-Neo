package net.futurepack.registry;

import net.futurepack.item.EScanner.EScannerItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("futurepack");

    // Регистрируем сам предмет сканера
    public static final DeferredItem<Item> E_SCANNER = ITEMS.register("e_scanner",
            () -> new EScannerItem(new Item.Properties().stacksTo(1)));
}