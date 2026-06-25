package fracture.mod.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT)
public class BombardmentRenderer {

    private static final List<ActiveLaser> activeLasers = new ArrayList<>();

    public static void addLaser(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        activeLasers.add(new ActiveLaser(startX, startY, startZ, endX, endY, endZ, 20)); // 20 ticks = 1 second
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.isGamePaused()) return;

        Iterator<ActiveLaser> iterator = activeLasers.iterator();
        while (iterator.hasNext()) {
            ActiveLaser laser = iterator.next();
            laser.ticksExisted++;
            if (laser.ticksExisted > laser.maxLifespan) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (activeLasers.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        Entity viewEntity = mc.getRenderViewEntity();
        if (viewEntity == null) return;

        float partialTicks = event.getPartialTicks();

        double interpX = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double interpY = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double interpZ = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-interpX, -interpY, -interpZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false); 

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (ActiveLaser laser : activeLasers) {
            float lifePercentage = (float) laser.ticksExisted / laser.maxLifespan;
            float alpha = 1.0F - lifePercentage;

            GL11.glLineWidth(12.0F);
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(laser.startX, laser.startY, laser.startZ).color(1.0F, 0.8F, 0.0F, alpha * 0.5F).endVertex();
            buffer.pos(laser.endX, laser.endY, laser.endZ).color(1.0F, 0.5F, 0.0F, alpha * 0.5F).endVertex();
            tessellator.draw();

            GL11.glLineWidth(4.0F);
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(laser.startX, laser.startY, laser.startZ).color(1.0F, 1.0F, 0.6F, alpha).endVertex();
            buffer.pos(laser.endX, laser.endY, laser.endZ).color(1.0F, 1.0F, 0.6F, alpha).endVertex();
            tessellator.draw();
        }

        GL11.glLineWidth(1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static class ActiveLaser {
        double startX, startY, startZ;
        double endX, endY, endZ;
        int ticksExisted = 0;
        int maxLifespan;

        public ActiveLaser(double sx, double sy, double sz, double ex, double ey, double ez, int maxLifespan) {
            this.startX = sx;
            this.startY = sy;
            this.startZ = sz;
            this.endX = ex;
            this.endY = ey;
            this.endZ = ez;
            this.maxLifespan = maxLifespan;
        }
    }
}