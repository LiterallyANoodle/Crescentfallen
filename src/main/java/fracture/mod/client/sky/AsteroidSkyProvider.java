package fracture.mod.client.sky;

import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

import fracture.mod.CFInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AsteroidSkyProvider extends IRenderHandler {

    private static final ResourceLocation overworldTexture = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/oncegreatearth.png");
    private static final ResourceLocation sunTexture = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/orbitalsun.png");

    public int starList, starListTwinkle, glSkyList, glSkyList2;
    private float sunSize;

    private List<ShootingStar> activeShootingStars = new ArrayList<>();
    private Random starRandom = new Random();
    private long lastFrameTime = System.nanoTime();

    public AsteroidSkyProvider(IGalacticraftWorldProvider provider) {
        this.sunSize = 17.5F * provider.getSolarSize();
        int lists = GLAllocation.generateDisplayLists(4);
        this.starList = lists;
        this.starListTwinkle = lists + 1;
        this.glSkyList = lists + 2;
        this.glSkyList2 = lists + 3;

        GL11.glNewList(this.starList, GL11.GL_COMPILE);
        this.renderStars(true);
        GL11.glEndList();

        GL11.glNewList(this.starListTwinkle, GL11.GL_COMPILE);
        this.renderStars(false);
        GL11.glEndList();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
        drawSkyShell(buffer, 16F, true);
        GL11.glEndList();
        GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
        drawSkyShell(buffer, -16F, false);
        GL11.glEndList();
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        long currentFrameTime = System.nanoTime();
        float deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000.0F;
        lastFrameTime = currentFrameTime;
        float time = world.getTotalWorldTime() + partialTicks;

        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.depthMask(false);

        GlStateManager.color(0, 0, 0);
        GL11.glCallList(this.glSkyList);

        renderNebulaShell(Tessellator.getInstance(), time);

        GlStateManager.pushMatrix();
        float sway = MathHelper.sin(time * 0.006F) * 2.0F; 
        GlStateManager.rotate(sway, 0.0F, 1.0F, 0.0F);
        float starBrightness = 0.8F; 
        GlStateManager.color(starBrightness, starBrightness, starBrightness, starBrightness);
        GL11.glCallList(this.starList);
        
        float flicker = (MathHelper.sin(time * 0.03F) + MathHelper.cos(time * 0.015F)) * 0.5F;
        GlStateManager.color(starBrightness, starBrightness, starBrightness, starBrightness * (0.7F + 0.3F * flicker));
        GL11.glCallList(this.starListTwinkle);
        GlStateManager.popMatrix();

        updateAndRenderShootingStars(Tessellator.getInstance(), deltaTime);
        renderOrbitalSystem(world, partialTicks, time);
        renderHome(mc);

        renderSpaceGreebles(Tessellator.getInstance(), time);

        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    private void renderNebulaShell(Tessellator tess, float time) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        BufferBuilder b = tess.getBuffer();
        float r = 98.0F;
        float a = 0.07F + MathHelper.sin(time * 0.01F) * 0.02F;

        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        b.pos(-r, r, -r).color(0.01F, 0.01F, 0.1F, a).endVertex();
        b.pos(r, r, -r).color(0.02F, 0.02F, 0.15F, a).endVertex();
        b.pos(r, r, r).color(0.01F, 0.01F, 0.1F, a).endVertex();
        b.pos(-r, r, r).color(0.02F, 0.02F, 0.15F, a).endVertex();

        b.pos(-r, -r, r).color(0.01F, 0.01F, 0.1F, a).endVertex();
        b.pos(r, -r, r).color(0.02F, 0.02F, 0.15F, a).endVertex();
        b.pos(r, -r, -r).color(0.01F, 0.01F, 0.1F, a).endVertex();
        b.pos(-r, -r, -r).color(0.02F, 0.02F, 0.15F, a).endVertex();
        tess.draw();

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void renderOrbitalSystem(WorldClient world, float partialTicks, float time) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        float celestialAngle = world.getCelestialAngle(partialTicks);
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        
        // SUN AURA
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 100.0D, 0.0D).color(1.0F, 0.9F, 0.7F, 0.4F).endVertex();
        for (int i = 0; i <= 16; i++) {
            float a = i * (float)Math.PI * 2.0F / 16.0F;
            buffer.pos(MathHelper.cos(a) * 20.0F, 100.0D, MathHelper.sin(a) * 20.0F).color(1.0F, 0.6F, 0.2F, 0.0F).endVertex();
        }
        tess.draw();

        // SUN TEXTURE
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().renderEngine.bindTexture(sunTexture);
        float s = sunSize / 1.1F;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-s, 99.9D, -s).tex(0, 0).endVertex();
        buffer.pos(s, 99.9D, -s).tex(1, 0).endVertex();
        buffer.pos(s, 99.9D, s).tex(1, 1).endVertex();
        buffer.pos(-s, 99.9D, s).tex(0, 1).endVertex();
        tess.draw();

        renderAsteroidBelt(tess, time);
        GlStateManager.popMatrix();
    }

    private void renderAsteroidBelt(Tessellator tess, float time) {
        GlStateManager.disableTexture2D();
        BufferBuilder buffer = tess.getBuffer();
        int count = 120;
        float radius = 90.0F; 
        for (int i = 0; i < count; i++) {
            float angle = ((float)i / (float)count) * 360.0F + (time * 0.05F) + (i * 1.5F);
            float radAngle = (float)Math.toRadians(angle);
            float x = MathHelper.cos(radAngle) * radius;
            float z = MathHelper.sin(radAngle) * radius;
            float y = MathHelper.sin(time * 0.01F + i) * 5.0F;

            buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            Random r = new Random(i * 1000L);
            float size = 0.4F + r.nextFloat() * 0.8F;
            float grey = 0.3F + r.nextFloat() * 0.2F;
            buffer.pos(x, y + size, z).color(grey + 0.1F, grey + 0.1F, grey + 0.1F, 1.0F).endVertex();
            buffer.pos(x + size, y, z).color(grey, grey, grey, 1.0F).endVertex();
            buffer.pos(x, y, z + size).color(grey - 0.1F, grey - 0.1F, grey - 0.1F, 1.0F).endVertex();
            buffer.pos(x - size, y, z).color(grey, grey, grey, 1.0F).endVertex();
            buffer.pos(x, y, z - size).color(grey - 0.1F, grey - 0.1F, grey - 0.1F, 1.0F).endVertex();
            buffer.pos(x + size, y, z).color(grey, grey, grey, 1.0F).endVertex();
            tess.draw();
        }
    }

    private void renderHome(Minecraft mc) {
        GlStateManager.pushMatrix();
        GlStateManager.enableTexture2D();
        float s = 0.5F;
        GlStateManager.scale(0.6F, 0.6F, 0.6F);
        GlStateManager.rotate(40.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200F, 1.0F, 0.0F, 0.0F);
        mc.renderEngine.bindTexture(overworldTexture);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-s, -100.0D, s).tex(0, 1).endVertex();
        buffer.pos(s, -100.0D, s).tex(1, 1).endVertex();
        buffer.pos(s, -100.0D, -s).tex(1, 0).endVertex();
        buffer.pos(-s, -100.0D, -s).tex(0, 0).endVertex();
        tess.draw();
        GlStateManager.popMatrix();
    }

    private void updateAndRenderShootingStars(Tessellator tess, float dt) {
        if (this.starRandom.nextFloat() < 0.02F) { 
            double theta = this.starRandom.nextDouble() * Math.PI * 2.0;
            double phi = this.starRandom.nextDouble() * Math.PI;
            double radius = 90.0D;
            double sx = Math.sin(phi) * Math.cos(theta) * radius;
            double sy = Math.cos(phi) * radius;
            double sz = Math.sin(phi) * Math.sin(theta) * radius;
            activeShootingStars.add(new ShootingStar(sx, sy, sz, -sx/2, -sy/2, -sz/2, 1.5F));
        }
        BufferBuilder buffer = tess.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        Iterator<ShootingStar> it = activeShootingStars.iterator();
        while (it.hasNext()) {
            ShootingStar star = it.next();
            star.age += dt;
            star.x += star.vx * dt; star.y += star.vy * dt; star.z += star.vz * dt;
            if (star.age >= star.maxAge) { it.remove(); continue; }
            float alpha = 1.0F - (star.age / star.maxAge);
            buffer.pos(star.x, star.y, star.z).color(1, 1, 1, alpha).endVertex();
            buffer.pos(star.x - star.vx*0.7, star.y - star.vy*0.7, star.z - star.vz*0.7).color(1, 1, 1, 0).endVertex();
        }
        tess.draw();
    }

    private static class ShootingStar {
        double x, y, z, vx, vy, vz;
        float age, maxAge;
        public ShootingStar(double x, double y, double z, double vx, double vy, double vz, float maxAge) {
            this.x = x; this.y = y; this.z = z; this.vx = vx; this.vy = vy; this.vz = vz; this.maxAge = maxAge;
        }
    }

    private void renderStars(boolean staticPass) {
        Random rand = new Random(10842L);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int count = ConfigManagerCore.moreStars ? 12000 : 4500;
        for (int i = 0; i < count; ++i) {
            double x = rand.nextFloat() * 2.0F - 1.0F;
            double y = rand.nextFloat() * 2.0F - 1.0F;
            double z = rand.nextFloat() * 2.0F - 1.0F;
            double size = 0.07F + rand.nextFloat() * 0.06F;
            double distSq = x * x + y * y + z * z;
            if (distSq < 1.0D && distSq > 0.01D) {
                boolean twink = rand.nextFloat() < 0.28F;
                if (staticPass && twink) continue;
                if (!staticPass && !twink) continue;
                double invDist = 1.0D / Math.sqrt(distSq);
                double pX = x * invDist * 85.0D;
                double pY = y * invDist * 85.0D;
                double pZ = z * invDist * 85.0D;
                double yaw = Math.atan2(x, z);
                double sYaw = Math.sin(yaw), cYaw = Math.cos(yaw);
                double pitch = Math.atan2(Math.sqrt(x * x + z * z), y);
                double sPit = Math.sin(pitch), cPit = Math.cos(pitch);
                double roll = rand.nextDouble() * Math.PI * 2.0D;
                double sRol = Math.sin(roll), cRol = Math.cos(roll);
                drawStarQuadColor(buffer, pX, pY, pZ, size, size, sYaw, cYaw, sPit, cPit, sRol, cRol, 1.0F);
            }
        }
        tess.draw();
    }

    private void drawStarQuadColor(BufferBuilder b, double x, double y, double z, double w, double h, double s1, double c1, double s2, double c2, double s3, double c3, float brightness) {
        for (int i = 0; i < 4; ++i) {
            double lY = ((i & 2) - 1) * w, lZ = ((i + 1 & 2) - 1) * h;
            double rX = lY * c3 - lZ * s3, rZ = lZ * c3 + lY * s3;
            double rY2 = rX * s2, rX2 = -rX * c2;
            double fX = rX2 * s1 - rZ * c1, fZ = rZ * s1 + rX2 * c1;
            b.pos(x + fX, y + rY2, z + fZ).color(brightness, brightness, brightness, brightness).endVertex();
        }
    }

    private void renderSpaceGreebles(Tessellator tess, float time) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.bindTexture(0);
        GlStateManager.alphaFunc(516, 0.0F);
        GlStateManager.enableAlpha();
        
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        
        drawGreeble(tess, time, 130F, 30F, 1.1F, 0.5F, 0.1F, 0.8F, 4); 
        drawGreeble(tess, time, 45F, -20F, 0.9F, 0.2F, 0.6F, 0.5F, 5); 
        drawGreeble(tess, time, 280F, 15F, 1.3F, 0.6F, 0.0F, 0.3F, 3); 
        drawGreeble(tess, time, 190F, -40F, 1.0F, 0.4F, 0.1F, 0.9F, 6); 
        drawGreeble(tess, time, 0F, 60F, 0.7F, 0.1F, 0.5F, 0.3F, 4); 
        
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    private void drawGreeble(Tessellator tess, float time, float rotY, float rotX, float sizeMult, float r, float g, float b, int petals) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(rotY, 0, 1, 0);
        GlStateManager.rotate(rotX, 1, 0, 0);
        BufferBuilder buffer = tess.getBuffer();
        for (int l = 0; l < 2; l++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(time * (0.01F + (l * 0.005F)), 0, 0, 1); 
            float sc = sizeMult * (1.0F + (l * 0.15F));
            GlStateManager.scale(sc, sc, sc);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            int res = 10;
            float sz = 35.0F;
            float step = (sz * 2) / res;
            for (int xi = 0; xi < res; xi++) {
                for (int yi = 0; yi < res; yi++) {
                    drawCloudVtxAdvanced(buffer, xi, yi, step, sz, time, l, r, g, b, petals);
                    drawCloudVtxAdvanced(buffer, xi + 1, yi, step, sz, time, l, r, g, b, petals);
                    drawCloudVtxAdvanced(buffer, xi + 1, yi + 1, step, sz, time, l, r, g, b, petals);
                    drawCloudVtxAdvanced(buffer, xi, yi + 1, step, sz, time, l, r, g, b, petals);
                }
            }
            tess.draw();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private void drawCloudVtxAdvanced(BufferBuilder b, int xi, int yi, float step, float size, float t, int l, float cr, float cg, float cb, int petals) {
        float px = -size + (xi * step);
        float py = -size + (yi * step);
        float dist = MathHelper.sqrt(px * px + py * py);
        float angle = (float)Math.atan2(px, py);
        float shapeFactor = 1.0F + 0.45F * MathHelper.sin(angle * (float)petals + t * 0.02F); 
        float maxDist = size * 0.85F * shapeFactor;
        if (dist > maxDist) { b.pos(px, py, -100).color(0,0,0,0).endVertex(); return; }
        float wz = MathHelper.sin(t * 0.01F + (xi + yi)) * 3.0F; 
        float gr = MathHelper.sin(xi * 1.2F + l) * MathHelper.cos(yi * 1.2F - l);
        float alpha = (1.0F - (dist / maxDist)) * (0.25F + (0.15F * gr));
        b.pos(px, py, -100 + wz).color(cr, cg, cb + (gr * 0.1F), Math.max(0, alpha)).endVertex();
    }

    private void drawSkyShell(BufferBuilder b, float f, boolean reverse) {
        for (int j = -384; j <= 384; j += 64) {
            for (int l = -384; l <= 384; l += 64) {
                b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
                if (reverse) {
                    b.pos(j, f, l).endVertex(); b.pos(j + 64, f, l).endVertex();
                    b.pos(j + 64, f, l + 64).endVertex(); b.pos(j, f, l + 64).endVertex();
                } else {
                    b.pos(j + 64, f, l).endVertex(); b.pos(j, f, l).endVertex();
                    b.pos(j, f, l + 64).endVertex(); b.pos(j + 64, f, l + 64).endVertex();
                }
                Tessellator.getInstance().draw();
            }
        }
    }
}