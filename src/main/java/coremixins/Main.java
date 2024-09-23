package coremixins;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID)
public class Main {
  @Mod.EventHandler
  public void onPreInit(FMLPreInitializationEvent event) {}
}
