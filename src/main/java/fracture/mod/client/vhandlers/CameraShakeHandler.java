package fracture.mod.client.vhandlers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CameraShakeHandler {

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        float intensity = ScreenShakeManager.getIntensity();

        if (intensity > 0.0f) {
            Entity entity = event.getEntity();
            
            float time = entity.ticksExisted + (float) event.getRenderPartialTicks();

            float yawOffset = (float) (Math.sin(time * 1.2f) * Math.cos(time * 0.5f)) * intensity * 2.0f;
            float pitchOffset = (float) (Math.cos(time * 1.5f) * Math.sin(time * 0.8f)) * intensity * 2.0f;
            float rollOffset = (float) (Math.sin(time * 1.1f) * Math.cos(time * 0.6f)) * intensity * 1.5f;

            event.setYaw(event.getYaw() + yawOffset);
            event.setPitch(event.getPitch() + pitchOffset);
            event.setRoll(event.getRoll() + rollOffset);
        }
    }
}