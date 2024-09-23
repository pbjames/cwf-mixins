package coremixins.mixin.techguns;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.items.GenericItem;

@Mixin(GenericItem.class)
public abstract class StackUpCompatGenericItem extends Item {
  @Inject(method = "GenericItem", at = @At("RETURN"), remap = false)
  public void cons(String name, boolean addToItemList, CallbackInfo ci) {
    setMaxStackSize(Items.AIR.getItemStackLimit());
  }
}
