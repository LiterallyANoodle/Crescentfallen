package fracture.mod.util.handlers;

import fracture.mod.CFInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = CFInfo.ID)
public class DiveClientHandler {

    private static float currentCameraTranslation = 0.0F;

    @SubscribeEvent
    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        DiveHandler.DiveState state = DiveHandler.ClientPhysics.state;

        // Dynamically compute visual translation target based on exact profile state heights
        float targetTranslation = 0.0F;
        
        if (state == DiveHandler.DiveState.DIVING || state == DiveHandler.DiveState.SLIDING) {
            // Target delta: 1.62F (Standing) - 0.42F (Crawling eye height) = 1.2F
            targetTranslation = 1.2F;
        } else if (state == DiveHandler.DiveState.CROUCHING) {
            // Target delta: 1.62F (Standing) - 0.81F (Crouch eye height) = 0.81F
            targetTranslation = 0.81F;
        }

        // Smooth linear interpolation (lerp) for camera transitions
        currentCameraTranslation += (targetTranslation - currentCameraTranslation) * 0.2F;

        if (currentCameraTranslation > 0.001F) {
            // Negative vector simulation pushes camera down into low profile mechanics
            GlStateManager.translate(0.0F, currentCameraTranslation, 0.0F);
        }
    }
}