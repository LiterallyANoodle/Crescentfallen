package fracture.mod.client.gui;

import fracture.mod.client.event.ClientTimerSyncHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class DestructionTimerUI {

    private static final long EVENT_START_TICK = 600; 
    private static final long EVENT_DURATION = 72000; 
    private static final long EVENT_END_TICK = EVENT_START_TICK + EVENT_DURATION;

    private static final float SCALE_NORMAL = 2.0f; 
    private static final float SCALE_SMALL = 1.5f;
    private static final float SCALE_LARGE = 3.0f; 
    
    private static final int COLOR_RED_1 = 0xFFFF0000;
    private static final int COLOR_RED_2 = 0xFFAA0000; 
    private static final int COLOR_YELLOW = 0xFFFFD700; 
    private static final int COLOR_LIGHT_YELLOW = 0xFFFFFF99; 

    private static float smoothX = -1;
    private static float smoothY = -1;
    private static float smoothScale = -1;
    private static float smoothMasterAlpha = 0.0f;
    private static float smoothPrefixAlpha = 0.0f;
    private static int lastSoundPhase = -1;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || mc.currentScreen != null) return;
        if (mc.world.provider.getDimension() != 0) return;
        if (mc.gameSettings.hideGUI) return; 

        long currentTime = ClientTimerSyncHandler.getDisplayTime();
        
        float exactTime = mc.world.getWorldTime() + event.renderTickTime; 

        float targetMasterAlpha = (currentTime >= EVENT_START_TICK && currentTime < EVENT_END_TICK) ? 1.0f : 0.0f;
        if (targetMasterAlpha == 0.0f && smoothMasterAlpha < 0.01f) {
            smoothX = -1; 
            lastSoundPhase = -1;
            return;
        }

        long ticksActive = currentTime - EVENT_START_TICK;
        long ticksRemaining = EVENT_END_TICK - currentTime;

        int currentPhase = 0;
        if (ticksActive >= 0 && ticksActive < 100) currentPhase = 1;         
        else if (ticksActive >= 100 && ticksActive < 24000) currentPhase = 2; 
        else if (ticksActive >= 24000 && ticksActive < 24200) currentPhase = 3; 
        else if (ticksActive >= 24200 && ticksActive < 48000) currentPhase = 4; 
        else if (ticksActive >= 48000 && ticksActive < 48400) currentPhase = 5; 
        else if (ticksActive >= 48400 && ticksActive < 70800) currentPhase = 6; 
        else if (ticksActive >= 70800 && ticksActive < 72000) currentPhase = 7; 

        if (currentPhase != lastSoundPhase && currentPhase > 0) {
            if (currentPhase == 1) playSound(mc, SoundEvents.BLOCK_NOTE_PLING, 1.0f);
            else if (currentPhase == 3) playSound(mc, SoundEvents.BLOCK_NOTE_PLING, 0.8f);
            else if (currentPhase == 5) playSound(mc, SoundEvents.BLOCK_NOTE_PLING, 0.3f);
            else if (currentPhase == 7) playSound(mc, SoundEvents.BLOCK_NOTE_BASEDRUM, 0.5f);
            lastSoundPhase = currentPhase;
        }

        int totalSeconds = (int) Math.ceil(ticksRemaining / 20.0f);
        totalSeconds = Math.max(0, totalSeconds); 
        String timeText = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);

        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer font = mc.fontRenderer;
        float centerX = sr.getScaledWidth() / 2.0f;
        
        float targetX = centerX;
        float targetY = sr.getScaledHeight() / 3.0f;
        float targetScale = SCALE_NORMAL;
        float targetPrefixAlpha = 0.0f;
        int targetColor = COLOR_YELLOW;
        String prefixText = "TIME UNTIL WORLD DESTRUCTION: ";

        if (currentPhase == 1) { 
            targetPrefixAlpha = 1.0f;
            targetColor = getFlashingColor(exactTime, COLOR_RED_1, COLOR_RED_2, 10.0f);
        }
        else if (currentPhase == 2 || currentPhase == 4) { 
            targetScale = SCALE_SMALL;
            targetY = 20.0f; 
            if (ticksActive > 100 && ticksActive % 6000 < 40) {
                targetColor = getFlashingColor(exactTime, COLOR_YELLOW, COLOR_LIGHT_YELLOW, 2.0f);
            }
        }
        else if (currentPhase == 3) { 
            targetY = 40.0f; 
            targetPrefixAlpha = 1.0f;
            targetColor = COLOR_RED_1;
        }
        else if (currentPhase == 5) { 
            targetY = 60.0f;
            targetScale = 1.2f; 
            targetPrefixAlpha = 1.0f;
            prefixText = "WARNING! PLANET INTEGRITY COLLAPSING! ";
            targetColor = getFlashingColor(exactTime, COLOR_RED_1, COLOR_RED_2, 5.0f);
        }
        else if (currentPhase == 6) { 
            targetY = 30.0f;
            targetColor = getFlashingColor(exactTime, COLOR_RED_1, COLOR_RED_2, 8.0f);
        }
        else if (currentPhase == 7) { 
            targetScale = SCALE_LARGE;
            targetColor = getFlashingColor(exactTime, COLOR_RED_1, COLOR_RED_2, 3.0f);
        }

        float scaledPrefixW = font.getStringWidth(prefixText) * targetScale;
        float scaledTimeW = font.getStringWidth(timeText) * targetScale;

        if (currentPhase == 1 || currentPhase == 3 || currentPhase == 5) {
            targetX = centerX + (scaledPrefixW - scaledTimeW) / 2.0f;
        } else if (currentPhase == 2 || currentPhase == 4) {
            targetX = sr.getScaledWidth() - scaledTimeW - 10.0f;
        } else {
            targetX = centerX - (scaledTimeW / 2.0f);
        }

        if (smoothScale < 0) { 
            smoothScale = targetScale;
            smoothX = targetX;
            smoothY = targetY;
        }

        float delta = 0.1f;
        smoothX += (targetX - smoothX) * delta;
        smoothY += (targetY - smoothY) * delta;
        smoothScale += (targetScale - smoothScale) * delta;
        smoothMasterAlpha += (targetMasterAlpha - smoothMasterAlpha) * delta;
        smoothPrefixAlpha += (targetPrefixAlpha - smoothPrefixAlpha) * delta;

        GlStateManager.pushMatrix();
        GlStateManager.translate(smoothX, smoothY, 0.0F);
        GlStateManager.scale(smoothScale, smoothScale, smoothScale);

        int ma = (int) (smoothMasterAlpha * 255);
        int timeRenderColor = (ma << 24) | (targetColor & 0x00FFFFFF);

        font.drawStringWithShadow(timeText, 0, -font.FONT_HEIGHT / 2.0f, timeRenderColor);

        if (smoothPrefixAlpha > 0.01f) {
            int pa = (int) (smoothPrefixAlpha * smoothMasterAlpha * 255);
            int prefixRenderColor = (pa << 24) | (targetColor & 0x00FFFFFF);
            font.drawStringWithShadow(prefixText, -font.getStringWidth(prefixText), -font.FONT_HEIGHT / 2.0f, prefixRenderColor);
        }

        GlStateManager.popMatrix();
    }

    private static void playSound(Minecraft mc, SoundEvent sound, float pitch) {
        if (mc.player != null) mc.player.playSound(sound, 1.0f, pitch);
    }

    private static int getFlashingColor(float time, int color1, int color2, float speed) {
        float blend = (float) (Math.sin(time / speed) + 1.0) / 2.0f;
        return interpolateColor(color1, color2, blend);
    }

    private static int interpolateColor(int color1, int color2, float factor) {
        int a1 = (color1 >> 24) & 0xFF; int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;  int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF; int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;  int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * factor); int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor); int b = (int) (b1 + (b2 - b1) * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}