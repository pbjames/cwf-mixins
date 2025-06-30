package coremixins.mixin.techguns;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import techguns.entities.projectiles.GenericProjectile;

@Mixin(GenericProjectile.class)
public abstract class BulletsBreakGlass extends Entity {
  private BulletsBreakGlass(World worldIn) {
    super(worldIn);
  }

  @Inject(method = "hitBlock", at = @At("HEAD"), remap = false)
  protected void hitBlock(RayTraceResult rayTraceResultIn, CallbackInfo ci) {
    Set<Block> ALLOW_BLOCKS =
        new HashSet<>(Arrays.asList(new Block[] {Blocks.GLASS, Blocks.GLASS_PANE}));
    BlockPos localBlockPos = rayTraceResultIn.getBlockPos();
    Block localBlock = world.getBlockState(localBlockPos).getBlock();
    if (ALLOW_BLOCKS.contains(localBlock)) world.destroyBlock(localBlockPos, false);
  }
}
