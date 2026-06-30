package fracture.mod.util.handlers;

import fracture.mod.CFMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeybindHandler {
    public static KeyBinding diveKey;
    public static KeyBinding crouchKey; 

    public static void register() {
        diveKey = new KeyBinding("key.dive", Keyboard.KEY_LCONTROL, "key.categories.movement");
        ClientRegistry.registerKeyBinding(diveKey);
        
        crouchKey = new KeyBinding("key.crouch_low", Keyboard.KEY_X, "key.categories.movement");
        ClientRegistry.registerKeyBinding(crouchKey);

        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;
        EntityPlayerSP player = mc.player;

        if (diveKey.isPressed()) {
            float forward = player.movementInput.moveForward;
            float strafe = player.movementInput.moveStrafe;
            
            boolean success = DiveHandler.performClientDive(player, forward, strafe);
            if (success) {
                CFMain.NETWORK.sendToServer(new DivePacket(forward, strafe));
            }
        }
        
        // Note: Continuous holding/releasing of the crouch key is evaluated 
        // dynamically inside the living tick loop to maintain smoothness.
    }
}