package coremixins.mixin.techguns;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import techguns.items.GenericItemShared;

@Mixin(GenericItemShared.class)
public abstract class StackUpCompatGenericItemShared extends Item {
  @ModifyVariable(
      method =
          "addsharedVariant(Ljava/lang/String;ZLtechguns/api/tginventory/TGSlotType;IZ)Lnet/minecraft/item/ItemStack;",
      argsOnly = true,
      at = @At("HEAD"),
      remap = false)
  public int injectVariable(int maxStackSize) {
    return Items.AIR.getItemStackLimit();
  }

  @Inject(
      method =
          "addsharedVariant(Ljava/lang/String;ZLtechguns/api/tginventory/TGSlotType;IZ)Lnet/minecraft/item/ItemStack;",
      at = @At("RETURN"),
      remap = false)
  public void addsharedVariant(CallbackInfoReturnable<ItemStack> cir) {
    System.out.println("I AM WORKING");
  }
}

// public ItemStack addsharedVariant( String name, boolean useRenderHack, TGSlotType slottype, int
// maxStackSize, boolean enabled) {
//    int newMeta = sharedItems.size();
//    sharedItems.add(
//        new SharedItemEntry(name, newMeta, slottype, (short) maxStackSize, useRenderHack,
// enabled));
//    return new ItemStack(this, 1, newMeta);
//  }
