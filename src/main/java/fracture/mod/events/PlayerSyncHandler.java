package fracture.mod.events;

import fracture.mod.CFMain;
//import fracture.mod.network.PacketSyncTimer;
import fracture.mod.world.data.EventStateManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber
public class PlayerSyncHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncPlayer(event.player);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.toDim == 0) {
            syncPlayer(event.player);
        }
    }

    private static void syncPlayer(net.minecraft.entity.player.EntityPlayer player) {
        if (!player.world.isRemote && player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            World overworld = playerMP.getServer().getWorld(0);
            
            int currentTicks = EventStateManager.get(overworld).getEventTicks();
            
            //CFMain.NETWORK.sendTo(new PacketSyncTimer(currentTicks), playerMP);
        }
    }
}