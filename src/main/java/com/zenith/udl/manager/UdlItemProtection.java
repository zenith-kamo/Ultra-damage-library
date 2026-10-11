package com.zenith.udl.manager;

import com.zenith.udl.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Keeps recovery copies of the two protected UDL items in player persistent data. */
@Mod.EventBusSubscriber(modid = "udl", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class UdlItemProtection {
    private static final String BACKUP_KEY = "udlProtectedItems";
    private static final String DISARMED_KEY = "udlItemProtectionDisarmed";

    private UdlItemProtection() {}

    public static boolean isProtected(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(ModItems.UDL_SWORD.get())
                || stack.is(ModItems.HEAVEN_SKY_ELYTRA.get()));
    }

    public static void disarmCurrentlyHeld(ServerPlayer player) {
        player.getPersistentData().remove(BACKUP_KEY);
        player.getPersistentData().putBoolean(DISARMED_KEY, true);
    }

    public static boolean isDisarmed(net.minecraft.world.entity.player.Player player) {
        return player.getPersistentData().getBoolean(DISARMED_KEY);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        Inventory inventory = player.getInventory();
        if (player.getPersistentData().getBoolean(DISARMED_KEY)) {
            if (!containsProtectedItem(inventory)) {
                player.getPersistentData().remove(BACKUP_KEY);
                player.getPersistentData().remove(DISARMED_KEY);
            }
            return;
        }

        ListTag backups = player.getPersistentData().getList(BACKUP_KEY, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < backups.size(); i++) {
            CompoundTag entry = backups.getCompound(i);
            boolean hasSlot = entry.contains("Slot");
            ItemStack saved = entry.contains("Stack", CompoundTag.TAG_COMPOUND)
                    ? ItemStack.of(entry.getCompound("Stack"))
                    : ItemStack.of(entry);
            if (saved.isEmpty()) continue;
            int slot = hasSlot ? entry.getInt("Slot") : -1;
            if (slot >= 0 && slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, saved.copy());
            } else if (!containsSameItem(inventory, saved)) {
                inventory.add(saved.copy());
            }
        }

        // Refresh saved state from the live item so durability and NBT changes survive recovery.
        ListTag refreshed = new ListTag();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (isProtected(stack)) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("Slot", slot);
                entry.put("Stack", stack.save(new CompoundTag()));
                refreshed.add(entry);
            }
        }
        if (refreshed.isEmpty()) {
            player.getPersistentData().remove(BACKUP_KEY);
        } else {
            player.getPersistentData().put(BACKUP_KEY, refreshed);
        }
    }

    private static boolean containsProtectedItem(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isProtected(inventory.getItem(slot))) return true;
        }
        return false;
    }

    private static boolean containsSameItem(Inventory inventory, ItemStack expected) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (ItemStack.isSameItemSameTags(inventory.getItem(slot), expected)) return true;
        }
        return false;
    }
}
