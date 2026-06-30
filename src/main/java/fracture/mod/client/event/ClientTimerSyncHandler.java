package fracture.mod.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientTimerSyncHandler {

    private static float displayTime = 0f;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.world == null || mc.isGamePaused()) return;

        long realTime = mc.world.getWorldTime();

        // DAY 3
        if (realTime >= 48000) {
            displayTime = realTime;
        } 
        // DAYS 1/2
        else {
            float diff = realTime - displayTime;
            
            if (Math.abs(diff) > 100) {
                displayTime = realTime;
            } else {
                displayTime += diff * 0.1f; 
            }
        }
    }

    public static int getDisplayTime() {
        return Math.round(displayTime);
    }
}