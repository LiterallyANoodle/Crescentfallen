package fracture.mod.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
//import fracture.mod.world.data.DestructionEventData;

	// WIP

@Mod.EventBusSubscriber
public class OverworldDependencyStripper {

    private static final int MOON_DIM_ID = -28; 
    private static final int EVENT_COMPLETE_TICK = 72000; 


    //WARNING: CURRENTLY THIS IS BUGGED AND CONTINUES RUNNING EVERY SINGLE TICK AFTER THE EVENT ENDS
    public static void evaluateOverworldUnload(WorldServer overworld) {
        if (overworld.getWorldTime() >= EVENT_COMPLETE_TICK) {
            DimensionManager.keepDimensionLoaded(0, false);
            //System.out.println("[Crescentfallen] Event ended, Dim 0 KeepLoaded set to false");
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        checkAndReroutePlayer(event.player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        checkAndReroutePlayer(event.player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.world.isRemote) {
            if (event.player.ticksExisted % 20 == 0) {
                checkAndReroutePlayer(event.player);
            }
        }
    }

    private static void checkAndReroutePlayer(Entity playerEntity) {
        if (!(playerEntity instanceof EntityPlayerMP)) return;
        
        EntityPlayerMP player = (EntityPlayerMP) playerEntity;
        if (!player.isEntityAlive()) return;
        
        if (player.dimension == 0) {
            if (player.world.getWorldTime() >= EVENT_COMPLETE_TICK) {
            	forcePlayerToMoon(player);
            }
        }
    }

    private static void forcePlayerToMoon(EntityPlayerMP player) {
        WorldServer targetWorld = player.getServer().getWorld(MOON_DIM_ID);
        
        if (targetWorld != null) {
            player.getServer().getPlayerList().transferPlayerToDimension(
                player, 
                MOON_DIM_ID, 
                new EventTeleporter(targetWorld)
            );
        }
    }
    
    public static class EventTeleporter extends Teleporter {
        public EventTeleporter(WorldServer worldIn) {
            super(worldIn);
        }

        @Override
        public void placeInPortal(Entity entityIn, float rotationYaw) {
            BlockPos spawn = this.world.getSpawnPoint();
            BlockPos safePos = this.world.getTopSolidOrLiquidBlock(spawn);
            
            entityIn.setLocationAndAngles(safePos.getX() + 0.5D, safePos.getY() + 1.0D, safePos.getZ() + 0.5D, entityIn.rotationYaw, 0.0F);
            
            entityIn.motionX = 0.0D;
            entityIn.motionY = 0.0D;
            entityIn.motionZ = 0.0D;
        }

        @Override
        public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
            return false;
        }

        @Override
        public boolean makePortal(Entity entityIn) {
            return true;
        }

        @Override
        public void removeStalePortalLocations(long worldTime) {
        }
    }
}