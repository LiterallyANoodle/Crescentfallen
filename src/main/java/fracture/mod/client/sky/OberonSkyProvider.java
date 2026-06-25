package fracture.mod.client.sky;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mjr.mjrlegendslib.util.MCUtilities;

import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
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
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;

public class OberonSkyProvider extends IRenderHandler {

    private static final ResourceLocation URANUS_TEXTURE = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/uranus.png");
    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/planets/orbitalsun.png");
    private static final ResourceLocation TITANIA_TEXTURE = new ResourceLocation("extraplanets", "textures/gui/celestialbodies/titania.png");
    
    private static final float SKY_DAY_R = 0.12F;
    private static final float SKY_DAY_G = 0.14F;
    private static final float SKY_DAY_B = 0.28F;
    
    private static final float SKY_NIGHT_R = 0.001F;
    private static final float SKY_NIGHT_G = 0.001F;
    private static final float SKY_NIGHT_B = 0.004F;

    public int starList;
    public int glSkyList;
    public int glSkyList2;
    private float sunSize;

    private final RingGlisten[] ringGlistens;

    public OberonSkyProvider(IGalacticraftWorldProvider ceresProvider) {
        this.sunSize = 17.5F * ceresProvider.getSolarSize();
        this.ringGlistens = buildRingGlisten(420);

        int displayLists = GLAllocation.generateDisplayLists(3);
        this.starList = displayLists;
        this.glSkyList = displayLists + 1;
        this.glSkyList2 = displayLists + 2;

        GL11.glPushMatrix();
        GL11.glNewList(this.starList, GL11.GL_COMPILE);
        this.renderStars();
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
        float starBrightness = world.getStarBrightness(partialTicks);
        float dayBlend = 1.0F - starBrightness;
        float time = world.getTotalWorldTime() + partialTicks;

        float nightTransition = (float) Math.pow(starBrightness, 2.5D);
        float skyR = SKY_DAY_R * dayBlend + SKY_NIGHT_R * nightTransition;
        float skyG = SKY_DAY_G * dayBlend + SKY_NIGHT_G * nightTransition;
        float skyB = SKY_DAY_B * dayBlend + SKY_NIGHT_B * nightTransition;

        GlStateManager.disableTexture2D();
        GlStateManager.disableRescaleNormal();
        GlStateManager.color(skyR, skyG, skyB);
        GlStateManager.depthMask(false);
        GlStateManager.enableFog();
        GL11.glCallList(this.glSkyList);
        GlStateManager.disableFog();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();

        if (dayBlend > 0.05F) {
            renderDayHorizonGlow(tess, buffer, dayBlend, starBrightness);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderHelper.disableStandardItemLighting();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(110.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(95.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        
        renderUranusRing(tess, buffer, mc, time);
        
        GlStateManager.enableCull();
        GlStateManager.popMatrix();

        renderParallaxStars(world, partialTicks, starBrightness, time);

        float celestialAngle = world.getCelestialAngle(partialTicks);
        renderSunAndUranus(tess, buffer, world, mc, partialTicks, celestialAngle, starBrightness);

        renderTitania(tess, buffer, mc, celestialAngle);

        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    private void renderDayHorizonGlow(Tessellator tess, BufferBuilder buffer, float dayBlend, float starBrightness) {
        float glowAlpha = dayBlend * (1.0F - starBrightness * 0.6F) * 0.22F;
        if (glowAlpha <= 0.001F) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 0.0D, -40.0D).color(0.35F, 0.42F, 0.55F, glowAlpha * 0.15F).endVertex();
        buffer.pos(-70.0D, 0.0D, 8.0D).color(0.28F, 0.34F, 0.48F, 0.0F).endVertex();
        buffer.pos(0.0D, 0.0D, 18.0D).color(0.32F, 0.38F, 0.52F, glowAlpha * 0.35F).endVertex();
        buffer.pos(70.0D, 0.0D, 8.0D).color(0.28F, 0.34F, 0.48F, 0.0F).endVertex();
        buffer.pos(0.0D, 0.0D, -40.0D).color(0.35F, 0.42F, 0.55F, glowAlpha * 0.15F).endVertex();
        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    private void renderParallaxStars(WorldClient world, float partialTicks, float starBrightness, float time) {
        float celestialAngle = world.getCelestialAngle(partialTicks);
        float dayStarAlpha = 0.12F + starBrightness * 0.88F;

        renderStarLayer(celestialAngle, 0.0F, 1.0F, dayStarAlpha, time, 1.0F, 0.0F);
        renderStarLayer(celestialAngle, 22.0F, 0.62F, dayStarAlpha * 0.75F, time, 1.35F, 0.33F);
        renderStarLayer(celestialAngle, -18.0F, 0.38F, dayStarAlpha * 0.55F, time, 1.75F, 0.61F);
        renderStarGlintOverlay(world, partialTicks, dayStarAlpha, time);
    }

    private void renderStarLayer(float celestialAngle, float extraYaw, float scale, float alpha, float time, float twinkleSpeed, float phaseOffset) {
        if (alpha <= 0.01F) {
            return;
        }

        float flicker = 0.92F + 0.08F * MathHelper.sin(time * 0.02F * twinkleSpeed + phaseOffset);
        float layerAlpha = alpha * flicker;

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(extraYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, layerAlpha);
        GL11.glCallList(this.starList);
        GlStateManager.popMatrix();
    }

    private void renderStarGlintOverlay(WorldClient world, float partialTicks, float baseAlpha, float time) {
        if (baseAlpha <= 0.01F) {
            return;
        }

        Random rand = new Random(10842L);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        int count = ConfigManagerCore.moreStars ? 900 : 280;
        float celestialAngle = world.getCelestialAngle(partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < count; ++i) {
            double x = rand.nextFloat() * 2.0F - 1.0F;
            double y = rand.nextFloat() * 2.0F - 1.0F;
            double z = rand.nextFloat() * 2.0F - 1.0F;
            double distSq = x * x + y * y + z * z;

            if (distSq >= 1.0D || distSq <= 0.01D) {
                continue;
            }

            if (rand.nextFloat() > 0.18F) {
                continue;
            }

            double inv = 1.0D / Math.sqrt(distSq);
            float px = (float) (x * inv * 92.0D);
            float py = (float) (y * inv * 92.0D);
            float pz = (float) (z * inv * 92.0D);

            float phase = rand.nextFloat() * 6.2831855F;
            float speed = 0.35F + rand.nextFloat() * 0.45F;
            float wave = MathHelper.sin(time * 0.035F * speed + phase);
            float envelope = MathHelper.clamp((wave - 0.55F) * 3.5F, 0.0F, 1.0F);
            envelope = envelope * envelope * (3.0F - 2.0F * envelope);

            if (envelope < 0.02F) {
                continue;
            }

            float size = 0.045F + rand.nextFloat() * 0.035F;
            float brightness = baseAlpha * (0.35F + envelope * 1.4F);
            float r = 0.85F + envelope * 0.15F;
            float g = 0.88F + envelope * 0.12F;
            float b = 1.0F;

            buffer.pos(px - size, py - size, pz).color(r, g, b, brightness).endVertex();
            buffer.pos(px + size, py - size, pz).color(r, g, b, brightness).endVertex();
            buffer.pos(px + size, py + size, pz).color(r, g, b, brightness).endVertex();
            buffer.pos(px - size, py + size, pz).color(r, g, b, brightness).endVertex();
        }

        tess.draw();
        GlStateManager.popMatrix();
    }

    private void renderSunAndUranus(Tessellator tessellator1, BufferBuilder worldRenderer1, WorldClient world, Minecraft mc, float partialTicks, float celestialAngle, float starBrightness) {
        float sunVisibility = 1.0F - starBrightness;

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);

        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

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
        worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, a).endVertex();
        r = afloat[0] * f18;
        g = afloat[1] * f18;
        b = afloat[2] * f18;
        a = 0.0F;

        // Render base sun aura
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

        // Render larger sun aura pass
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
        float s = this.sunSize;
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer1.pos(-s, 100.0D, -s).tex(0.0D, 0.0D).endVertex();
        worldRenderer1.pos(s, 100.0D, -s).tex(1.0D, 0.0D).endVertex();
        worldRenderer1.pos(s, 100.0D, s).tex(1.0D, 1.0D).endVertex();
        worldRenderer1.pos(-s, 100.0D, s).tex(0.0D, 1.0D).endVertex();
        tessellator1.draw();

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.95F);
        mc.renderEngine.bindTexture(URANUS_TEXTURE);

        float uSize = 26.0F;
        worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer1.pos(-uSize, 100.0D, -uSize).tex(0, 0).endVertex();
        worldRenderer1.pos(uSize, 100.0D, -uSize).tex(1, 0).endVertex();
        worldRenderer1.pos(uSize, 100.0D, uSize).tex(1, 1).endVertex();
        worldRenderer1.pos(-uSize, 100.0D, uSize).tex(0, 1).endVertex();
        tessellator1.draw();
        GlStateManager.popMatrix();
    }

    private void renderTitania(Tessellator tessellator, BufferBuilder worldRenderer, Minecraft mc, float celestialAngle) {
        GlStateManager.pushMatrix();

        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((celestialAngle * 360.0F) + 180.0F, 1.0F, 0.0F, 0.0F);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        mc.renderEngine.bindTexture(TITANIA_TEXTURE);

        float tSize = 7.0F; 
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(-tSize, 100.0D, -tSize).tex(0.0D, 0.0D).endVertex();
        worldRenderer.pos(tSize, 100.0D, -tSize).tex(1.0D, 0.0D).endVertex();
        worldRenderer.pos(tSize, 100.0D, tSize).tex(1.0D, 1.0D).endVertex();
        worldRenderer.pos(-tSize, 100.0D, tSize).tex(0.0D, 1.0D).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
    }
    
    private void renderUranusRing(Tessellator tess, BufferBuilder buffer, Minecraft mc, float time) {
        renderRingSDither(tess, buffer);
        renderRingBand(tess, buffer);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0);
        renderRingGlisten(tess, buffer, mc, time);
    }

    private void renderRingBand(Tessellator tess, BufferBuilder buffer) {
        drawRingShell(tess, buffer, 98.0F, 6.0F, 180, 12, 0.42F, 0.01F, 0.18F, 0.82F);
    }

    private void renderRingSDither(Tessellator tess, BufferBuilder buffer) {
        drawRingShell(tess, buffer, 98.0F, 9.5F, 180, 10, 0.18F, 0.005F, 0.10F, 0.65F);
    }

    private void drawRingShell(Tessellator tess, BufferBuilder buffer, float radius, float ringHalfHeight,
            int segments, int thicknessSlices, float peakAlpha, float r, float g, float b) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (i * Math.PI * 2.0 / segments);
            float a2 = (float) ((i + 1) * Math.PI * 2.0 / segments);
            float aMid = (a1 + a2) * 0.5F;

            float warp = 1.0F + 0.12F * MathHelper.cos(aMid * 2.0F);
            float r1 = radius * (1.0F + 0.05F * MathHelper.cos(a1));
            float r2 = radius * (1.0F + 0.05F * MathHelper.cos(a2));
            float x1 = MathHelper.cos(a1) * r1;
            float z1 = MathHelper.sin(a1) * r1;
            float x2 = MathHelper.cos(a2) * r2;
            float z2 = MathHelper.sin(a2) * r2;

            for (int slice = 0; slice < thicknessSlices; slice++) {
                float t0 = (float) slice / thicknessSlices;
                float t1 = (float) (slice + 1) / thicknessSlices;
                float y0 = (t0 * 2.0F - 1.0F) * ringHalfHeight * warp;
                float y1 = (t1 * 2.0F - 1.0F) * ringHalfHeight * warp;
                
                float alpha0 = peakAlpha * thicknessFade(Math.abs(y0), ringHalfHeight * warp);
                float alpha1 = peakAlpha * thicknessFade(Math.abs(y1), ringHalfHeight * warp);

                float striations = 0.85F + 0.15F * MathHelper.sin(t0 * 36.0F) * MathHelper.cos(aMid * 3.0F);
                float cassiniGap = (t0 > 0.62F && t0 < 0.68F) ? 0.15F : 1.0F;
                
                float finalAlpha0 = alpha0 * striations * cassiniGap;
                float finalAlpha1 = alpha1 * striations * cassiniGap;

                buffer.pos(x1, y0, z1).color(r, g, b, finalAlpha0).endVertex();
                buffer.pos(x2, y0, z2).color(r, g, b, finalAlpha0).endVertex();
                buffer.pos(x2, y1, z2).color(r, g, b, finalAlpha1).endVertex();
                buffer.pos(x1, y1, z1).color(r, g, b, finalAlpha1).endVertex();
            }
        }
        tess.draw();
    }

    private float thicknessFade(float distFromCenter, float halfHeight) {
        float t = MathHelper.clamp(distFromCenter / halfHeight, 0.0F, 1.0F);
        float edge = 1.0F - smoothStep(0.30F, 1.0F, t);
        float shoulder = 1.0F - smoothStep(0.55F, 0.78F, t);
        return edge * (0.55F + 0.45F * shoulder);
    }

    private void renderRingGlisten(Tessellator tess, BufferBuilder buffer, Minecraft mc, float time) {
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0);

        java.nio.FloatBuffer matrixBuffer = org.lwjgl.BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrixBuffer);

        float rx = matrixBuffer.get(0);
        float ry = matrixBuffer.get(4);
        float rz = matrixBuffer.get(8);

        float ux = matrixBuffer.get(1);
        float uy = matrixBuffer.get(5);
        float uz = matrixBuffer.get(9);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (RingGlisten g : this.ringGlistens) {
            float alpha = computeGlistenAlpha(g, time);
            if (alpha < 0.05F) {
                continue;
            }

            float currentAngle = g.initialAngle + time * g.orbitSpeed;
            float px = MathHelper.cos(currentAngle) * g.radius;
            float pz = MathHelper.sin(currentAngle) * g.radius;
            float py = g.yOff;

            float spinAngle = time * g.spinSpeed + g.spinPhase;
            float cosS = MathHelper.cos(spinAngle) * g.size;
            float sinS = MathHelper.sin(spinAngle) * g.size;

            float v0x = -cosS + sinS;
            float v0y = -sinS - cosS;

            float v1x = cosS + sinS;
            float v1y = sinS - cosS;

            float v2x = cosS - sinS;
            float v2y = sinS + cosS;

            float v3x = -cosS - sinS;
            float v3y = -sinS + cosS;

            //Pass 1
            buffer.pos(px + rx * v0x + ux * v0y, py + ry * v0x + uy * v0y, pz + rz * v0x + uz * v0y)
                  .color(0.35F, 0.65F, 1.0F, alpha).endVertex();
            
            //Pass 2
            buffer.pos(px + rx * v1x + ux * v1y, py + ry * v1x + uy * v1y, pz + rz * v1x + uz * v1y)
                  .color(0.35F, 0.65F, 1.0F, alpha).endVertex();

            //Pass 3
            buffer.pos(px + rx * v2x + ux * v2y, py + ry * v2x + uy * v2y, pz + rz * v2x + uz * v2y)
                  .color(0.90F, 0.95F, 1.0F, alpha).endVertex();

            //Pass 4
            buffer.pos(px + rx * v3x + ux * v3y, py + ry * v3x + uy * v3y, pz + rz * v3x + uz * v3y)
                  .color(0.90F, 0.95F, 1.0F, alpha).endVertex();
        }

        tess.draw();
    }

    private float computeGlistenAlpha(RingGlisten g, float time) {
        float wave1 = MathHelper.sin(time * g.glintSpeed + g.glintPhase);
        float wave2 = MathHelper.cos(time * (g.glintSpeed * 0.67F) + g.glintPhase * 1.4F);
        float combined = (wave1 + wave2) * 0.5F;

        float flashIntensity = MathHelper.clamp((combined - 0.52F) / 0.48F, 0.0F, 1.0F);
        flashIntensity = flashIntensity * flashIntensity * (3.0F - 2.0F * flashIntensity); // Smoothstep ramp

        float baseline = 0.10F * g.baseWeight;
        float flash = flashIntensity * 0.88F * g.baseWeight;

        return baseline + flash;
    }
    private RingGlisten[] buildRingGlisten(int count) {
        Random rand = new Random(3333L);
        RingGlisten[] glistens = new RingGlisten[count];
        float radius = 97.5F;
        float ringHalfHeight = 5.0F;
        
        for (int i = 0; i < count; i++) {
            float angle = rand.nextFloat() * (float) (Math.PI * 2.0D);
            float yOff = (rand.nextFloat() * 2.0F - 1.0F) * ringHalfHeight * 0.95F;
            float r = radius + (rand.nextFloat() - 0.5F) * 1.2F;
            
            float size = 0.18F + rand.nextFloat() * 0.16F; 
            
            float orbitSpeed = 0.00015F + rand.nextFloat() * 0.0025F;
            
            float spinSpeed = 0.015F + rand.nextFloat() * 0.035F;
            float spinPhase = rand.nextFloat() * (float) Math.PI * 2.0F;
            
            float glintSpeed = 0.025F + rand.nextFloat() * 0.045F;
            float glintPhase = rand.nextFloat() * (float) Math.PI * 2.0F;
            float baseWeight = 0.6F + rand.nextFloat() * 0.4F;

            glistens[i] = new RingGlisten(r, yOff, angle, size, orbitSpeed, spinSpeed, spinPhase, glintSpeed, glintPhase, baseWeight);
        }
        return glistens;
    }

    private static float smoothStep(float edge0, float edge1, float x) {
        float t = MathHelper.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private void renderStars() {
        final Random rand = new Random(10842L);
        final Tessellator var2 = Tessellator.getInstance();
        BufferBuilder worldRenderer = var2.getBuffer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        for (int starIndex = 0; starIndex < (ConfigManagerCore.moreStars ? 35000 : 6000); ++starIndex) {
            double var4 = rand.nextFloat() * 2.0F - 1.0F;
            double var6 = rand.nextFloat() * 2.0F - 1.0F;
            double var8 = rand.nextFloat() * 2.0F - 1.0F;
            final double var10 = 0.12F + rand.nextFloat() * 0.08F;
            double var12 = var4 * var4 + var6 * var6 + var8 * var8;
            if (var12 < 1.0D && var12 > 0.01D) {
                var12 = 1.0D / Math.sqrt(var12);
                var4 *= var12;
                var6 *= var12;
                var8 *= var12;
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

    public float getSkyBrightness(float par1) {
        final float var2 = MCUtilities.getClient().world.getCelestialAngle(par1);
        float var3 = 1.0F - (MathHelper.sin(var2 * com.mjr.extraplanets.Constants.twoPI) * 2.0F + 0.25F);
        if (var3 < 0.0F) {
            var3 = 0.0F;
        }
        if (var3 > 1.0F) {
            var3 = 1.0F;
        }
        return var3 * var3;
    }

    private static final class RingGlisten {
        final float radius;
        final float yOff;
        final float initialAngle;
        final float size;
        final float orbitSpeed;
        final float spinSpeed;
        final float spinPhase;
        final float glintSpeed;
        final float glintPhase;
        final float baseWeight;

        RingGlisten(float radius, float yOff, float initialAngle, float size, float orbitSpeed, 
                    float spinSpeed, float spinPhase, float glintSpeed, float glintPhase, float baseWeight) {
            this.radius = radius;
            this.yOff = yOff;
            this.initialAngle = initialAngle;
            this.size = size;
            this.orbitSpeed = orbitSpeed;
            this.spinSpeed = spinSpeed;
            this.spinPhase = spinPhase;
            this.glintSpeed = glintSpeed;
            this.glintPhase = glintPhase;
            this.baseWeight = baseWeight;
        }
    }
}