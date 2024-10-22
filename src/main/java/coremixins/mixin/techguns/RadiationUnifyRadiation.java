package coremixins.mixin.techguns;

import nc.capability.radiation.entity.IEntityRads;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import techguns.TGSounds;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.radiation.RadiationPotion;

@Mixin(RadiationPotion.class)
public abstract class RadiationUnifyRadiation extends Potion {
  public RadiationUnifyRadiation() {
    super(false, 0xffa2000);
  }

  @Overwrite(remap = false)
  public void performEffect(EntityLivingBase elb, int amplifier) {
    if (elb instanceof EntityPlayer) {
      System.out.println("radiation mixin");
      EntityPlayer player = (EntityPlayer) elb;
      World world = player.world;
      if (world.isRemote) {
        if (amplifier >= 2) {
          elb.world.playSound(
              (EntityPlayer) elb,
              elb.posX,
              elb.posY,
              elb.posZ,
              TGSounds.GEIGER_HIGH,
              SoundCategory.PLAYERS,
              1f,
              1f);
        } else {
          elb.world.playSound(
              (EntityPlayer) elb,
              elb.posX,
              elb.posY,
              elb.posZ,
              TGSounds.GEIGER_LOW,
              SoundCategory.PLAYERS,
              1f,
              1f);
        }
        return;
      }
      IEntityRads playerRads = player.getCapability(IEntityRads.CAPABILITY_ENTITY_RADS, null);
      if (playerRads == null) return;
      int amount =
          techguns.util.MathUtil.clamp(
                  amplifier + 1 - (int) playerRads.getFullRadiationResistance(), 0, 100)
              * 10;
      playerRads.setRadiationLevel(playerRads.getRadiationLevel() + amount * 10000);
    } else if (elb instanceof EntityLiving) {
      int amount = techguns.util.MathUtil.clamp(amplifier + 1, 0, 1000);
      float damage = amount * 0.5f;
      if (damage >= 1.0f) {
        elb.attackEntityFrom(
            TGDamageSource.causeRadiationDamage(null, null, DeathType.BIO), damage);
      }
    }
  }
}
