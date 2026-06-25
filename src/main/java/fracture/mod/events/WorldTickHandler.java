package fracture.mod.events;

import fracture.mod.world.data.EventStateManager;
//import fracture.mod.network.MessageSyncEventState;
import fracture.mod.network.NetworkHandler; 
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WorldTickHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || event.phase != TickEvent.Phase.END || event.world.provider.getDimension() != 0) {
            return;
        }

        EventStateManager manager = EventStateManager.get(event.world);
        
        if (manager.getEventTicks() < EventStateManager.MAX_TICKS) {
            manager.addTick();
        }

        int ticks = manager.getEventTicks();
        int state = manager.getCurrentState();

        if (ticks >= 24000 && state < 1) {
            manager.setCurrentState(1);
            System.out.println("[Crescentfallen] Event Stage 1");
        } 
        else if (ticks >= 48000 && state < 2) {
            manager.setCurrentState(2);
            System.out.println("[Crescentfallen] Event Stage 2");
        }
        else if (ticks >= 72000 && state < 3) {
            manager.setCurrentState(3);
            System.out.println("[Crescentfallen] Event Stage 3");
        }

//        if (ticks % 100 == 0) {
//            fracture.mod.CFMain.NETWORK.sendToDimension(new fracture.mod.network.PacketSyncTimer(ticks), 0);
//        }
    }
}