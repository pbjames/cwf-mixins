package coremixins.mixin.techguns;

import org.spongepowered.asm.mixin.Mixin;
import techguns.TGuns;

@Mixin(TGuns.class)
public abstract class AIFurtherRange {
  private static final float RANGE_CLOSE = 12.0f;
  private static final float RANGE_SHORT = 24.0f;
  private static final float RANGE_MEDIUM = 48.0f;
  private static final float RANGE_FAR = 80.0f;
}

// @Mixin(GenericProjectile.class)
// public abstract class AIFurtherRange extends Entity {
//  private BulletsBreakGlass(World worldIn) {
//    super(worldIn);
//  }
//
//  @Inject(method = "sendImpactFX", at = @At("HEAD"), remap = false)
//  protected void sendImpactFX(
//      double x, double y, double z, float pitch, float yaw, int type, CallbackInfo ci) {
//    if (this.world.isRemote || type != 2) {
//      return;
//    }
//    List<Block> ALLOWLIST = new ArrayList<Block>();
//    ALLOWLIST.add(Blocks.GLASS_PANE);
//    System.out.println("im in the mainframe");
//    BlockPos localBlockPos = new BlockPos(new Vec3d(x, y, z));
//    Block localBlock = this.world.getBlockState(localBlockPos).getBlock();
//    if (ALLOWLIST.contains(localBlock)) {
//      this.world.destroyBlock(localBlockPos, false);
//    }
//  }
// }
