package fracture.mod.util.handlers;

import fracture.mod.CFInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HudOverlayHandler {

    private static final ResourceLocation dashIcon = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/dash_icon.png");
    private static final ResourceLocation slideIcon = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/slide_icon.png");
    

    private static final int DEFAULT_COOLDOWN_TICKS = 20; 
    private static final int DASH_DISPLAY_TICKS = 40; 
    
    //Note: this is not done, needs debugging

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null || player.isCreative()) return;

        int cooldown = player.getEntityData().getInteger("diveCooldown");
        boolean canDive = player.getEntityData().hasKey("canDive") ? player.getEntityData().getBoolean("canDive") : true;

        int slideTicks = player.getEntityData().getInteger("slideTicks"); 
        boolean sliding = player.getEntityData().getString("diveState").equals("SLIDING");

        if (sliding) {
            slideTicks = DASH_DISPLAY_TICKS; 
        } else if (slideTicks > 0) {
            slideTicks--;
        }
        player.getEntityData().setInteger("slideTicks", slideTicks);

        ScaledResolution res = new ScaledResolution(mc);
        int screenWidth = res.getScaledWidth();
        int screenHeight = res.getScaledHeight();

        // VELOCITY HUD
        double speedX = player.motionX;
        double speedZ = player.motionZ;
        double velocity = Math.sqrt(speedX * speedX + speedZ * speedZ) * 20; 

        if (velocity > 0.5) {
            String speedText = String.format("%.1f m/s", velocity);
            int textWidth = mc.fontRenderer.getStringWidth(speedText);
            mc.fontRenderer.drawStringWithShadow(speedText, (screenWidth - textWidth) / 2, screenHeight - 60, 0xFFFFFF);
        }
        
        int dashX = (screenWidth - 16) / 2;
        int dashY = screenHeight - 42;

        if (cooldown > 0) {
            //DRAW ICON
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 0.5f); // Fade out slightly
            
            mc.getTextureManager().bindTexture(dashIcon);
            Gui.drawModalRectWithCustomSizedTexture(dashX, dashY, 0, 0, 16, 16, 16, 16);
            
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f); // Reset color

            // BAR 
            int barWidth = 16;
            int barHeight = 2;
            float fraction = 1.0f - ((float) cooldown / (float) DEFAULT_COOLDOWN_TICKS);
            int filled = (int) (barWidth * fraction);
            GuiIngame.drawRect(dashX, dashY + 16, dashX + filled, dashY + 16 + barHeight, 0xFF00FFFF);
        } 
        else if (canDive) {
            // READY ICON
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            
            mc.getTextureManager().bindTexture(dashIcon);
            Gui.drawModalRectWithCustomSizedTexture(dashX, dashY, 0, 0, 16, 16, 16, 16);
            
            // Text Indicator
            //mc.fontRenderer.drawStringWithShadow("DR", dashX + 20, dashY + 3, 0xF74510);
        }

        // SLIDE ICON
        // NCI
        if (slideTicks > 0) {
            int slideX = screenWidth / 2 - 20; 
            int slideY = screenHeight - 36;
            
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

            mc.getTextureManager().bindTexture(slideIcon);
            Gui.drawModalRectWithCustomSizedTexture(slideX, slideY, 0, 0, 16, 16, 16, 16);

            int barWidth = 16;
            int barHeight = 2;
            float fraction = (float) slideTicks / DASH_DISPLAY_TICKS;
            int filled = (int) (barWidth * fraction);
            GuiIngame.drawRect(slideX, slideY + 16, slideX + filled, slideY + 16 + barHeight, 0xAAFFAA00);
        }
        
        GlStateManager.disableBlend();
    }
}