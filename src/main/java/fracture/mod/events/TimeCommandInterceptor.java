package fracture.mod.events;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;

@Mod.EventBusSubscriber
public class TimeCommandInterceptor {
    
    private static long lastWorldTime = -1;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || event.phase != TickEvent.Phase.END || event.world.provider.getDimension() != 0) {
            return;
        }

        World world = event.world;
        long currentWorldTime = world.getWorldTime();

        if (lastWorldTime != -1) {
            long expectedTime = lastWorldTime + 1;
            
            if (currentWorldTime > expectedTime) {
                long jumpAmount = currentWorldTime - expectedTime;
                
                world.getWorldInfo().setWorldTotalTime(world.getTotalWorldTime() + jumpAmount);
                System.out.println("[Crescentfallen] Time jump detected. Fast-forwarding event by " + jumpAmount + " ticks.");
            } 
            else if (currentWorldTime < lastWorldTime) {
                long jumpAmount = lastWorldTime - currentWorldTime;
                long newTotal = world.getTotalWorldTime() - jumpAmount;
                
                if (newTotal < 0) newTotal = 0; 
                world.getWorldInfo().setWorldTotalTime(newTotal);
                System.out.println("[Crescentfallen] Time reverse detected. Rewinding event by " + jumpAmount + " ticks.");
            }
        }
        
        lastWorldTime = currentWorldTime;
    }
}