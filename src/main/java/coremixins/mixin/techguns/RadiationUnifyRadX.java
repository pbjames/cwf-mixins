package coremixins.mixin.techguns;

import nc.capability.radiation.entity.IEntityRads;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import techguns.radiation.RadResistancePotion;

@Mixin(RadResistancePotion.class)
public abstract class RadiationUnifyRadX extends Potion {
  public RadiationUnifyRadX() {
    super(false, 0xffa2000);
  }

  private double radiation_resistance_amount = 10.0D;

  @Override
  public void performEffect(EntityLivingBase elb, int amplifier) {
    if (elb instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) elb;
      World world = player.world;
      if (world.isRemote) return;
      IEntityRads playerRads = player.getCapability(IEntityRads.CAPABILITY_ENTITY_RADS, null);
      if (playerRads == null) return;
      if (!playerRads.canConsumeRadX()) return;
      System.out.println("radx mixin");
      playerRads.setConsumedMedicine(true);
      playerRads.setRadXCooldown(5.0D);
      playerRads.setRecentRadXAddition(radiation_resistance_amount);
      playerRads.setInternalRadiationResistance(
          playerRads.getInternalRadiationResistance() + radiation_resistance_amount);
      playerRads.setRadXUsed(true);
    }
  }
}
