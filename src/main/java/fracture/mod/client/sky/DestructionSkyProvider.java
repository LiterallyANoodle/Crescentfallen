package fracture.mod.client.sky;

import fracture.mod.CFInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class DestructionSkyProvider extends IRenderHandler {

    private static final ResourceLocation MOON_PHASES_TEXTURES = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation ORBITAL_SUN_TEXTURE = new ResourceLocation("galacticraftcore", "textures/gui/planets/orbitalsun.png");

    private boolean isInitialized = false;
    private int glSkyList = -1;
    private int glSkyList2 = -1;
    private int starGLCallList = -1;

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        
        if (!isInitialized) {
            this.generateSky();
            this.generateSky2();
            this.generateStars();
            this.isInitialized = true;
        }

        float exactTime = world.getTotalWorldTime() + partialTicks;

        Vec3d vanillaColor = world.getSkyColor(mc.getRenderViewEntity(), partialTicks);
        Vec3d customColor = calculateSkyColor(exactTime, vanillaColor);

        float f = (float) customColor.x;
        float f1 = (float) customColor.y;
        float f2 = (float) customColor.z;

        if (mc.gameSettings.anaglyph) {
            float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
            float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
            float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
            f = f3; f1 = f4; f2 = f5;
        }

        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.enableFog();
        GlStateManager.color(f, f1, f2);

        GlStateManager.callList(this.glSkyList);

        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.disableStandardItemLighting();

        float[] glowColors = getAtmosphereGlowColors(exactTime);
        if (glowColors[3] > 0.01f) {
            renderDynamicAtmosphere(Tessellator.getInstance(), glowColors);
        }

        float sunriseAlphaModifier = 1.0f;
        if (exactTime >= 20000 && exactTime < 23000) {
            sunriseAlphaModifier = 1.0f - ((exactTime - 20000) / 3000f);
        } else if (exactTime >= 23000) {
            sunriseAlphaModifier = 0.0f;
        }

        if (sunriseAlphaModifier > 0.01f) {
            float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
            if (afloat != null) {
                GlStateManager.disableTexture2D();
                GlStateManager.shadeModel(7425);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                
                float f6 = afloat[0]; float f7 = afloat[1]; float f8 = afloat[2];
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3] * sunriseAlphaModifier).endVertex();

                for (int j2 = 0; j2 <= 16; ++j2) {
                    float f21 = (float)j2 * ((float)Math.PI * 2F) / 16.0F;
                    float f12 = MathHelper.sin(f21);
                    float f13 = MathHelper.cos(f21);
                    bufferbuilder.pos((double)(f12 * 120.0F), (double)(f13 * 120.0F), (double)(-f13 * 40.0F * afloat[3])).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                }

                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.shadeModel(7424);
            }
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend(); 
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        
        float f16 = 1.0F - world.getRainStrength(partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, f16);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
        
        float f17 = 30.0F;
        
        if (exactTime >= 40000) {
            mc.getTextureManager().bindTexture(ORBITAL_SUN_TEXTURE);
        } else {
            mc.getTextureManager().bindTexture(SUN_TEXTURES);
        }
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(-f17), 100.0D, (double)(-f17)).tex(0.0D, 0.0D).endVertex();
        bufferbuilder.pos((double)f17, 100.0D, (double)(-f17)).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos((double)f17, 100.0D, (double)f17).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos((double)(-f17), 100.0D, (double)f17).tex(0.0D, 1.0D).endVertex();
        tessellator.draw();
        
        f17 = 20.0F;
        mc.getTextureManager().bindTexture(MOON_PHASES_TEXTURES);
        int k1 = world.getMoonPhase();
        int i2 = k1 % 4;
        int k2 = k1 / 4 % 2;
        float f22 = (float)(i2 + 0) / 4.0F;
        float f23 = (float)(k2 + 0) / 2.0F;
        float f24 = (float)(i2 + 1) / 4.0F;
        float f14 = (float)(k2 + 1) / 2.0F;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(-f17), -100.0D, (double)f17).tex((double)f24, (double)f14).endVertex();
        bufferbuilder.pos((double)f17, -100.0D, (double)f17).tex((double)f22, (double)f14).endVertex();
        bufferbuilder.pos((double)f17, -100.0D, (double)(-f17)).tex((double)f22, (double)f23).endVertex();
        bufferbuilder.pos((double)(-f17), -100.0D, (double)(-f17)).tex((double)f24, (double)f23).endVertex();
        tessellator.draw();
        
        GlStateManager.disableTexture2D();
        float f15 = world.getStarBrightness(partialTicks) * f16;
        
        if (exactTime >= 24000 && exactTime < 36000) {
            f15 = 0.0F;
        }
        else if (exactTime >= 36000) {
            float extraStars = MathHelper.clamp((exactTime - 36000) / 12000f, 0f, 1f);
            f15 = Math.max(f15, extraStars * f16);
        }

        if (f15 > 0.0F) {
            GlStateManager.color(f15, f15, f15, f15);
            GlStateManager.callList(this.starGLCallList);
            
            if (exactTime >= 36000) {
                float parallaxAlpha = f15 * 0.8F; 
                
                GlStateManager.pushMatrix();
                GlStateManager.rotate(exactTime * 0.005F, 0.0F, 1.0F, 0.0F); 
                GlStateManager.color(parallaxAlpha, parallaxAlpha, parallaxAlpha, parallaxAlpha);
                GlStateManager.callList(this.starGLCallList);
                GlStateManager.popMatrix();
                
                GlStateManager.pushMatrix();
                GlStateManager.rotate(exactTime * 0.01F, 0.2F, 1.0F, 0.0F); 
                GlStateManager.color(parallaxAlpha * 0.7F, parallaxAlpha * 0.7F, parallaxAlpha * 0.7F, parallaxAlpha);
                GlStateManager.callList(this.starGLCallList);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.rotate(exactTime * 0.015F, -0.2F, 1.0F, 0.0F); 
                GlStateManager.color(parallaxAlpha * 0.5F, parallaxAlpha * 0.5F, parallaxAlpha * 0.5F, parallaxAlpha);
                GlStateManager.callList(this.starGLCallList);
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        if (exactTime > 600 && exactTime < 24000) {
            // add occasional meteors day 1
        }
        if (exactTime >= 24000 && exactTime < 48000) {
            // add sky smoke
        }
        if (exactTime >= 48000) {
            // add ring of fire
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        this.renderSmokeRising(Tessellator.getInstance(), exactTime);

        this.renderSurfaceBombardment(Tessellator.getInstance(), exactTime);

        GlStateManager.disableTexture2D();
        GlStateManager.color(0.0F, 0.0F, 0.0F);
        double d3 = mc.player.getPositionEyes(partialTicks).y - world.getHorizon();

        if (d3 < 0.0D) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 12.0F, 0.0F);
            GlStateManager.callList(this.glSkyList2);
            GlStateManager.popMatrix();
            
            float f19 = -((float)(d3 + 65.0D));
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(-1.0D, (double)f19, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, (double)f19, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, (double)f19, -1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(-1.0D, (double)f19, -1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, (double)f19, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, (double)f19, -1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(-1.0D, (double)f19, -1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(-1.0D, (double)f19, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
        }

        if (world.provider.isSkyColored()) {
            GlStateManager.color(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
        } else {
            GlStateManager.color(f, f1, f2);
        }
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -((float)(d3 - 16.0D)), 0.0F);
        GlStateManager.callList(this.glSkyList2);
        GlStateManager.popMatrix();

        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
    }

    private Vec3d calculateSkyColor(float time, Vec3d vanilla) {
        if (time <= 600) {
            return vanilla;
        }

        double vectorLength = Math.sqrt(vanilla.x * vanilla.x + vanilla.y * vanilla.y + vanilla.z * vanilla.z);
        double brightness = MathHelper.clamp(vectorLength / 1.3, 0.0, 1.0);

        //NOTE: CORRECT TIMES, TIMES ARE WIP
        
        if (time <= 24000) {
            float p = MathHelper.clamp((time - 600) / 18000f, 0f, 1f); 
            Vec3d desatBlue = new Vec3d(0.15, 0.2, 0.3).scale(brightness); 
            return lerpVector(vanilla, desatBlue, p);
        }
        
        else if (time <= 27000) {
            float p = (time - 24000) / 3000f;
            Vec3d desatBlue = new Vec3d(0.15, 0.2, 0.3).scale(brightness);
            Vec3d charOrange = new Vec3d(0.35, 0.15, 0.05).scale(Math.max(brightness, 0.3));
            return lerpVector(desatBlue, charOrange, p);
        }
        
        else if (time <= 32000) {
            return new Vec3d(0.35, 0.15, 0.05).scale(Math.max(brightness, 0.3));
        }

        else if (time <= 36000) {
            float p = (time - 32000) / 4000f;
            Vec3d charOrange = new Vec3d(0.35, 0.15, 0.05).scale(Math.max(brightness, 0.3));
            Vec3d darkGrey = new Vec3d(0.02, 0.02, 0.02).scale(Math.max(brightness, 0.1));
            return lerpVector(charOrange, darkGrey, p);
        }

        else if (time <= 42000) {
            return new Vec3d(0.02, 0.02, 0.02).scale(Math.max(brightness, 0.1));
        }

        else if (time <= 48000) {
            float p = (time - 42000) / 6000f;
            Vec3d darkGrey = new Vec3d(0.02, 0.02, 0.02).scale(Math.max(brightness, 0.1));
            Vec3d darkBlue = new Vec3d(0.05, 0.1, 0.2); 
            return lerpVector(darkGrey, darkBlue, p);
        }
        
        else if (time <= 72000) {
            float p = MathHelper.clamp((time - 48000) / 24000f, 0f, 1f);
            Vec3d darkBlue = new Vec3d(0.05, 0.1, 0.2);
            Vec3d pitchBlack = new Vec3d(0.01, 0.01, 0.02);
            return lerpVector(darkBlue, pitchBlack, p);
        }
        
        return new Vec3d(0.01, 0.01, 0.02);
    }

    private float[] getAtmosphereGlowColors(float time) {
        if (time < 24000) return new float[] {0, 0, 0, 0, 80.0f}; 

        if (time <= 27000) {
            float p = (time - 24000) / 3000f; 
            return new float[] {0.6f, 0.2f, 0.0f, p * 0.9f, 80.0f};
        }
        
        else if (time <= 32000) {
            return new float[] {0.6f, 0.2f, 0.0f, 0.9f, 80.0f}; 
        }

        else if (time <= 36000) {
            float p = (time - 32000) / 4000f;
            float r = lerpFloat(0.6f, 0.10f, p);
            float g = lerpFloat(0.2f, 0.03f, p);
            float b = 0.0f;
            float alpha = lerpFloat(0.9f, 0.2f, p);
            float topY = lerpFloat(80.0f, 40.0f, p);
            return new float[] {r, g, b, alpha, topY}; 
        }
        
        else if (time <= 42000) {
            float p = (time - 36000) / 6000f;
            float r = lerpFloat(0.10f, 0.0f, p);
            float g = lerpFloat(0.03f, 0.0f, p);
            float alpha = lerpFloat(0.2f, 0.0f, p);
            return new float[] {r, g, 0.0f, alpha, 40.0f};
        }
        
        else if (time <= 48000) {
            float p = (time - 42000) / 6000f;
            return new float[] {0.0f, 0.4f, 1.0f, p * 0.8f, 30.0f}; 
        }
        
        else if (time <= 72000) {
            float p = (time - 48000) / 24000f;
            float alpha = lerpFloat(0.8f, 0.1f, p); 
            return new float[] {0.0f, 0.4f, 1.0f, alpha, 30.0f};
        }

        return new float[] {0.0f, 0.4f, 1.0f, 0.1f, 30.0f}; 
    }

    private void renderDynamicAtmosphere(Tessellator tessellator, float[] colors) {
        float r = colors[0];
        float g = colors[1];
        float b = colors[2];
        float alpha = colors[3];
        float topY = colors[4];

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,  GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,        GlStateManager.DestFactor.ZERO);
        GlStateManager.disableCull();

        BufferBuilder buffer = tessellator.getBuffer();
        float radius   = 120.0F;
        int   segments = 40;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            float angle = (float) i * ((float) Math.PI * 2.0F) / segments;
            float vx = MathHelper.sin(angle) * radius;
            float vz = MathHelper.cos(angle) * radius;
            
            float nextAngle = (float) (i + 1) * ((float) Math.PI * 2.0F) / segments;
            float nextVx = MathHelper.sin(nextAngle) * radius;
            float nextVz = MathHelper.cos(nextAngle) * radius;

            float topR = Math.min(1.0f, r + 0.2f);
            float topG = Math.min(1.0f, g + 0.2f);
            float topB = Math.min(1.0f, b + 0.2f);

            buffer.pos(nextVx, topY, nextVz).color(topR, topG, topB, 0.0F).endVertex();
            buffer.pos(vx, topY, vz).color(topR, topG, topB, 0.0F).endVertex();
            buffer.pos(vx, -16.0F, vz).color(r, g, b, alpha).endVertex();
            buffer.pos(nextVx, -16.0F, nextVz).color(r, g, b, alpha).endVertex();

            buffer.pos(nextVx, -16.0F, nextVz).color(r, g, b, alpha).endVertex();
            buffer.pos(vx, -16.0F, vz).color(r, g, b, alpha).endVertex();
            buffer.pos(vx, -200.0F, vz).color(r, g, b, alpha).endVertex();
            buffer.pos(nextVx, -200.0F, nextVz).color(r, g, b, alpha).endVertex();
        }
        tessellator.draw();

        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private Vec3d lerpVector(Vec3d start, Vec3d end, float percent) {
        return new Vec3d(
            start.x + (end.x - start.x) * percent,
            start.y + (end.y - start.y) * percent,
            start.z + (end.z - start.z) * percent
        );
    }

    private float lerpFloat(float start, float end, float percent) {
        return start + (end - start) * percent;
    }

    private void generateSky() {
        this.glSkyList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.glSkyList, 4864);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.renderSkyGeometry(bufferbuilder, 16.0F, false);
        tessellator.draw();
        GlStateManager.glEndList();
    }

    private void generateSky2() {
        this.glSkyList2 = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.glSkyList2, 4864);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.renderSkyGeometry(bufferbuilder, -16.0F, true);
        tessellator.draw();
        GlStateManager.glEndList();
    }

    private void renderSkyGeometry(BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);
        for (int k = -384; k <= 384; k += 64) {
            for (int l = -384; l <= 384; l += 64) {
                float f = (float)k;
                float f1 = (float)(k + 64);
                if (reverseX) {
                    f1 = (float)k;
                    f = (float)(k + 64);
                }
                bufferBuilderIn.pos((double)f, (double)posY, (double)l).endVertex();
                bufferBuilderIn.pos((double)f1, (double)posY, (double)l).endVertex();
                bufferBuilderIn.pos((double)f1, (double)posY, (double)(l + 64)).endVertex();
                bufferBuilderIn.pos((double)f, (double)posY, (double)(l + 64)).endVertex();
            }
        }
    }

    private void generateStars() {
        this.starGLCallList = GLAllocation.generateDisplayLists(1);
        GlStateManager.pushMatrix();
        GlStateManager.glNewList(this.starGLCallList, 4864);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.renderStarsGeometry(bufferbuilder);
        tessellator.draw();
        GlStateManager.glEndList();
        GlStateManager.popMatrix();
    }

    private void renderStarsGeometry(BufferBuilder bufferBuilderIn) {
        Random random = new Random(10842L);
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);
        for (int i = 0; i < 1500; ++i) {
            double d0 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d1 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d2 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d3 = (double)(0.15F + random.nextFloat() * 0.1F);
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D) {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4; d1 = d1 * d4; d2 = d2 * d4;
                double d5 = d0 * 100.0D; double d6 = d1 * 100.0D; double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8); double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11); double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14); double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j) {
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    bufferBuilderIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }
    }

    private void renderSmokeRising(Tessellator tessellator, float exactTime) {
        if (exactTime < 32000f || exactTime > 48000f) {
            return;
        }

        float envelopeAlpha = 0.0f;
        float riseProgress = 0.0f;
        float colorDarken = 0.0f;

        if (exactTime <= 36000f) {
            float p = (exactTime - 32000f) / 4000f;
            envelopeAlpha = p; 
            riseProgress = p;  
            colorDarken = p * 0.6f; 
        } 
        else if (exactTime <= 45000f) { 
            float p = (exactTime - 36000f) / 9000f;
            envelopeAlpha = 1.0f;
            riseProgress = 1.0f;
            colorDarken = 0.6f + (p * 0.4f); 
        } 
        else {
            float p = (exactTime - 45000f) / 3000f;
            envelopeAlpha = 1.0f - p; 
            riseProgress = 1.0f;
            colorDarken = 1.0f; 
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH); 
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, 
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, 
            GlStateManager.DestFactor.ZERO
        );
        GlStateManager.disableCull();
        GlStateManager.depthMask(false); 

        BufferBuilder buffer = tessellator.getBuffer();

        int layers = 5;
        int radialSegments = 40; 
        int verticalSegments = 12;
        
        float baseY = -40.0f;
        float topY = 160.0f;
        float currentMaxY = baseY + (topY - baseY) * riseProgress;
        
        float scrollTime = exactTime * 0.015f; 
        float globalSmokeAlpha = 0.85f;

        for (int l = 0; l < layers; l++) {
            float baseLayerRadius = 80.0f + (l * 14.0f);
            float layerOffset = l * 27.3f; 
            
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            for (int v = 0; v < verticalSegments; v++) {
                float pY1 = (float) v / verticalSegments;
                float pY2 = (float) (v + 1) / verticalSegments;
                
                float y1 = baseY + (topY - baseY) * pY1;
                float y2 = baseY + (topY - baseY) * pY2;

                float bottomFade1 = MathHelper.clamp(pY1 * 5.0f, 0.0f, 1.0f);
                float bottomFade2 = MathHelper.clamp(pY2 * 5.0f, 0.0f, 1.0f);

                float verticalFade1 = MathHelper.clamp(1.0f - pY1, 0.0f, 1.0f) * bottomFade1 * MathHelper.clamp((currentMaxY - y1) / 30.0f, 0.0f, 1.0f) * envelopeAlpha * globalSmokeAlpha;
                float verticalFade2 = MathHelper.clamp(1.0f - pY2, 0.0f, 1.0f) * bottomFade2 * MathHelper.clamp((currentMaxY - y2) / 30.0f, 0.0f, 1.0f) * envelopeAlpha * globalSmokeAlpha;

                float fire1 = MathHelper.clamp(1.0f - (pY1 * 4.0f), 0.0f, 1.0f) * (1.0f - colorDarken);
                float r1 = 0.03f + fire1 * 0.97f;
                float g1 = 0.03f + fire1 * 0.47f;
                float b1 = 0.04f + fire1 * 0.06f; 

                float fire2 = MathHelper.clamp(1.0f - (pY2 * 4.0f), 0.0f, 1.0f) * (1.0f - colorDarken);
                float r2 = 0.03f + fire2 * 0.97f;
                float g2 = 0.03f + fire2 * 0.47f;
                float b2 = 0.04f + fire2 * 0.06f;

                for (int i = 0; i < radialSegments; i++) {
                    float angle1 = (float) i * ((float) Math.PI * 2.0F) / radialSegments;
                    float angle2 = (float) (i + 1) * ((float) Math.PI * 2.0F) / radialSegments;

                    float rDist1 = MathHelper.sin(angle1 * 3.7f + layerOffset) * 14.0f 
                                 + MathHelper.cos(angle1 * 6.2f - layerOffset * 1.5f) * 9.0f 
                                 + MathHelper.sin(angle1 * 11.1f + layerOffset * 0.5f) * 6.0f
                                 + MathHelper.cos(angle1 * 19.4f) * 3.0f;
                                 
                    float rDist2 = MathHelper.sin(angle2 * 3.7f + layerOffset) * 14.0f 
                                 + MathHelper.cos(angle2 * 6.2f - layerOffset * 1.5f) * 9.0f 
                                 + MathHelper.sin(angle2 * 11.1f + layerOffset * 0.5f) * 6.0f
                                 + MathHelper.cos(angle2 * 19.4f) * 3.0f;
                    
                    float radius1 = baseLayerRadius + rDist1;
                    float radius2 = baseLayerRadius + rDist2;

                    float x1 = MathHelper.sin(angle1) * radius1;
                    float z1 = MathHelper.cos(angle1) * radius1;
                    float x2 = MathHelper.sin(angle2) * radius2;
                    float z2 = MathHelper.cos(angle2) * radius2;

                    float noise1 = (MathHelper.sin(scrollTime - pY1 * 15.0f + angle1 * 5.2f + layerOffset) + MathHelper.cos(scrollTime * 0.6f - pY1 * 9.4f - angle1 * 3.8f + layerOffset * 2.1f) + 2.0f) * 0.25f;
                    float noise2 = (MathHelper.sin(scrollTime - pY1 * 15.0f + angle2 * 5.2f + layerOffset) + MathHelper.cos(scrollTime * 0.6f - pY1 * 9.4f - angle2 * 3.8f + layerOffset * 2.1f) + 2.0f) * 0.25f;
                    float noise3 = (MathHelper.sin(scrollTime - pY2 * 15.0f + angle2 * 5.2f + layerOffset) + MathHelper.cos(scrollTime * 0.6f - pY2 * 9.4f - angle2 * 3.8f + layerOffset * 2.1f) + 2.0f) * 0.25f;
                    float noise4 = (MathHelper.sin(scrollTime - pY2 * 15.0f + angle1 * 5.2f + layerOffset) + MathHelper.cos(scrollTime * 0.6f - pY2 * 9.4f - angle1 * 3.8f + layerOffset * 2.1f) + 2.0f) * 0.25f;

                    float a1 = verticalFade1 * (0.2f + 0.8f * noise1);
                    float a2 = verticalFade1 * (0.2f + 0.8f * noise2);
                    float a3 = verticalFade2 * (0.2f + 0.8f * noise3);
                    float a4 = verticalFade2 * (0.2f + 0.8f * noise4);

                    buffer.pos(x1, y1, z1).color(r1, g1, b1, a1).endVertex();
                    buffer.pos(x2, y1, z2).color(r1, g1, b1, a2).endVertex();
                    buffer.pos(x2, y2, z2).color(r2, g2, b2, a3).endVertex();
                    buffer.pos(x1, y2, z1).color(r2, g2, b2, a4).endVertex();
                }
            }
            tessellator.draw();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }


    private void renderSurfaceBombardment(Tessellator tessellator, float exactTime) {
    	if (exactTime < 25000f || exactTime > 48000f) {
            return;
        }
        
        int activeJets = 0;
        if (exactTime >= 25000 && exactTime < 32000) {
            activeJets = 1 + (int)(17f * ((exactTime - 25000f) / 7000f)); 
        } else if (exactTime >= 32000 && exactTime <= 45000) {
            activeJets = 18;
        } else if (exactTime > 45000 && exactTime <= 48000) {
            activeJets = (int)(18f * (1.0f - ((exactTime - 45000f) / 3000f)));
        }

        if (activeJets <= 0) return;

        float intensity = MathHelper.clamp((exactTime - 25000f) / 10000f, 0.0f, 1.0f);
        
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);

        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        for (int i = 0; i < activeJets; i++) {
            float speedX = 0.012f + (i * 0.001f);
            float speedZ = 0.015f + (i * 0.0008f);
            float diveSpeed = 0.01f + (i * 0.0005f); 
            float offset = i * 43.7f;

            double x = MathHelper.sin(exactTime * speedX + offset) * 200.0;
            double z = MathHelper.cos(exactTime * speedZ + offset) * 200.0;
            
            float cycle = (exactTime * diveSpeed + offset) % ((float)Math.PI * 2f);
            double diveProfile = Math.pow(MathHelper.sin(cycle), 12.0); 
            double y = 140.0 - (diveProfile * 90.0); 

            double nextX = MathHelper.sin((exactTime + 1f) * speedX + offset) * 200.0;
            double nextZ = MathHelper.cos((exactTime + 1f) * speedZ + offset) * 200.0;
            float nextCycle = ((exactTime + 1f) * diveSpeed + offset) % ((float)Math.PI * 2f);
            double nextY = 140.0 - (Math.pow(MathHelper.sin(nextCycle), 12.0) * 90.0);

            double vX = nextX - x;
            double vY = nextY - y;
            double vZ = nextZ - z;

            float yaw = (float)(MathHelper.atan2(vX, vZ) * (180D / Math.PI));
            float horizontalDist = MathHelper.sqrt(vX * vX + vZ * vZ);
            float pitch = (float)(MathHelper.atan2(vY, horizontalDist) * (180D / Math.PI));

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-pitch, 1.0F, 0.0F, 0.0F);

            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            float c1 = 0.15f, c2 = 0.10f; 
            
            buffer.pos(0, 0, 3.0).color(c1, c1, c1, intensity).endVertex(); 
            buffer.pos(-1.5, 0, -1.0).color(c2, c2, c2, intensity).endVertex(); 
            buffer.pos(1.5, 0, -1.0).color(c2, c2, c2, intensity).endVertex();  
            
            buffer.pos(0, 0, 0.5).color(c2, c2, c2, intensity).endVertex(); 
            buffer.pos(0, 1.0, -1.0).color(c1, c1, c1, intensity).endVertex(); 
            buffer.pos(0, 0, -1.0).color(c2, c2, c2, intensity).endVertex(); 
            tessellator.draw();
            
            GlStateManager.popMatrix();
        }

        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        
        for (int i = 0; i < activeJets; i++) {
            float speedX = 0.012f + (i * 0.001f);
            float speedZ = 0.015f + (i * 0.0008f);
            float diveSpeed = 0.01f + (i * 0.0005f); 
            float offset = i * 43.7f;
            
            float cycle = (exactTime * diveSpeed + offset) % ((float)Math.PI * 2f);
            double diveProfile = Math.pow(MathHelper.sin(cycle), 12.0); 

            if (diveProfile > 0.6) {
                float snapInterval = 15.0f;
                float snappedTime = (float)Math.floor((exactTime + i * 11) / snapInterval) * snapInterval;

                double startX = MathHelper.sin(snappedTime * speedX + offset) * 200.0;
                double startZ = MathHelper.cos(snappedTime * speedZ + offset) * 200.0;
                float snappedCycle = (snappedTime * diveSpeed + offset) % ((float)Math.PI * 2f);
                double snappedDive = Math.pow(MathHelper.sin(snappedCycle), 12.0);
                double startY = 140.0 - (snappedDive * 90.0); 
                
                double nextStartX = MathHelper.sin((snappedTime + 1f) * speedX + offset) * 200.0;
                double nextStartZ = MathHelper.cos((snappedTime + 1f) * speedZ + offset) * 200.0;
                double vX = nextStartX - startX;
                double vZ = nextStartZ - startZ;

                double targetX = startX + (vX * 150.0);
                double targetZ = startZ + (vZ * 150.0);
                double targetY = -18.0; 

                float tracerSpeed = exactTime * 1.5f + i;
                int bulletsInBurst = 3;

                for (int b = 0; b < bulletsInBurst; b++) {
                    float p1 = ((tracerSpeed + b * 0.35f) % 1.0f); 
                    float p2 = Math.min(1.0f, p1 + 0.06f); 

                    double bx1 = startX + (targetX - startX) * p1; 
                    double by1 = startY + (targetY - startY) * p1; 
                    double bz1 = startZ + (targetZ - startZ) * p1;
                    
                    double bx2 = startX + (targetX - startX) * p2; 
                    double by2 = startY + (targetY - startY) * p2; 
                    double bz2 = startZ + (targetZ - startZ) * p2;

                    double w = 0.4; 
                    buffer.pos(bx1 - w, by1, bz1).color(1.0f, 0.9f, 0.2f, intensity).endVertex();
                    buffer.pos(bx1 + w, by1, bz1).color(1.0f, 0.9f, 0.2f, intensity).endVertex();
                    buffer.pos(bx2 + w, by2, bz2).color(1.0f, 0.5f, 0.0f, intensity).endVertex();
                    buffer.pos(bx2 - w, by2, bz2).color(1.0f, 0.5f, 0.0f, intensity).endVertex();
                }

                float impactScale = 2.0f + (float)(Math.random() * 4.0f); 
                buffer.pos(targetX - impactScale, targetY + impactScale, targetZ).color(1.0f, 0.4f, 0.0f, intensity).endVertex();
                buffer.pos(targetX + impactScale, targetY + impactScale, targetZ).color(1.0f, 0.4f, 0.0f, intensity).endVertex();
                buffer.pos(targetX + impactScale, targetY - impactScale, targetZ).color(0.2f, 0.1f, 0.0f, intensity).endVertex();
                buffer.pos(targetX - impactScale, targetY - impactScale, targetZ).color(0.2f, 0.1f, 0.0f, intensity).endVertex();
            }
        }
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}