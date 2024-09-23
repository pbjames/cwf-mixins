package coremixins.mixin.techguns;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

  @Inject(method = "sendImpactFX", at = @At("HEAD"), remap = false)
  protected void sendImpactFX(
      double x, double y, double z, float pitch, float yaw, int type, CallbackInfo ci) {
    if (this.world.isRemote || type != 2) {
      return;
    }
    List<Block> ALLOWLIST = new ArrayList<Block>();
    ALLOWLIST.add(Blocks.GLASS_PANE);
    System.out.println("im in the mainframe");
    BlockPos localBlockPos = new BlockPos(new Vec3d(x, y, z));
    Block localBlock = this.world.getBlockState(localBlockPos).getBlock();
    if (ALLOWLIST.contains(localBlock)) {
      this.world.destroyBlock(localBlockPos, false);
    }
  }
}
