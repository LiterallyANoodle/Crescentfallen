package fracture.mod.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class DestructionEventHandler {

    private static long lastKnownTime = -1;

    public static class DamageSourceHomeworld extends DamageSource {
        public DamageSourceHomeworld() {
            super("homeworld_collapse");
            this.setDamageBypassesArmor();
            this.setDamageAllowedInCreativeMode();
            this.setDamageIsAbsolute();
        }

        @Override
        public ITextComponent getDeathMessage(EntityLivingBase entity) {
            return new TextComponentString(entity.getName() + " wished to die with their Homeworld.");
        }
    }

    private static final DamageSourceHomeworld COLLAPSE_DAMAGE = new DamageSourceHomeworld();

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || event.phase == TickEvent.Phase.START || event.world.provider.getDimension() != 0) {
            return;
        }
        
        if (!fracture.mod.util.handlers.ConfigHandler.enableDestructionEvent) {
            return;
        }

        // Disable Rain in the Overworld completely
        //NOTE: FIX THIS
        if (event.world.getWorldInfo().isRaining() || event.world.getWorldInfo().isThundering()) {
            event.world.getWorldInfo().setRaining(false);
            event.world.getWorldInfo().setRainTime(0);
            event.world.getWorldInfo().setThundering(false);
            event.world.getWorldInfo().setThunderTime(0);
        }

        long currentTime = event.world.getWorldTime();

        // CONTINUOUS CULL ENFORCEMENT
        // If the world time is Day 3 End (72000) or beyond, lock the Overworld
        if (currentTime >= 72000) {
            WorldServer worldServer = (WorldServer) event.world;
            for (net.minecraft.entity.player.EntityPlayer player : worldServer.playerEntities) {
                if (player.isEntityAlive()) {
                    player.attackEntityFrom(COLLAPSE_DAMAGE, Float.MAX_VALUE);
                }
            }
            OverworldDependencyStripper.evaluateOverworldUnload(worldServer); 
        }

        if (lastKnownTime == -1) {
            lastKnownTime = currentTime;
            return;
        }

        // If a player uses /time set 0, reset the tracking so the event can play again
        if (currentTime < lastKnownTime) {
            lastKnownTime = currentTime;
            return;
        }

        // Check milestones
        checkMilestone(1, lastKnownTime, currentTime, (WorldServer) event.world);
        checkMilestone(600, lastKnownTime, currentTime, (WorldServer) event.world);
        checkMilestone(24000, lastKnownTime, currentTime, (WorldServer) event.world);
        checkMilestone(48000, lastKnownTime, currentTime, (WorldServer) event.world);
        checkMilestone(72000, lastKnownTime, currentTime, (WorldServer) event.world);

        lastKnownTime = currentTime;
    }

    private static void checkMilestone(long targetTick, long oldTime, long newTime, WorldServer world) {
        if (oldTime < targetTick && newTime >= targetTick) {
            
            if (targetTick == 1) {
                System.out.println("[Crescentfallen] TRIGGER: EVENT_STARTED");
            } 
            else if (targetTick == 600) {
                System.out.println("[Crescentfallen] TRIGGER: GRACE_PERIOD_END");
            } 
            else if (targetTick == 24000) {
                System.out.println("[Crescentfallen] TRIGGER: DAY_2_START");
            } 
            else if (targetTick == 48000) {
                System.out.println("[Crescentfallen] TRIGGER: DAY_3_START");
            } 
            else if (targetTick == 72000) {
                System.out.println("[Crescentfallen] TRIGGER: EVENT_FINALE");
            }
        }
    }
}