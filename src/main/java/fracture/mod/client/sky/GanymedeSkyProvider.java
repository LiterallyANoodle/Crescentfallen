package fracture.mod.client.sky;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import fracture.mod.CFInfo;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;

public class GanymedeSkyProvider extends IRenderHandler {

    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/orbitalsun.png");
    private static final ResourceLocation JUPITER_TEXTURE = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/jupiter.png");
    private static final ResourceLocation IO_TEXTURE = new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/io.png");
    private static final ResourceLocation EUROPA_TEXTURE = new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/europa.png");
    private static final ResourceLocation CALLISTO_TEXTURE = new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/callisto.png");
    
    // Sky
    private float skyR = 0.0F;
    private float skyG = 0.0F;
    private float skyB = 0.02F; 

    // Horizon
    private float horizonR = 0.4F;
    private float horizonG = 0.0F;
    private float horizonB = 0.6F;

    public int starList;
    public int starList2; 
    public int glSkyList;
    public int glSkyList2;
    private float sunSize;

    public GanymedeSkyProvider() {
        this.sunSize = 17.5F; 

        int displayLists = GLAllocation.generateDisplayLists(4);
        this.starList = displayLists;
        this.starList2 = displayLists + 1;
        this.glSkyList = displayLists + 2;
        this.glSkyList2 = displayLists + 3;

        GL11.glPushMatrix();
        GL11.glNewList(this.starList, GL11.GL_COMPILE);
        this.renderStars(10842L, 6000);
        GL11.glEndList();
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glNewList(this.starList2, GL11.GL_COMPILE);
        this.renderStars(99413L, 5000);
        GL11.glEndList();
        GL11.glPopMatrix();

        final Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        
        GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
        final byte byte2 = 64;
        final int i = 256 / byte2 + 2;
        float f = 16F;

        for (int j = -byte2 * i; j <= byte2 * i; j += byte2) {
            for (int l = -byte2 * i; l <= byte2 * i; l += byte2) {
                worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
                worldRenderer.pos(j, f, l).endVertex();
                worldRenderer.pos(j + byte2, f, l).endVertex();
                worldRenderer.pos(j + byte2, f, l + byte2).endVertex();
                worldRenderer.pos(j, f, l + byte2).endVertex();
                tessellator.draw();
            }
        }
        GL11.glEndList();

        GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
        f = -16F;
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        for (int k = -byte2 * i; k <= byte2 * i; k += byte2) {
            for (int i1 = -byte2 * i; i1 <= byte2 * i; i1 += byte2) {
                worldRenderer.pos(k + byte2, f, i1).endVertex();
                worldRenderer.pos(k, f, i1).endVertex();
                worldRenderer.pos(k, f, i1 + byte2).endVertex();
                worldRenderer.pos(k + byte2, f, i1 + byte2).endVertex();
            }
        }
        tessellator.draw();
        GL11.glEndList();
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        GlStateManager.disableTexture2D();
        GlStateManager.disableRescaleNormal();
        
        Vec3d vec3 = world.getSkyColor(mc.getRenderViewEntity(), partialTicks);
        float f1 = (float) vec3.x;
        float f2 = (float) vec3.y;
        float f3 = (float) vec3.z;

        float starBrightness = world.getStarBrightness(partialTicks);
        float totalTime = (float)(world.getTotalWorldTime()) + partialTicks;
        float celestialAngle = world.getCelestialAngle(partialTicks);
        float sunVisibility = 1.0F - starBrightness;
        
        f1 = f1 * (1.0F - starBrightness) + this.skyR * starBrightness;
        f2 = f2 * (1.0F - starBrightness) + this.skyG * starBrightness;
        f3 = f3 * (1.0F - starBrightness) + this.skyB * starBrightness;

        if (mc.gameSettings.anaglyph) {
            float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
            float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
            float f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
            f1 = f4; f2 = f5; f3 = f6;
        }

        GlStateManager.color(f1, f2, f3);
        Tessellator tessellator1 = Tessellator.getInstance();
        BufferBuilder worldRenderer1 = tessellator1.getBuffer();
        
        GlStateManager.depthMask(false);
        GlStateManager.enableFog(); 
        GlStateManager.color(f1, f2, f3);
        GL11.glCallList(this.glSkyList);

        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0); 
        RenderHelper.disableStandardItemLighting();


        if (starBrightness > 0.0F) {
            float parallax1Y = (totalTime * 0.013F) % 360.0F;
            float parallax1X = (totalTime * 0.003F) % 360.0F;
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-19.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(parallax1Y, 0.0F, 1.0F, 0.0F); 
            GlStateManager.rotate(parallax1X, 1.0F, 0.0F, 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, starBrightness);
            GL11.glCallList(this.starList);
            GlStateManager.popMatrix();

            float parallax2Y = (totalTime * 0.021F) % 360.0F;
            float parallax2Z = (totalTime * 0.005F) % 360.0F;
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-19.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(parallax2Y, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(parallax2Z, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(12.0F, 1.0F, 0.0F, 0.0F); // offset tilt = depth cue
            GlStateManager.color(0.82F, 0.88F, 1.0F, starBrightness * 0.65F);
            GL11.glCallList(this.starList2);
            GlStateManager.popMatrix();
        }

        if (starBrightness > 0.2F) {
            renderStarShine(tessellator1, totalTime, starBrightness);
        }

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);

        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // Sun Aura Pass 1
        float f18 = sunVisibility;
        float f6 = 1.0F;
        float f7 = 0.90F;
        float f8 = 0.65F;
        float[] afloat = new float[]{1.0F, 0.70F, 0.30F, 0.32F};

        worldRenderer1.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        float r = f6 * f18;
        float g = f7 * f18;
        float b = f8 * f18;
        float a = afloat[3] * 2.0F / f18;
        if (a > 1.0F) a = 1.0F;
        
        worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, a).endVertex();
        r = afloat[0] * f18; g = afloat[1] * f18; b = afloat[2] * f18; a = 0.0F;

        float f10 = 20.0F;
        worldRenderer1.pos(-f10, 100.0D, -f10).color(r, g, b, a).endVertex();
        worldRenderer1.pos(0, 100.0D, (double) -f10 * 1.5F).color(r, g, b, a).endVertex();
        worldRenderer1.pos(f10, 100.0D, -f10).color(r, g, b, a).endVertex();
        worldRenderer1.pos((double) f10 * 1.5F, 100.0D, 0).color(r, g, b, a).endVertex();
        worldRenderer1.pos(f10, 100.0D, f10).color(r, g, b, a).endVertex();
        worldRenderer1.pos(0, 100.0D, (double) f10 * 1.5F).color(r, g, b, a).endVertex();
        worldRenderer1.pos(-f10, 100.0D, f10).color(r, g, b, a).endVertex();
        worldRenderer1.pos((double) -f10 * 1.5F, 100.0D, 0).color(r, g, b, a).endVertex();
        worldRenderer1.pos(-f10, 100.0D, -f10).color(r, g, b, a).endVertex();
        tessellator1.draw();

        // Sun Aura Pass 2
        worldRenderer1.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        r = f6 * f18; g = f7 * f18; b = f8 * f18; a = afloat[3] * f18;
        worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, a).endVertex();
        r = afloat[0] * f18; g = afloat[1] * f18; b = afloat[2] * f18; a = 0.0F;

        f10 = 40.0F;
        worldRenderer1.pos(-f10, 100.0D, -f10).color(r, g, b, a).endVertex();
        worldRenderer1.pos(0, 100.0D, (double) -f10 * 1.5F).color(r, g, b, a).endVertex();
        worldRenderer1.pos(f10, 100.0D, -f10).color(r, g, b, a).endVertex();
        worldRenderer1.pos((double) f10 * 1.5F, 100.0D, 0).color(r, g, b, a).endVertex();
        worldRenderer1.pos(f10, 100.0D, f10).color(r, g, b, a).endVertex();
        worldRenderer1.pos(0, 100.0D, (double) f10 * 1.5F).color(r, g, b, a).endVertex();
        worldRenderer1.pos(-f10, 100.0D, f10).color(r, g, b, a).endVertex();
        worldRenderer1.pos((double) -f10 * 1.5F, 100.0D, 0).color(r, g, b, a).endVertex();
        worldRenderer1.pos(-f10, 100.0D, -f10).color(r, g, b, a).endVertex();
        tessellator1.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, sunVisibility);

        mc.renderEngine.bindTexture(SUN_TEXTURE);
        float sSize = this.sunSize;
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer1.pos(-sSize, 100.0D, -sSize).tex(0.0D, 0.0D).endVertex();
        worldRenderer1.pos(sSize, 100.0D, -sSize).tex(1.0D, 0.0D).endVertex();
        worldRenderer1.pos(sSize, 100.0D, sSize).tex(1.0D, 1.0D).endVertex();
        worldRenderer1.pos(-sSize, 100.0D, sSize).tex(0.0D, 1.0D).endVertex();
        tessellator1.draw();
        GlStateManager.popMatrix();

        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);


        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(celestialAngle * 360.0F + 35.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(25.0F, 0.0F, 0.0F, 1.0F); 
        GlStateManager.color(1.0F, 1.0F, 1.0F, sunVisibility);
        
        mc.renderEngine.bindTexture(CALLISTO_TEXTURE);
        float cSize = 15.0F; 
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer1.pos(-cSize, 100.0D, -cSize).tex(0, 0).endVertex();
        worldRenderer1.pos(cSize, 100.0D, -cSize).tex(1, 0).endVertex();
        worldRenderer1.pos(cSize, 100.0D, cSize).tex(1, 1).endVertex();
        worldRenderer1.pos(-cSize, 100.0D, cSize).tex(0, 1).endVertex();
        tessellator1.draw();
        GlStateManager.popMatrix();


        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(celestialAngle * 360.0F + 180.0F, 1.0F, 0.0F, 0.0F); 
        GlStateManager.rotate(-15.0F, 0.0F, 0.0F, 1.0F); // Shared path tilt
        GlStateManager.color(1.0F, 1.0F, 1.0F, starBrightness);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F); 
        mc.renderEngine.bindTexture(EUROPA_TEXTURE);
        float eSize = 7.0F; 
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer1.pos(-eSize, 100.0D, -eSize).tex(0, 0).endVertex();
        worldRenderer1.pos(eSize, 100.0D, -eSize).tex(1, 0).endVertex();
        worldRenderer1.pos(eSize, 100.0D, eSize).tex(1, 1).endVertex();
        worldRenderer1.pos(-eSize, 100.0D, eSize).tex(0, 1).endVertex();
        tessellator1.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-35.0F, 1.0F, 0.0F, 0.0F);
        mc.renderEngine.bindTexture(IO_TEXTURE);
        float iSize = 4.0F; 
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer1.pos(-iSize, 100.0D, -iSize).tex(0, 0).endVertex();
        worldRenderer1.pos(iSize, 100.0D, -iSize).tex(1, 0).endVertex();
        worldRenderer1.pos(iSize, 100.0D, iSize).tex(1, 1).endVertex();
        worldRenderer1.pos(-iSize, 100.0D, iSize).tex(0, 1).endVertex();
        tessellator1.draw();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();


        GlStateManager.pushMatrix();
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F); 
        GlStateManager.rotate(40.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F); 
        
        mc.renderEngine.bindTexture(JUPITER_TEXTURE);
        float jSize = 25.0F; 
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer1.pos(-jSize, 100.0D, -jSize).tex(0, 0).endVertex();
        worldRenderer1.pos(jSize, 100.0D, -jSize).tex(1, 0).endVertex();
        worldRenderer1.pos(jSize, 100.0D, jSize).tex(1, 1).endVertex();
        worldRenderer1.pos(-jSize, 100.0D, jSize).tex(0, 1).endVertex();
        tessellator1.draw();
        GlStateManager.popMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
        GlStateManager.enableTexture2D();
        
        GlStateManager.depthMask(true); 
    }

    private void renderStarShine(Tessellator tessellator, float time, float starBrightness) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.depthMask(false);

        BufferBuilder buffer = tessellator.getBuffer();
        final int SHINE_COUNT = 18;
        final long BASE_SEED = 0xA3F7B2C1L;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < SHINE_COUNT; i++) {
            Random rng = new Random(BASE_SEED + i * 0x9E3779B9L);
            float phi = (float)(rng.nextDouble() * Math.PI * 0.85D);
            float theta = (float)(rng.nextDouble() * Math.PI * 2.0D);
            float bx = (float)(Math.sin(phi) * Math.cos(theta)) * 97.0F;
            float by = (float)(Math.cos(phi)) * 97.0F;
            float bz = (float)(Math.sin(phi) * Math.sin(theta)) * 97.0F;

            if (by < 8.0F) continue;

            float cycleSpeed = 0.006F + (float)(rng.nextDouble() * 0.010D);
            float phase = (float)(rng.nextDouble());
            float progress = (time * cycleSpeed + phase) % 1.0F;
            
            float alpha;
            if (progress < 0.20F) alpha = progress / 0.20F;
            else if (progress < 0.50F) alpha = 1.0F;
            else alpha = 1.0F - (progress - 0.50F) / 0.50F;
            
            alpha *= starBrightness * ((float)(rng.nextDouble() * 0.55D) + 0.45F);
            if (alpha < 0.01F) continue;
            
            float sz = 0.30F + (float)(rng.nextDouble() * 0.65D);
            float arm = sz * 3.2F; 

            buffer.pos(bx - arm, by, bz).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
            buffer.pos(bx - sz, by, bz).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.pos(bx + sz, by, bz).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.pos(bx + arm, by, bz).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();

            buffer.pos(bx, by - arm, bz).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
            buffer.pos(bx, by - sz, bz).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.pos(bx, by + sz, bz).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.pos(bx, by + arm, bz).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
        }

        tessellator.draw();


        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void renderStars(long seed, int count) {
        final Random rand = new Random(seed);
        final Tessellator var2 = Tessellator.getInstance();
        BufferBuilder worldRenderer = var2.getBuffer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        int total = ConfigManagerCore.moreStars ? (count * 5) : count;

        for (int starIndex = 0; starIndex < total; ++starIndex) {
            double var4 = rand.nextFloat() * 2.0F - 1.0F;
            double var6 = rand.nextFloat() * 2.0F - 1.0F;
            double var8 = rand.nextFloat() * 2.0F - 1.0F;
            final double var10 = 0.15F + rand.nextFloat() * 0.1F;
            double var12 = var4 * var4 + var6 * var6 + var8 * var8;
            
            if (var12 < 1.0D && var12 > 0.01D) {
                var12 = 1.0D / Math.sqrt(var12);
                var4 *= var12; var6 *= var12; var8 *= var12;
                final double var14 = var4 * (ConfigManagerCore.moreStars ? rand.nextDouble() * 150D + 130D : 100.0D);
                final double var16 = var6 * (ConfigManagerCore.moreStars ? rand.nextDouble() * 150D + 130D : 100.0D);
                final double var18 = var8 * (ConfigManagerCore.moreStars ? rand.nextDouble() * 150D + 130D : 100.0D);
                final double var20 = Math.atan2(var4, var8);
                final double var22 = Math.sin(var20);
                final double var24 = Math.cos(var20);
                final double var26 = Math.atan2(Math.sqrt(var4 * var4 + var8 * var8), var6);
                final double var28 = Math.sin(var26);
                final double var30 = Math.cos(var26);
                final double var32 = rand.nextDouble() * Math.PI * 2.0D;
                final double var34 = Math.sin(var32);
                final double var36 = Math.cos(var32);

                for (int var38 = 0; var38 < 4; ++var38) {
                    final double var39 = 0.0D;
                    final double var41 = ((var38 & 2) - 1) * var10;
                    final double var43 = ((var38 + 1 & 2) - 1) * var10;
                    final double var47 = var41 * var36 - var43 * var34;
                    final double var49 = var43 * var36 + var41 * var34;
                    final double var53 = var47 * var28 + var39 * var30;
                    final double var55 = var39 * var28 - var47 * var30;
                    final double var57 = var55 * var22 - var49 * var24;
                    final double var61 = var49 * var22 + var55 * var24;
                    worldRenderer.pos(var14 + var57, var16 + var53, var18 + var61).endVertex();
                }
            }
        }
        var2.draw();
    }
}