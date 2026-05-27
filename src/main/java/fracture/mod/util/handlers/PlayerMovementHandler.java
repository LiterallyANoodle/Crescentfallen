package fracture.mod.util.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

//MOVEMENT SYSTEM TESTING

public class PlayerMovementHandler {
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		
		
	    EntityPlayer player = event.player;

	    
	    int cd = player.getEntityData().getInteger("diveCooldown");
	    if (cd > 0) {
	        player.getEntityData().setInteger("diveCooldown", cd - 1);
	    }
	    
	    
	    if (!player.isSpectator() && !player.isSneaking()) {
	        // Raise base walk speed in all directions to match vanilla sprint
	    	// Tie this to spacesuit later and fix viewbobbing
	        if (player.capabilities.getWalkSpeed() < 0.15F) {
	            player.capabilities.setPlayerWalkSpeed(0.15F);
	        }
	    }
	}
		
	}
