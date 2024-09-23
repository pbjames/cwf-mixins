package coremixins.mixin.techguns;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import techguns.items.GenericItemShared;

@Mixin(GenericItemShared.class)
public abstract class StackUpCompatGenericItemShared extends Item {
  @ModifyVariable(method = "addsharedVariant", argsOnly = true, at = @At("HEAD"), remap = false)
  public int injectVariable(int maxStackSize) {
    return Items.AIR.getItemStackLimit();
  }
}
