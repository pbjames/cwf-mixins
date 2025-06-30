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
