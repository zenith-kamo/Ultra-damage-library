package com.zenith.udl.init;

import com.zenith.udl.Udl;
import com.zenith.udl.item.EndOfLifeSwordItem;
import com.zenith.udl.item.debug.Debug2Item;
import com.zenith.udl.item.debug.Debug3Item;
import com.zenith.udl.item.debug.DebugItem;
import com.zenith.udl.item.PocketWatchItem;
import com.zenith.udl.item.UltraDamageLibrarySwordItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Udl.MODID);

    public static final RegistryObject<Item> UDL_SWORD =
            ITEMS.register("ultra_damage_library_sword", UltraDamageLibrarySwordItem::new);
    public static final RegistryObject<Item> ENDOFLIFE =
            ITEMS.register("end_of_life", EndOfLifeSwordItem::new);
    public static final RegistryObject<Item> PRO_SWORD = Udl.PRO_BUILD
            ? ITEMS.register("pro_sword", ModItems::createProSword)
            : null;
    public static final RegistryObject<Item> POCKET_WATCH =
            ITEMS.register("pocket_watch", PocketWatchItem::new);
    public static final RegistryObject<Item> DEBUG_ITEM =
            ITEMS.register("debug", () -> new DebugItem(new Item.Properties()));
    public static final RegistryObject<Item> DEBUG2_ITEM =
            ITEMS.register("debug2", () -> new Debug2Item(new Item.Properties()));
    public static final RegistryObject<Item> DEBUG3_ITEM =
            ITEMS.register("debug3", () -> new Debug3Item(new Item.Properties()));

        private static Item createProSword() {
                try {
                        return (Item) Class.forName("com.zenith.udl.pro.ProSwordItem")
                                        .getDeclaredConstructor()
                                        .newInstance();
                } catch (ReflectiveOperationException exception) {
                        throw new IllegalStateException("Could not load the PRO sword", exception);
                }
        }
}
