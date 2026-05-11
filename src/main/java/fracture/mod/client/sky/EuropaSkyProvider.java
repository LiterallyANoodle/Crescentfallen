package fracture.mod.client.sky;

import java.util.Random;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.Constants;
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
import net.minecraftforge.client.IRenderHandler;

public class EuropaSkyProvider extends IRenderHandler {

    private static final ResourceLocation jupiterTexture = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/jupiter.png");
    private static final ResourceLocation sunTexture = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/planets/orbitalsun.png");

    public int starList;        
    public int starListTwinkle; 
    public int glSkyList;
    public int glSkyList2;
    private float sunSize;

    public EuropaSkyProvider(IGalacticraftWorldProvider europaProvider) {
        this.sunSize = 17.5F * europaProvider.getSolarSize();
        
        int displayLists = GLAllocation.generateDisplayLists(4);
        this.starList = displayLists;
        this.starListTwinkle = displayLists + 1;
        this.glSkyList = displayLists + 2;
        this.glSkyList2 = displayLists + 3;

        GL11.glPushMatrix();
        GL11.glNewList(this.starList, GL11.GL_COMPILE);
        this.renderStars(true); 
        GL11.glEndList();
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glNewList(this.starListTwinkle, GL11.GL_COMPILE);
        this.renderStars(false); // false = generate twinkling stars
        GL11.glEndList();
        GL11.glPopMatrix();

        // Skybox
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
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        
        float starBrightnessForSky = world.getStarBrightness(partialTicks);
        float skyBrightness = 1.0F - starBrightnessForSky;

        // Background sky colors
        float horR = 0.25F * skyBrightness;
        float horG = 0.00F * skyBrightness;
        float horB = 0.50F * skyBrightness;

        GL11.glColor3f(horR, horG, horB);

        Tessellator tessellator1 = Tessellator.getInstance();
        BufferBuilder worldRenderer1 = tessellator1.getBuffer();
        
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_FOG); 
        
        GL11.glCallList(this.glSkyList);
        
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();

        float f6;
        float f7;
        float f8;
        float f9;
        float f10;

        float time = world.getTotalWorldTime() + partialTicks;
        float starBrightness = world.getStarBrightness(partialTicks);

        // Render Stars
        if (starBrightness > 0.0F) {
            GL11.glPushMatrix();
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-19.0F, 0, 1.0F, 0);

            GL11.glColor4f(starBrightness, starBrightness, starBrightness, starBrightness);
            GL11.glCallList(this.starList);

            float flicker = (MathHelper.sin(time * 0.15F) + MathHelper.cos(time * 0.65F)) * 0.5F; 
            float twinkleAlpha = starBrightness * (0.6F + 0.4F * flicker); 

            GL11.glColor4f(starBrightness, starBrightness, starBrightness, twinkleAlpha);
            GL11.glCallList(this.starListTwinkle);

            GL11.glPopMatrix();
        }

        // Render Sun
        float[] afloat = new float[4];
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glPushMatrix();
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
        
        afloat[0] = 255 / 255.0F;
        afloat[1] = 194 / 255.0F;
        afloat[2] = 180 / 255.0F;
        afloat[3] = 0.3F;
        
        f6 = afloat[0];
        f7 = afloat[1];
        f8 = afloat[2];
        float f11;

        if (mc.gameSettings.anaglyph) {
            f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
            f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
            f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
            f6 = f9;
            f7 = f10;
            f8 = f11;
        }

        float f18 = 1.0F - starBrightness; 

        // Sun Aura logic(Inner)
        worldRenderer1.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        float r = f6 * f18;
        float g = f7 * f18;
        float b = f8 * f18;
        float a = afloat[3] * 2 / f18; 
        if(a > 1.0f) a = 1.0f;
        worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, a).endVertex();
        r = afloat[0] * f18;
        g = afloat[1] * f18;
        b = afloat[2] * f18;
        a = 0.0F;
        f10 = 20.0F; 
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

        // Sun Aura logic(Outer)
        worldRenderer1.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        r = f6 * f18;
        g = f7 * f18;
        b = f8 * f18;
        a = afloat[3] * f18;
        worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, a).endVertex();
        r = afloat[0] * f18;
        g = afloat[1] * f18;
        b = afloat[2] * f18;
        a = 0.0F;
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
        
        GL11.glPopMatrix();
        GL11.glShadeModel(GL11.GL_FLAT);

        // Sun Texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glPushMatrix();
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
        f10 = this.sunSize / 3.5F; 
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer1.pos(-f10, 99.9D, -f10).endVertex();
        worldRenderer1.pos(f10, 99.9D, -f10).endVertex();
        worldRenderer1.pos(f10, 99.9D, f10).endVertex();
        worldRenderer1.pos(-f10, 99.9D, f10).endVertex();
        tessellator1.draw();
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        f10 = this.sunSize;
        mc.renderEngine.bindTexture(sunTexture);
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer1.pos(-f10, 100.0D, -f10).tex(0.0D, 0.0D).endVertex();
        worldRenderer1.pos(f10, 100.0D, -f10).tex(1.0D, 0.0D).endVertex();
        worldRenderer1.pos(f10, 100.0D, f10).tex(1.0D, 1.0D).endVertex();
        worldRenderer1.pos(-f10, 100.0D, f10).tex(0.0D, 1.0D).endVertex();
        tessellator1.draw();
        GL11.glPopMatrix(); 

        // Jupiter
        GL11.glPushMatrix();
        float jupiterSize = sunSize * 3.5F; 
        GL11.glScalef(0.6F, 0.6F, 0.6F);
        GL11.glRotatef(40.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(200F, 1.0F, 0.0F, 0.0F); 
        mc.renderEngine.bindTexture(jupiterTexture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F); 
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer1.pos(-jupiterSize, -100.0D, jupiterSize).tex(0, 1).endVertex();
        worldRenderer1.pos(jupiterSize, -100.0D, jupiterSize).tex(1, 1).endVertex();
        worldRenderer1.pos(jupiterSize, -100.0D, -jupiterSize).tex(1, 0).endVertex();
        worldRenderer1.pos(-jupiterSize, -100.0D, -jupiterSize).tex(0, 0).endVertex();
        tessellator1.draw();
        GL11.glPopMatrix();

        // Aurora
        this.renderAurora(tessellator1, time);
       
        // Procedural Gas Cloud test
        this.renderGasCloud(tessellator1, time);

        // Cleanup
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(0.0F, 0.0F, 0.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_ALPHA_TEST); 
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    private void renderAurora(Tessellator tessellator, float time) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.rotate(45.0F, 60.0F, 0.0F, 1.0F); 
        GlStateManager.translate(0.0F, 50.0F, 0.0F);
        
        BufferBuilder worldRenderer = tessellator.getBuffer();
        worldRenderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        
        float width = 30.0F; 
        float height = 110.0F; 
        float length = 300.0F; 
        int segments = 400; 
        
        for (int i = 0; i <= segments; i++) {
            float t = (float)i / (float)segments; 
            float posOffset = (t - 0.5F) * length; 

            float wave1 = MathHelper.sin(t * 10.0F + time * 0.02F) * 5.0F;
            float wave2 = MathHelper.cos(t * 15.0F - time * 0.02F) * 5.0F;
            float hue = (t * 5.0F + time * 0.01F) % 1.0F;
            
            float r = MathHelper.clamp(0.2F + 0.6F * MathHelper.sin(hue * 6.28F), 0, 1);
            float g = MathHelper.clamp(0.2F + 0.6F * MathHelper.sin(hue * 6.28F + 2.0F), 0, 1);
            float b = MathHelper.clamp(0.6F + 0.4F * MathHelper.sin(hue * 6.28F + 4.0F), 0, 1);

            float alphaBase = 1.0F - Math.abs((t - 0.5F) * 2.0F);
            float alpha = Math.max(0, alphaBase * 0.6F + 0.1F * MathHelper.sin(time * 0.1F));

            worldRenderer.pos(posOffset, height + wave1, -width + wave2).color(r, g, b, 0.0F).endVertex(); 
            worldRenderer.pos(posOffset, height + wave1, 0 + wave2).color(r, g, b, alpha).endVertex(); 
            worldRenderer.pos(posOffset, height + wave1, width + wave2).color(r, g, b, 0.0F).endVertex(); 
        }
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void renderGasCloud(Tessellator tessellator, float time) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.rotate(130.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(30.0F, 1.0F, 0.0F, 0.0F); 
        
        BufferBuilder buffer = tessellator.getBuffer();
        
        for (int layer = 0; layer < 3; layer++) {
            GlStateManager.pushMatrix();
            float layerRotation = time * (0.05F + (layer * 0.02F));
            GlStateManager.rotate(layerRotation, 0.0F, 0.0F, 1.0F);
            
            float scale = 1.0F + (layer * 0.2F);
            GlStateManager.scale(scale, scale, scale);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            int gridRes = 10; 
            float cloudSize = 40.0F;
            float step = (cloudSize * 2) / gridRes;

            for (int x = 0; x < gridRes; x++) {
                for (int y = 0; y < gridRes; y++) {
                    drawWorblingVertex(buffer, x, y, step, cloudSize, time, layer);
                    drawWorblingVertex(buffer, x + 1, y, step, cloudSize, time, layer);
                    drawWorblingVertex(buffer, x + 1, y + 1, step, cloudSize, time, layer);
                    drawWorblingVertex(buffer, x, y + 1, step, cloudSize, time, layer);
                }
            }
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void drawWorblingVertex(BufferBuilder buffer, int xi, int yi, float step, float size, float time, int layer) {
        float xPos = -size + (xi * step);
        float yPos = -size + (yi * step);

        float dist = MathHelper.sqrt(xPos * xPos + yPos * yPos);
        float maxDist = size * 0.9F;
        
        float worbleX = MathHelper.sin(time * 0.05F + (xi * 0.5F)) * 1.2F;
        float worbleY = MathHelper.cos(time * 0.03F + (yi * 0.5F)) * 1.2F;
        float worbleZ = MathHelper.sin(time * 0.02F + (xi + yi)) * 2.5F;

        float greebles = MathHelper.sin(xi * 1.5F + layer) * MathHelper.cos(yi * 1.5F - layer);
        float alpha = (1.0F - (dist / maxDist)) * (0.3F + (0.2F * greebles));
        
        if (alpha < 0) alpha = 0;
        if (dist > maxDist) alpha = 0;

        float r = 0.5F + (layer * 0.1F);
        float g = 0.1F;
        float b = 0.8F + (greebles * 0.2F);

        buffer.pos(xPos + worbleX, yPos + worbleY, -100.0D + worbleZ).color(r, g, b, alpha).endVertex();
    }

    private void renderStars(boolean generateStatic) {
        final Random rand = new Random(10842L);
        final Tessellator var2 = Tessellator.getInstance();
        BufferBuilder worldRenderer = var2.getBuffer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        for (int starIndex = 0; starIndex < 12000; ++starIndex) {
            double var4 = rand.nextFloat() * 2.0F - 1.0F;
            double var6 = rand.nextFloat() * 2.0F - 1.0F;
            double var8 = rand.nextFloat() * 2.0F - 1.0F;
            
            double baseSize = 0.06F + rand.nextFloat() * 0.08F;
            double var12 = var4 * var4 + var6 * var6 + var8 * var8;

            if (var12 < 1.0D && var12 > 0.01D) {
                boolean isTwinkler = rand.nextFloat() < 0.28F;
                if (generateStatic && isTwinkler) continue;
                if (!generateStatic && !isTwinkler) continue;

                var12 = 1.0D / Math.sqrt(var12);
                var4 *= var12;
                var6 *= var12;
                var8 *= var12;
                
                final double var14 = var4 * 100.0D;
                final double var16 = var6 * 100.0D;
                final double var18 = var8 * 100.0D;
                final double var20 = Math.atan2(var4, var8);
                final double var22 = Math.sin(var20);
                final double var24 = Math.cos(var20);
                final double var26 = Math.atan2(Math.sqrt(var4 * var4 + var8 * var8), var6);
                final double var28 = Math.sin(var26);
                final double var30 = Math.cos(var26);
                final double var32 = rand.nextDouble() * Math.PI * 2.0D;
                final double var34 = Math.sin(var32);
                final double var36 = Math.cos(var32);

                float typeRoll = rand.nextFloat();
                if (typeRoll < 0.02F) { 
                    double flareSize = baseSize * 2.5D; 
                    drawCustomQuad(worldRenderer, var14, var16, var18, flareSize, flareSize, var22, var24, var28, var30, var34, var36);
                    drawCustomQuad(worldRenderer, var14, var16, var18, flareSize * 0.15D, flareSize * 4.0D, var22, var24, var28, var30, var34, var36);
                    drawCustomQuad(worldRenderer, var14, var16, var18, flareSize * 4.0D, flareSize * 0.15D, var22, var24, var28, var30, var34, var36);
                } else if (typeRoll < 0.15F) {
                    double largeSize = baseSize * 1.8D;
                    drawCustomQuad(worldRenderer, var14, var16, var18, largeSize, largeSize, var22, var24, var28, var30, var34, var36);
                } else {
                    drawCustomQuad(worldRenderer, var14, var16, var18, baseSize, baseSize, var22, var24, var28, var30, var34, var36);
                }
            }
        }
        var2.draw();
    }

    private void drawCustomQuad(BufferBuilder buffer, double x, double y, double z, double width, double height, 
                               double s1, double c1, double s2, double c2, double s3, double c3) {
        for (int i = 0; i < 4; ++i) {
            double localX = 0.0D;
            double localY = ((i & 2) - 1) * width;
            double localZ = ((i + 1 & 2) - 1) * height;
            
            double rotX = localY * c3 - localZ * s3;
            double rotZ = localZ * c3 + localY * s3;
            double rotY2 = rotX * s2 + localX * c2;
            double rotX2 = localX * s2 - rotX * c2;
            double finalX = rotX2 * s1 - rotZ * c1;
            double finalZ = rotZ * s1 + rotX2 * c1;
            
            buffer.pos(x + finalX, y + rotY2, z + finalZ).endVertex();
        }
    }
}