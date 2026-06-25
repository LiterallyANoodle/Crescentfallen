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
    private static final ResourceLocation ceresTexture = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/ceres.png");

    public int starList, starListTwinkle, glSkyList, glSkyList2;
    private float sunSize;

    private List<ShootingStar> activeShootingStars = new ArrayList<>();
    private List<RogueStar> activeRogueStars = new ArrayList<>();
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

        Tessellator tess = Tessellator.getInstance();
        renderSkyGasBGCloud(tess, time);
        renderHorizon(tess, time);
        renderNebula(tess, time);

        GlStateManager.pushMatrix();
        
        GlStateManager.rotate(time * 0.005F, 0.15F, 1.0F, 0.25F);
        
        float starBrightness = 0.8F; 
        GlStateManager.color(starBrightness, starBrightness, starBrightness, starBrightness);
        GL11.glCallList(this.starList);
        
        float flicker = (MathHelper.sin(time * 0.03F) + MathHelper.cos(time * 0.015F)) * 0.5F;
        GlStateManager.color(starBrightness, starBrightness, starBrightness, starBrightness * (0.7F + 0.3F * flicker));
        GL11.glCallList(this.starListTwinkle);
        
        updateAndRenderGStars(tess, deltaTime);
        
        GlStateManager.popMatrix();

        updateAndRenderShootingStars(tess, deltaTime);
        renderOrbitalSystem(world, partialTicks, time);
        renderHome(mc);
        renderCeres(mc); 

        renderGreeble(tess, time);

        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    private void renderSkyGasBGCloud(Tessellator tess, float time) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableCull(); 
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        BufferBuilder buffer = tess.getBuffer();
        float r = 110.0F; 

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int res = 16; 
        for (int face = 0; face < 6; face++) {
            for (int x = 0; x < res; x++) {
                for (int y = 0; y < res; y++) {
                    float fx0 = (x / (float)res) * 2.0F - 1.0F;
                    float fy0 = (y / (float)res) * 2.0F - 1.0F;
                    float fx1 = ((x + 1) / (float)res) * 2.0F - 1.0F;
                    float fy1 = ((y + 1) / (float)res) * 2.0F - 1.0F;
                    drawSphereQuad(buffer, face, fx0, fy0, fx1, fy1, r, time);
                }
            }
        }
        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull(); 
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void drawSphereQuad(BufferBuilder buffer, int face, float x0, float y0, float x1, float y1, float radius, float time) {
        float[][] v = new float[4][3];
        switch (face) {
            case 0: v[0]=new float[]{1, y0, -x0}; v[1]=new float[]{1, y1, -x0}; v[2]=new float[]{1, y1, -x1}; v[3]=new float[]{1, y0, -x1}; break;
            case 1: v[0]=new float[]{-1, y0, x0}; v[1]=new float[]{-1, y1, x0}; v[2]=new float[]{-1, y1, x1}; v[3]=new float[]{-1, y0, x1}; break;
            case 2: v[0]=new float[]{x0, 1, -y0}; v[1]=new float[]{x1, 1, -y0}; v[2]=new float[]{x1, 1, -y1}; v[3]=new float[]{x0, 1, -y1}; break;
            case 3: v[0]=new float[]{x0, -1, y0}; v[1]=new float[]{x1, -1, y0}; v[2]=new float[]{x1, -1, y1}; v[3]=new float[]{x0, -1, y1}; break;
            case 4: v[0]=new float[]{x0, y0, 1}; v[1]=new float[]{x1, y0, 1}; v[2]=new float[]{x1, y1, 1}; v[3]=new float[]{x0, y1, 1}; break;
            case 5: v[0]=new float[]{-x0, y0, -1}; v[1]=new float[]{-x1, y0, -1}; v[2]=new float[]{-x1, y1, -1}; v[3]=new float[]{-x0, y1, -1}; break;
        }

        for (int i = 0; i < 4; i++) {
            float len = MathHelper.sqrt(v[i][0]*v[i][0] + v[i][1]*v[i][1] + v[i][2]*v[i][2]);
            float nx = v[i][0]/len; 
            float ny = v[i][1]/len; 
            float nz = v[i][2]/len;

            float noise = MathHelper.sin(nx * 4.0F + time * 0.01F) * MathHelper.cos(ny * 4.0F - time * 0.015F) * MathHelper.sin(nz * 4.0F + time * 0.005F);
            float intensity = Math.max(0, noise * 0.5F + 0.5F);

            float alpha = 0.15F + (0.3F * intensity); 
            float r = 0.02F;
            float g = 0.05F + (0.08F * intensity);
            float b = 0.12F + (0.15F * intensity);

            buffer.pos(nx * radius, ny * radius, nz * radius).color(r, g, b, alpha).endVertex();
        }
    }

    private void renderHorizon(Tessellator tess, float time) {
    	
    	//make this easier to see
    	
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableCull(); 
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        BufferBuilder buffer = tess.getBuffer();
        float radius = 105.0F; 
        int segments = 40;
        float height = 25.0F;
        float yOffset = -30.0F; 

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2.0 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2.0 / segments);

            float x1 = MathHelper.cos(angle1) * radius;
            float z1 = MathHelper.sin(angle1) * radius;
            float x2 = MathHelper.cos(angle2) * radius;
            float z2 = MathHelper.sin(angle2) * radius;

            float flicker1 = Math.max(0, MathHelper.sin(angle1 * 2.5F + time * 0.012F) * MathHelper.cos(angle1 * 1.5F - time * 0.008F));
            float flicker2 = Math.max(0, MathHelper.sin(angle2 * 2.5F + time * 0.012F) * MathHelper.cos(angle2 * 1.5F - time * 0.008F));

            float aBase = 0.15F; 
            float aGlow1 = aBase + (0.25F * flicker1);
            float aGlow2 = aBase + (0.25F * flicker2);

            float r = 0.12F, g = 0.08F, b = 0.28F;

            buffer.pos(x1, yOffset + height, z1).color(r, g, b, 0.0F).endVertex();
            buffer.pos(x2, yOffset + height, z2).color(r, g, b, 0.0F).endVertex();
            buffer.pos(x2, yOffset, z2).color(r, g, b, aGlow2).endVertex();
            buffer.pos(x1, yOffset, z1).color(r, g, b, aGlow1).endVertex();

            buffer.pos(x1, yOffset, z1).color(r, g, b, aGlow1).endVertex();
            buffer.pos(x2, yOffset, z2).color(r, g, b, aGlow2).endVertex();
            buffer.pos(x2, yOffset - height, z2).color(r, g, b, 0.0F).endVertex();
            buffer.pos(x1, yOffset - height, z1).color(r, g, b, 0.0F).endVertex();
        }
        tess.draw();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull(); 
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void renderNebula(Tessellator tess, float time) {
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
       
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        renderAsteroidBelt(tess, time);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 100.0D, 0.0D).color(1.0F, 1.0F, 0.9F, 0.8F).endVertex();
        for (int i = 0; i <= 16; i++) {
            float a = i * (float)Math.PI * 2.0F / 16.0F;
            buffer.pos(MathHelper.cos(a) * 32.0F, 100.0D, MathHelper.sin(a) * 32.0F).color(1.0F, 0.9F, 0.6F, 0.0F).endVertex();
        }
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); 
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 
        
        Minecraft.getMinecraft().renderEngine.bindTexture(sunTexture);
        float s = sunSize / 1.1F;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-s, 99.9D, -s).tex(0, 0).endVertex();
        buffer.pos(s, 99.9D, -s).tex(1, 0).endVertex();
        buffer.pos(s, 99.9D, s).tex(1, 1).endVertex();
        buffer.pos(-s, 99.9D, s).tex(0, 1).endVertex();
        tess.draw();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.popMatrix();
    }

    private void renderAsteroidBelt(Tessellator tess, float time) {
        GlStateManager.disableTexture2D();
        BufferBuilder buffer = tess.getBuffer();
        
        int count = 1200; 
        float sunCenterY = 100.0F; 
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        
        for (int i = 0; i < count; i++) {
            Random rand = new Random(i * 8731L);
            
            float radius = 80.0F + rand.nextFloat() * 40.0F; 
            float baseAngle = rand.nextFloat() * (float)Math.PI * 2.0F;
            float speed = 0.02F + rand.nextFloat() * 0.04F;
            float angle = baseAngle + (time * 0.005F * speed);
            
            float astX = radius * MathHelper.cos(angle);
            float astY = sunCenterY + radius * MathHelper.sin(angle); 
            float astZ = (rand.nextFloat() - 0.5F) * 25.0F + MathHelper.sin(angle * 2.0F) * 4.0F;

            float distToPlayer = MathHelper.sqrt(astX * astX + astY * astY + astZ * astZ);
            
            float fadeStart = 65.0F; 
            float fadeEnd = 30.0F;
            float alphaMult = MathHelper.clamp((distToPlayer - fadeEnd) / (fadeStart - fadeEnd), 0.0F, 1.0F);
            
            if (alphaMult <= 0.01F) continue; 
            
            float size = 0.15F + rand.nextFloat() * 0.5F; 
            float grey = 0.15F + rand.nextFloat() * 0.25F;
            float rCol = grey + 0.03F; 
            float gCol = grey;
            float bCol = grey - 0.03F;
            float finalAlpha = 0.85F * alphaMult;
            
            float rotX = time * (0.01F * rand.nextFloat()) + rand.nextFloat() * (float)Math.PI;
            float rotY = time * (0.01F * rand.nextFloat()) + rand.nextFloat() * (float)Math.PI;
            float rotZ = time * (0.01F * rand.nextFloat()) + rand.nextFloat() * (float)Math.PI;
            
            drawProceduralRock(buffer, astX, astY, astZ, size, rotX, rotY, rotZ, rCol, gCol, bCol, finalAlpha, rand);
        }
        
        tess.draw();
    }

    private void drawProceduralRock(BufferBuilder b, float cx, float cy, float cz, float size, float rx, float ry, float rz, float rCol, float gCol, float bCol, float alpha, Random rand) {
        float[][] v = new float[8][3];
        
        for (int i = 0; i < 8; i++) {
            float dx = (i & 1) == 0 ? -size : size;
            float dy = (i & 2) == 0 ? -size : size;
            float dz = (i & 4) == 0 ? -size : size;
            
            dx *= (0.6F + rand.nextFloat() * 0.8F);
            dy *= (0.6F + rand.nextFloat() * 0.8F);
            dz *= (0.6F + rand.nextFloat() * 0.8F);
            
            float x1 = dx * MathHelper.cos(ry) - dz * MathHelper.sin(ry);
            float z1 = dx * MathHelper.sin(ry) + dz * MathHelper.cos(ry);
            float y2 = dy * MathHelper.cos(rx) - z1 * MathHelper.sin(rx);
            float z2 = dy * MathHelper.sin(rx) + z1 * MathHelper.cos(rx);
            float x3 = x1 * MathHelper.cos(rz) - y2 * MathHelper.sin(rz);
            float y3 = x1 * MathHelper.sin(rz) + y2 * MathHelper.cos(rz);
            
            v[i][0] = cx + x3;
            v[i][1] = cy + y3;
            v[i][2] = cz + z2;
        }
        
        int[][] faces = {
            {0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, 
            {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}
        };
        
        float[] shades = {1.0F, 0.4F, 0.8F, 0.5F, 0.7F, 0.9F};
        
        for (int f = 0; f < 6; f++) {
            float faceR = rCol * shades[f];
            float faceG = gCol * shades[f];
            float faceB = bCol * shades[f];
            
            for (int j = 0; j < 4; j++) {
                float[] pt = v[faces[f][j]];
                b.pos(pt[0], pt[1], pt[2]).color(faceR, faceG, faceB, alpha).endVertex();
            }
        }
    }
    private void renderHome(Minecraft mc) {
        GlStateManager.pushMatrix();
        GlStateManager.enableTexture2D();
        float s = 0.5F;
        GlStateManager.scale(0.6F, 0.6F, 0.6F);
        GlStateManager.rotate(40.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200F, 1.0F, 0.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 
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

    private void renderCeres(Minecraft mc) {
        GlStateManager.pushMatrix();
        GlStateManager.enableTexture2D();
        float s = 0.3F; 
        GlStateManager.scale(0.6F, 0.6F, 0.6F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 
        mc.renderEngine.bindTexture(ceresTexture);
        
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

    private void updateAndRenderGStars(Tessellator tess, float dt) {
        if (this.starRandom.nextFloat() < 0.05F && activeRogueStars.size() < 15) {
            double theta = this.starRandom.nextDouble() * Math.PI * 2.0;
            double phi = this.starRandom.nextDouble() * Math.PI;
            double radius = 85.0D; 
            double sx = Math.sin(phi) * Math.cos(theta) * radius;
            double sy = Math.cos(phi) * radius;
            double sz = Math.sin(phi) * Math.sin(theta) * radius;
            double size = 0.15D + this.starRandom.nextDouble() * 0.15D; 
            activeRogueStars.add(new RogueStar(sx, sy, sz, size, 2.0F + this.starRandom.nextFloat() * 3.0F)); 
        }

        BufferBuilder buffer = tess.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        Iterator<RogueStar> it = activeRogueStars.iterator();
        while (it.hasNext()) {
            RogueStar star = it.next();
            star.age += dt;
            if (star.age >= star.maxAge) { it.remove(); continue; }
            
            float progress = star.age / star.maxAge;
            float brightness = MathHelper.sin(progress * (float)Math.PI);
            
            double yaw = Math.atan2(star.x, star.z);
            double sYaw = Math.sin(yaw), cYaw = Math.cos(yaw);
            double pitch = Math.atan2(Math.sqrt(star.x * star.x + star.z * star.z), star.y);
            double sPit = Math.sin(pitch), cPit = Math.cos(pitch);
            
            drawStarQuadColor(buffer, star.x, star.y, star.z, star.size, star.size, sYaw, cYaw, sPit, cPit, 0, 1, brightness);
        }
        tess.draw();
        
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void updateAndRenderShootingStars(Tessellator tess, float dt) {
        if (this.starRandom.nextFloat() < 0.002F) { 
            double theta = this.starRandom.nextDouble() * Math.PI * 2.0;
            double phi = this.starRandom.nextDouble() * Math.PI;
            
            double radius = 100.0D; 
            double sx = Math.sin(phi) * Math.cos(theta) * radius;
            double sy = Math.cos(phi) * radius;
            double sz = Math.sin(phi) * Math.sin(theta) * radius;
            
            double vx = (this.starRandom.nextDouble() - 0.5) * 30.0;
            double vy = -(15.0 + this.starRandom.nextDouble() * 20.0);
            double vz = (this.starRandom.nextDouble() - 0.5) * 30.0;
            
            activeShootingStars.add(new ShootingStar(sx, sy, sz, vx, vy, vz, 1.5F + this.starRandom.nextFloat()));
        }
        
        BufferBuilder buffer = tess.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        
        GlStateManager.glLineWidth(2.0F);
        
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        Iterator<ShootingStar> it = activeShootingStars.iterator();
        while (it.hasNext()) {
            ShootingStar star = it.next();
            star.age += dt;
            star.x += star.vx * dt; 
            star.y += star.vy * dt; 
            star.z += star.vz * dt;
            
            if (star.age >= star.maxAge) { it.remove(); continue; }
            float alpha = 1.0F - (star.age / star.maxAge);
            
            buffer.pos(star.x, star.y, star.z).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.pos(star.x - star.vx * 0.6, star.y - star.vy * 0.6, star.z - star.vz * 0.6).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
        }
        tess.draw();
        
        GlStateManager.glLineWidth(1.0F);
    }

    private static class ShootingStar {
        double x, y, z, vx, vy, vz;
        float age, maxAge;
        public ShootingStar(double x, double y, double z, double vx, double vy, double vz, float maxAge) {
            this.x = x; this.y = y; this.z = z; this.vx = vx; this.vy = vy; this.vz = vz; this.maxAge = maxAge;
        }
    }

    private static class RogueStar {
        double x, y, z, size;
        float age, maxAge;
        public RogueStar(double x, double y, double z, double size, float maxAge) {
            this.x = x; this.y = y; this.z = z; this.size = size; this.maxAge = maxAge;
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

    private void renderGreeble(Tessellator tess, float time) {
        GlStateManager.pushMatrix();
        GlStateManager.disableFog(); 
        
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
        
        GlStateManager.enableFog(); 
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

    // WIP
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