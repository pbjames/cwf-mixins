package coremixins.mixin.techguns;

import nc.capability.radiation.entity.IEntityRads;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import techguns.radiation.RadRegenerationPotion;

@Mixin(RadRegenerationPotion.class)
public abstract class RadiationUnifyRadaway extends Potion {
  public RadiationUnifyRadaway() {
    super(false, 0xffa2000);
  }

  private double radiation_radaway_amount = 15.0D;

  @Overwrite(remap = false)
  public void performEffect(EntityLivingBase elb, int amplifier) {
    if (elb instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) elb;
      World world = player.world;
      if (world.isRemote) return;
      IEntityRads playerRads = player.getCapability(IEntityRads.CAPABILITY_ENTITY_RADS, null);
      if (playerRads == null) return;
      if (!playerRads.canConsumeRadaway()) {
        playerRads.setConsumedMedicine(false);
        return;
      }
      System.out.println("radregen mixin");
      playerRads.setConsumedMedicine(true);
      playerRads.setRadawayBuffer(
          false, playerRads.getRadawayBuffer(false) + (radiation_radaway_amount));
      playerRads.setRecentRadawayAddition(radiation_radaway_amount);
    }
  }
}
