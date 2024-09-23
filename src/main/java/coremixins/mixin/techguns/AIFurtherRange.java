package coremixins.mixin.techguns;

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
