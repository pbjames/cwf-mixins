package coremixins.mixin.invasions;

import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.difficulty.UtilEntityBuffs;
import CoroUtil.forge.CULog;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.EnumSpawnPlacementType;
import com.corosus.inv.InvLog;
import com.corosus.inv.Invasion;
import com.corosus.inv.InvasionEntitySpawn;
import com.corosus.inv.InvasionManager;
import com.corosus.inv.capabilities.PlayerDataInstance;
import com.corosus.inv.config.ConfigAdvancedOptions;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(InvasionManager.class)
public abstract class CanSpawnAttackHelisInvasion {
  @Overwrite(remap = false)
  public static boolean spawnNewMobFromProfile(EntityLivingBase player, float difficultyScale) {
    System.out.println("IAM ALIVE");
    PlayerDataInstance storage = player.getCapability(Invasion.PLAYER_DATA_INSTANCE, null);
    int minDist =
        ConfigAdvancedOptions.spawnRangeMin; // 20;//ZAConfigSpawning.extraSpawningDistMin;
    int maxDist = ConfigAdvancedOptions.spawnRangeMax; // ZAConfigSpawning.extraSpawningDistMax;
    int range = maxDist * 2;

    Random rand = player.world.rand;

    InvasionEntitySpawn randomEntityList = storage.getRandomEntityClassToSpawn();

    int tryCachedCaveSpotThreshold = 300;
    int tryWaterSurfaceAreasThreshold = 300;

    if (randomEntityList != null) {
      for (int tries = 0; tries < ConfigAdvancedOptions.attemptsPerSpawn; tries++) {
        storage.triesSinceWorkingAnySpawn++;
        int tryX = MathHelper.floor(player.posX) - (range / 2) + (rand.nextInt(range));
        int tryZ = MathHelper.floor(player.posZ) - (range / 2) + (rand.nextInt(range));
        int tryY = MathHelper.floor(player.posY) - (range / 2) + (rand.nextInt(range));
        int surfaceY = player.world.getHeight(new BlockPos(tryX, 0, tryZ)).getY();

        // set position in the solid ground
        // previously it was targetting above ground but not excluding things like tallgrass
        // eg: this fixes mobs not spawning in desert where theres no tallgrass
        surfaceY--;

        // dont factor in y for high elevation change based spawns, otherwise things wont spawn in
        // certain conditions, eg base on high hill
        double distXZ = player.getDistance(tryX, player.posY, tryZ);

        // TODO: make spawn check rules use entities own rules
        /*if (dist < minDist || dist > maxDist ||
                !canSpawnMob(player.world, tryX, tryY, tryZ) || player.world.getLightFromNeighbors(new BlockPos(tryX, tryY, tryZ)) >= 6) {
            continue;
        }*/

        if (distXZ < minDist || distXZ > maxDist) {
          // CULog.dbg("spawnNewMobFromProfile: dist fail, minDist: " + minDist + ", maxDist: " +
          // maxDist + ", tryDist: " + distXZ);
          continue;
        }

        boolean requireSolidGround = true;
        boolean allowWaterSurfaceSpawn = false;
        if (randomEntityList.spawnProfile.spawnType == EnumSpawnPlacementType.WATER
            || randomEntityList.spawnProfile.spawnType == EnumSpawnPlacementType.AIR) {
          requireSolidGround = false;
        }

        int yToUse = surfaceY;

        boolean caveSpawn = false;

        if (randomEntityList.spawnProfile.spawnType == EnumSpawnPlacementType.GROUND) {
          // if near surface
          if (player.posY + 10 > surfaceY) {
            // try surface
            yToUse = surfaceY;
          } else {
            // try cave
            yToUse = tryY;
            caveSpawn = true;
          }
        } else if (randomEntityList.spawnProfile.spawnType == EnumSpawnPlacementType.CAVE) {
          caveSpawn = true;
          yToUse = tryY;
        } else if (randomEntityList.spawnProfile.spawnType == EnumSpawnPlacementType.SURFACE) {
          // redundant given above defaults, but here for sake of clarity
          caveSpawn = false;
          yToUse = surfaceY;
        } else if (randomEntityList.spawnProfile.spawnType == EnumSpawnPlacementType.WATER) {
          yToUse = tryY;
          BlockPos pos = new BlockPos(tryX, yToUse, tryZ);
          IBlockState state = player.world.getBlockState(pos);
          if (state.getMaterial() != Material.WATER
              || player.world.getBlockState(pos.down()).getMaterial() != Material.WATER
              || player.world.getBlockState(pos.up()).isNormalCube()) {
            continue;
          }
        } else if (randomEntityList.spawnProfile.spawnType == EnumSpawnPlacementType.AIR) {
          yToUse = tryY;
          BlockPos pos = new BlockPos(tryX, yToUse, tryZ);
          // IBlockState state = player.world.getBlockState(pos);
          if (!player.world.isAirBlock(pos) || !player.world.isAirBlock(pos.up())) {
            continue;
          }
        }

        // CULog.dbg("spawn type chosen: " + (caveSpawn ? "cave" : "surface"));

        if (caveSpawn
            && storage.triesSinceWorkingCaveSpawn > tryCachedCaveSpotThreshold
            && storage.listGoodCavePositions.size() > 0) {
          CULog.dbg("trying cached cave spot to spawn with");
          BlockPos pos =
              storage.listGoodCavePositions.get(
                  rand.nextInt(storage.listGoodCavePositions.size() - 1));
          tryX = pos.getX();
          yToUse = pos.getY();
          tryZ = pos.getZ();
        }

        // if we cant find any surface area to spawn something, its probably because theyre on an
        // island, support ocean spawning, but dont lockout land spawning
        if (!caveSpawn
            && storage.triesSinceWorkingSolidGroundSpawn > tryWaterSurfaceAreasThreshold) {
          allowWaterSurfaceSpawn = true;
        }

        boolean isWaterSurfaceSpawned = false;

        if (requireSolidGround) {

          storage.triesSinceWorkingSolidGroundSpawn++;

          // TEST
          if (!CoroUtilEntity.canSpawnMobOnGround(player.world, tryX, yToUse, tryZ)) {
            // CULog.dbg("spawnNewMobFromProfile: canSpawnMobOnGround fail");
            if (allowWaterSurfaceSpawn) {

              CULog.dbg("trying surface water to spawn with");

              BlockPos pos = new BlockPos(tryX, surfaceY, tryZ);
              IBlockState state = player.world.getBlockState(pos);
              // if spot at feet isnt water or where head is isnt air, we dont want to spawn them
              // submerged or in solid block
              if (state.getMaterial() != Material.WATER
                  || !CoroUtilBlock.isAir(player.world.getBlockState(pos.up()).getBlock())) {
                continue;
              }

              isWaterSurfaceSpawned = true;
              // dont contribute to counter if its a water surface spawn
              storage.triesSinceWorkingSolidGroundSpawn--;
            } else {
              continue;
            }
          }
        }

        if (ConfigAdvancedOptions.failedTriesBeforeAllowingSpawnInLitAreas != -1) {
          if (!storage.allowSpawnInLitAreas
              && storage.triesSinceWorkingAnySpawn
                  > ConfigAdvancedOptions.failedTriesBeforeAllowingSpawnInLitAreas) {
            // give up on finding a dark spot and allow lit areas
            CULog.dbg(
                "couldnt find a dark area to spawn for "
                    + ConfigAdvancedOptions.failedTriesBeforeAllowingSpawnInLitAreas
                    + " tries, allowing spawning in lit areas now");
            storage.allowSpawnInLitAreas = true;
          }
        }

        boolean skipDarknessCheck =
            storage.allowSpawnInLitAreas || !ConfigAdvancedOptions.mobsMustSpawnInDarkness;

        if (caveSpawn) {
          storage.triesSinceWorkingCaveSpawn++;
          if (!CoroUtilEntity.isInDarkCave(
              player.world, tryX, yToUse, tryZ, true, skipDarknessCheck)) {
            // CULog.dbg("spawnNewMobFromProfile: isInDarkCave fail");
            continue;
          }
        } else {
          if (!skipDarknessCheck
              && player.world.getLightFromNeighbors(new BlockPos(tryX, yToUse + 1, tryZ)) >= 6) {
            // CULog.dbg("spawnNewMobFromProfile: getLightFromNeighbors fail");
            continue;
          }
        }

        try {

          String spawnStr =
              randomEntityList.spawnProfile.entities.get(
                  rand.nextInt(randomEntityList.spawnProfile.entities.size()));

          String spawn = CoroUtilEntity.getEntityNameStringFromNBTLoadedName(spawnStr);
          String spawnStrNBT = CoroUtilEntity.getEntityNBTStringFromNBTLoadedName(spawnStr);

          // hardcoded fixes to convert to AI taskable entities
          if (spawn.equals("minecraft:bat")) {
            spawn = "coroutil:bat_smart";
          }

          Class classToSpawn = CoroUtilEntity.getClassFromRegistry(spawn);
          if (classToSpawn != null) {
            if (EntityLiving.class.isAssignableFrom(classToSpawn)) {
              EntityLiving ent = null;
              NBTTagCompound spawnNBT = null;

              boolean handleSpawning = true;

              if (spawnStrNBT != "") {
                handleSpawning = false;
                try {
                  spawnNBT = JsonToNBT.getTagFromJson(spawnStrNBT);
                  spawnNBT.setString("id", spawn);
                  spawnNBT.setBoolean(UtilEntityBuffs.dataEntityInitialSpawn, true);
                } catch (NBTException nbtexception) {
                  throw new CommandException(
                      "commands.summon.tagError", new Object[] {nbtexception.getMessage()});
                }
                ent =
                    (EntityLiving)
                        AnvilChunkLoader.readWorldEntityPos(
                            spawnNBT, player.world, tryX, yToUse + 1, tryZ, true);
              } else {
                ent =
                    (EntityLiving)
                        classToSpawn
                            .getConstructor(new Class[] {World.class})
                            .newInstance(new Object[] {player.world});
              }

              // set to above the solid block we can spawn on
              ent.setPosition(tryX, yToUse + 1, tryZ);
              ent.onInitialSpawn(
                  ent.world.getDifficultyForLocation(new BlockPos(ent)), (IEntityLivingData) null);
              ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityWaveSpawned, true);
              ent.getEntityData().setBoolean(TaskDigTowardsTarget.dataUseInvasionRules, true);

              // store players name the mob was spawned for
              ent.getEntityData()
                  .setString(UtilEntityBuffs.dataEntityBuffed_PlayerSpawnedFor, player.getName());

              // set cmod data to entity
              if (EntityCreature.class.isAssignableFrom(classToSpawn)) {
                UtilEntityBuffs.registerAndApplyCmods(
                    (EntityCreature) ent, randomEntityList.spawnProfile.cmods, difficultyScale);
              }

              if (handleSpawning) {
                // put into nbt above before entity instanced otherwise
                ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityInitialSpawn, true);
                player.world.spawnEntity(ent);
              }
              ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityInitialSpawn, false);
              // leave this to omniscience task if config says so
              // ent.setAttackTarget(player);

              // no children!
              if (ent instanceof EntityZombie) {
                ((EntityZombie) ent).setChild(false);
              }

              randomEntityList.spawnCountCurrent++;

              storage.triesSinceWorkingAnySpawn = 0;

              if (caveSpawn) {
                storage.triesSinceWorkingCaveSpawn = 0;

                BlockPos pos = new BlockPos(tryX, yToUse, tryZ);

                if (!storage.listGoodCavePositions.contains(pos)) {
                  CULog.dbg("found good cave spot, adding: " + pos);
                  storage.listGoodCavePositions.add(pos);
                }
              }

              // if we arent in water fallback mode and actually got a successfull spawning on
              // ground, reset
              if (requireSolidGround && !isWaterSurfaceSpawned) {
                storage.triesSinceWorkingSolidGroundSpawn = 0;
              }

              // InvLog.dbg("skipDarknessCheck: " + skipDarknessCheck);

              InvLog.dbg(
                  "Spawned "
                      + randomEntityList.spawnCountCurrent
                      + " at "
                      + new BlockPos(tryX, yToUse, tryZ)
                      + " mobs now: "
                      + ent.getName()
                      + (randomEntityList.spawnProfile.spawnType == EnumSpawnPlacementType.GROUND
                          ? (caveSpawn ? " cavespawned" : " surfacespawned")
                          : "")
                      + " "
                      + randomEntityList.spawnProfile.spawnType);
            }
          } else {
            InvLog.err("could not find registered class for entity name: " + spawn);
          }

          // String spawn = storage.getRandomEntityClassToSpawn();

          // Class classToSpawn = spawnables.get(randSpawn);

        } catch (Exception e) {
          InvLog.err("HW_Invasions: error spawning invasion entity: ");
          e.printStackTrace();
        }

        /*EntityZombie entZ = new EntityZombie(player.world);
        entZ.setPosition(tryX, tryY, tryZ);
        entZ.onInitialSpawn(player.world.getDifficultyForLocation(new BlockCoord(entZ)), (IEntityLivingData)null);
        enhanceMobForDifficulty(entZ, difficultyScale);
        player.world.spawnEntityInWorld(entZ);

        entZ.setAttackTarget(player);*/

        // if (ZAConfig.debugConsoleSpawns) ZombieAwareness.dbg("spawnNewMobSurface: " + tryX + ", "
        // + tryY + ", " + tryZ);
        // System.out.println("spawnNewMobSurface: " + tryX + ", " + tryY + ", " + tryZ);

        return true;
      }
    }

    return false;
  }
}
