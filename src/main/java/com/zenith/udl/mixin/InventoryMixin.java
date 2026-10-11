package com.zenith.udl.mixin;

import com.zenith.udl.manager.UdlItemProtection;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow public Player player;
    @Unique private Map<Integer, ItemStack> udl$heldItemsBeforeDrop;

    @ModifyVariable(method = "clearOrCountMatchingItems", at = @At("HEAD"), argsOnly = true)
    private Predicate<ItemStack> udl$excludeProtectedItemsFromClear(Predicate<ItemStack> predicate) {
        if (UdlItemProtection.isDisarmed(player)) return predicate;
        return stack -> !UdlItemProtection.isProtected(stack) && predicate.test(stack);
    }

    @Inject(method = "dropAll", at = @At("HEAD"))
    private void udl$keepProtectedItemsBeforeDrop(CallbackInfo ci) {
        if (player.level().isClientSide || UdlItemProtection.isDisarmed(player)) return;

        Inventory inventory = (Inventory) (Object) this;
        udl$heldItemsBeforeDrop = new LinkedHashMap<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (UdlItemProtection.isProtected(stack)) {
                udl$heldItemsBeforeDrop.put(slot, stack.copy());
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    @Inject(method = "dropAll", at = @At("RETURN"))
    private void udl$restoreProtectedItemsAfterDrop(CallbackInfo ci) {
        if (udl$heldItemsBeforeDrop == null || udl$heldItemsBeforeDrop.isEmpty()) return;
        Inventory inventory = (Inventory) (Object) this;
        for (Map.Entry<Integer, ItemStack> entry : udl$heldItemsBeforeDrop.entrySet()) {
            int slot = entry.getKey();
            if (inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, entry.getValue());
            } else {
                inventory.add(entry.getValue());
            }
        }
        udl$heldItemsBeforeDrop = null;
    }
}
