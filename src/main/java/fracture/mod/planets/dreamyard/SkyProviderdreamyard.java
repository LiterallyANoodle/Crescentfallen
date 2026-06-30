package fracture.mod.planets.dreamyard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import fracture.mod.CFInfo;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.client.FMLClientHandler;

public class SkyProviderdreamyard extends IRenderHandler {

    private static final ResourceLocation sunTexture = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/triopas.png");
    private static final ResourceLocation hollowsDreamyardSky = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/hollows.png");
    private static final ResourceLocation dreamyardRing = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/dreamyard_ring.png");
    private static final ResourceLocation dreamyardBubble = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/dreamyard_bubble.png");

    public int starList;
    public int glSkyList;
    public int glSkyList2;
    private float sunSize;
    
    // Shooting Star Logic
    private List<ShootingStar> activeShootingStars = new ArrayList<>();
    private Random starRandom = new Random();
    private long lastFrameTime = System.nanoTime();

    public SkyProviderdreamyard(IGalacticraftWorldProvider dreamyardProvider) {
        this.sunSize = 0.7F * dreamyardProvider.getSolarSize() * 7;

        int displayLists = GLAllocation.generateDisplayLists(3);
        this.starList = displayLists;
        this.glSkyList = displayLists + 1;
        this.glSkyList2 = displayLists + 2;

        // Bind stars to display list
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
        // Calculate delta time for animations
        long currentFrameTime = System.nanoTime();
        float deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000.0F;
        lastFrameTime = currentFrameTime;

        GlStateManager.pushMatrix();
        try {
            // Basic sky clear / setup
            GlStateManager.disableTexture2D();
            GlStateManager.disableRescaleNormal();
            GlStateManager.color(1F, 1F, 1F);
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            // Draw sky quads
            GL11.glColor3f(0, 0, 0);
            GL11.glCallList(this.glSkyList);

            // Sky color logic
            Vec3d vec3 = world.getSkyColor(mc.getRenderViewEntity(), partialTicks);
            float f1 = (float) vec3.x;
            float f2 = (float) vec3.y;
            float f3 = (float) vec3.z;

            // Determine Darkness (0.0 = Noon, 1.0 = Midnight)
            float starBrightness = world.getStarBrightness(partialTicks);

            // Add Dark Blue
            float navyR = 0.0F;
            float navyG = 0.0F;
            float navyB = 0.1F; 

            // Interpolate color towards darker blue based on darkness
            f1 = f1 * (1.0F - starBrightness) + navyR * starBrightness;
            f2 = f2 * (1.0F - starBrightness) + navyG * starBrightness;
            f3 = f3 * (1.0F - starBrightness) + navyB * starBrightness;

            if (mc.gameSettings.anaglyph) {
                float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
                float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
                float f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
                f1 = f4;
                f2 = f5;
                f3 = f6;
            }

            GlStateManager.color(f1, f2, f3);
            final Tessellator tessellator1 = Tessellator.getInstance();
            final BufferBuilder worldRenderer1 = tessellator1.getBuffer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();
            GlStateManager.color(f1, f2, f3);
            GL11.glCallList(this.glSkyList);

            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();

            // Sun / aura colors
            float[] afloat = new float[4];
            GlStateManager.disableTexture2D();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);

            // DYNAMICLY SPINNIN PARALAX STARS RENDERING BEGIN
            GlStateManager.pushMatrix();
            try {
                long worldTime = world.getTotalWorldTime();
                float celestialAngle = world.getCelestialAngle(partialTicks);
                
                // Laver 1 Main Stars
                GlStateManager.pushMatrix();
                GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-19.0F, 0f, 1.0f, 0f);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glCallList(this.starList);
                GlStateManager.popMatrix();

                // Laver 2 Stars 
                GlStateManager.pushMatrix();
                GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F); 
                GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-19.0F, 0f, 1.0f, 0f);
                GlStateManager.scale(0.6F, 0.6F, 0.6F); 
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.4F); 
                GL11.glCallList(this.starList);
                GlStateManager.popMatrix();

            } finally {
                GlStateManager.popMatrix();
            }
            
         // --- SHOOTING STARS ---
            this.updateAndRenderShootingStars(tessellator1, worldRenderer1, deltaTime);

            // --- SUN RENDERING ---
            GlStateManager.pushMatrix();
            try {
                // BUG FIX: The shooting stars method disabled blend. We MUST re-enable it!
                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                
                // Position the sun in the sky (Stationary concept)
                GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(60.0F, 1.0F, 0.0F, 0.0F); 

                float r = 255 / 255.0F;
                float g = 194 / 255.0F;
                float b = 180 / 255.0F;
                float auraAlpha = 0.4F; 

                // Additive Blending for a smooth, glowing aura
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                
                // --- Sun Aura Fan 1 (Inner Glow) ---
                double f10 = 20.0D;
                worldRenderer1.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
                worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, auraAlpha).endVertex(); // Center color
                
                // Outer ring (fades cleanly to 0 opacity now that blending is fixed)
                worldRenderer1.pos(-f10, 100.0D, -f10).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(0.0D, 100.0D, -f10 * 1.5D).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(f10, 100.0D, -f10).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(f10 * 1.5D, 100.0D, 0.0D).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(f10, 100.0D, f10).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(0.0D, 100.0D, f10 * 1.5D).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(-f10, 100.0D, f10).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(-f10 * 1.5D, 100.0D, 0.0D).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(-f10, 100.0D, -f10).color(r, g, b, 0.0F).endVertex(); // Closes the fan
                tessellator1.draw();
                
                // --- Sun Aura Fan 2 (Outer Glow) ---
                f10 = 40.0D;
                worldRenderer1.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
                worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, auraAlpha * 0.5F).endVertex();
                
                worldRenderer1.pos(-f10, 100.0D, -f10).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(0.0D, 100.0D, -f10 * 1.5D).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(f10, 100.0D, -f10).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(f10 * 1.5D, 100.0D, 0.0D).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(f10, 100.0D, f10).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(0.0D, 100.0D, f10 * 1.5D).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(-f10, 100.0D, f10).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(-f10 * 1.5D, 100.0D, 0.0D).color(r, g, b, 0.0F).endVertex();
                worldRenderer1.pos(-f10, 100.0D, -f10).color(r, g, b, 0.0F).endVertex();
                tessellator1.draw();

                // --- TEXTURE RESET ---
                // Reset to standard alpha blending for the core texture so it doesn't wash out
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.shadeModel(GL11.GL_FLAT);
                GlStateManager.enableTexture2D();
                
                // --- Sun Texture Body ---
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                double coreSize = (double) this.sunSize;
                mc.renderEngine.bindTexture(SkyProviderdreamyard.sunTexture);
                
                worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                worldRenderer1.pos(-coreSize, 100.0D, -coreSize).tex(0.0D, 0.0D).endVertex();
                worldRenderer1.pos(coreSize, 100.0D, -coreSize).tex(1.0D, 0.0D).endVertex();
                worldRenderer1.pos(coreSize, 100.0D, coreSize).tex(1.0D, 1.0D).endVertex();
                worldRenderer1.pos(-coreSize, 100.0D, coreSize).tex(0.0D, 1.0D).endVertex();
                tessellator1.draw();

            } finally {
                GlStateManager.popMatrix(); 
            }

            // PINK RAINBOW PLANETARY RING BASE
            GlStateManager.pushMatrix();
            try {
                // Removed rotation so it stays flat with horizon
                double ribbonX = 0.0D; 
                double ribbonY = 0.0D; 
                double ribbonZ = 0.0D; 
                // Passed 0 worldTime
                
                this.renderPinkRainbow(partialTicks, 0, 1.0F, ribbonX, ribbonY, ribbonZ);
            } finally {
                GlStateManager.popMatrix();
            }
            
            // SUNSET RIBBONS
            GlStateManager.pushMatrix();
            GlStateManager.rotate(5.0F, 1.0F, 0.0F, 0.0F); 
            this.renderSunset(partialTicks, world.getTotalWorldTime(), 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);

            // BEGIN OBJECT RENDERING (rings, planet, etc.)
            GlStateManager.pushMatrix();

            float timeDegrees = (world.getWorldTime() % 24000L) / 24000.0F * 360.0F;
            int segments = 64;
            float angleStep = 360F / segments;
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();

            // Main ring
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            GlStateManager.enableTexture2D();
            mc.renderEngine.bindTexture(dreamyardRing);


            GlStateManager.color(1F, 1F, 1F, 0.8F); 
            GlStateManager.rotate(timeDegrees * 0.1F, 0.0F, 1.0F, 0.0F);

            buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX);
            float uStep = 1.0F / segments;
            float radius = 80.0F;
            float height = 2.0F;
            for (int i = 0; i <= segments; i++) {
                float angleDeg = i * angleStep;
                double angleRad = Math.toRadians(angleDeg);
                double x = Math.cos(angleRad) * radius;
                double z = Math.sin(angleRad) * radius;
                float u = i * uStep;
                buffer.pos(x, 0.0D, z).tex(u, 0.0D).endVertex();
                buffer.pos(x, height, z).tex(u, 1.0D).endVertex();
            }
            tess.draw();

            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.color(1F, 1F, 1F, 1F);

            // Multi ring complex
            GlStateManager.pushMatrix();
            try {
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                GlStateManager.rotate(timeDegrees * 0.2F, 0.0F, 1.0F, 0.0F);

                float[][] rings = {
                    {50F, 2F, 0.3F, 1F, 0.4F, 0.7F},
                    {60F, 2.5F, 0.25F, 0.8F, 0.5F, 1F},
                    {70F, 1.5F, 0.2F, 0.6F, 1F, 0.7F},
                    {80F, 1F, 0.15F, 1F, 0.8F, 0.4F},
                };

                for (float[] ring : rings) {
                    float radius1 = ring[0];
                    float height1 = ring[1];
                    float alpha = ring[2];
                    float r1 = ring[3];
                    float g2 = ring[4];
                    float b3 = ring[5];

                    buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                    for (int i = 0; i <= segments; i++) {
                        float angleDeg = i * angleStep;
                        double angleRad = Math.toRadians(angleDeg);
                        double x = Math.cos(angleRad) * radius1;
                        double z = Math.sin(angleRad) * radius1;
                        buffer.pos(x, 0.0D, z).color(r1, g2, b3, alpha).endVertex();
                        buffer.pos(x, height1, z).color(r1, g2, b3, alpha).endVertex();
                    }
                    tess.draw();
                }
            } finally {
                GlStateManager.enableTexture2D();
                GlStateManager.enableDepth();
                GlStateManager.disableBlend();
                GL11.glPopAttrib();
                GlStateManager.popMatrix();
            }

            // RENDER HOLLOWS
            GlStateManager.pushMatrix();
            try {
            	//Changed rotation
                GlStateManager.rotate(34.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                
                float planetScale = 10.0F;
                float orbitDistance = 80.0F; 
                GlStateManager.translate(0.0F, orbitDistance, 0.0F); 

                GlStateManager.scale(planetScale, planetScale, planetScale);
                GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F); 
                
                GlStateManager.enableTexture2D();
                mc.renderEngine.bindTexture(hollowsDreamyardSky);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(-1.0D, -1.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
                buffer.pos(1.0D, -1.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
                buffer.pos(1.0D, 1.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
                buffer.pos(-1.0D, 1.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
                tess.draw();

            } finally {
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix(); // pop objects

        } finally {
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    private void renderStars() {
        final Random rand = new Random(10842L);
        final Tessellator var2 = Tessellator.getInstance();
        BufferBuilder worldRenderer = var2.getBuffer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        for (int starIndex = 0; starIndex < (65000); ++starIndex) {
            double var4 = rand.nextFloat() * 2.0F - 1.0F;
            double var6 = rand.nextFloat() * 2.0F - 1.0F;
            double var8 = rand.nextFloat() * 2.0F - 1.0F;
            final double var10 = 0.15F + rand.nextFloat() * 0.1F;
            double var12 = var4 * var4 + var6 * var6 + var8 * var8;

            if (var12 < 1.0D && var12 > 0.01D) {
                var12 = 1.0D / Math.sqrt(var12);
                var4 *= var12;
                var6 *= var12;
                var8 *= var12;
                final double var14 = var4 * (rand.nextDouble() * 150D + 130D);
                final double var16 = var6 * (rand.nextDouble() * 150D + 130D);
                final double var18 = var8 * (rand.nextDouble() * 150D + 130D);
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
    
    // --- SHOOTING STAR LOGIC ---
    
    private static class ShootingStar {
        double x, y, z;      // Current position
        double vx, vy, vz;   // Velocity
        double r, g, b;      // Color
        float age;           // Current age
        float maxAge;        // Lifespan
        float size;          // Thickness

        public ShootingStar(double x, double y, double z, double vx, double vy, double vz, float maxAge) {
            this.x = x; this.y = y; this.z = z;
            this.vx = vx; this.vy = vy; this.vz = vz;
            this.maxAge = maxAge;
            this.age = 0;
            this.size = 2.0F + (float)Math.random() * 2.0F;
            this.r = 0.8F + (float)Math.random() * 0.2F;
            this.g = 0.9F + (float)Math.random() * 0.1F;
            this.b = 1.0F;
        }
    }

    private void updateAndRenderShootingStars(Tessellator tess, BufferBuilder buffer, float dt) {
        // 1. Spawning 
        // Adjust probability: 0.005 chance per frame ~ 3 seconds on 60fps
        if (this.starRandom.nextFloat() < 0.01F) { 
            double theta = this.starRandom.nextDouble() * Math.PI * 2.0;
            double phi = this.starRandom.nextDouble() * Math.PI; // Full sphere coverage
            double radius = 90.0D; // interior skybox
            
            double sx = Math.sin(phi) * Math.cos(theta) * radius;
            double sy = Math.cos(phi) * radius;
            double sz = Math.sin(phi) * Math.sin(theta) * radius;
            
            // Velocity: Picks random destination on the sphere
            double destTheta = this.starRandom.nextDouble() * Math.PI * 2.0;
            double destPhi = this.starRandom.nextDouble() * Math.PI;
            double ex = Math.sin(destPhi) * Math.cos(destTheta) * radius;
            double ey = Math.cos(destPhi) * radius;
            double ez = Math.sin(destPhi) * Math.sin(destTheta) * radius;
            
            // Normalize direction and multiply by speed
            double dx = ex - sx;
            double dy = ey - sy;
            double dz = ez - sz;
            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
            double speed = 80.0D; // Units per second
            
            activeShootingStars.add(new ShootingStar(sx, sy, sz, (dx/dist)*speed, (dy/dist)*speed, (dz/dist)*speed, 1.5F));
        }
        
        // 2. Render and Update
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        
        Iterator<ShootingStar> it = activeShootingStars.iterator();
        while (it.hasNext()) {
            ShootingStar star = it.next();
            
            // Update
            star.age += dt;
            star.x += star.vx * dt;
            star.y += star.vy * dt;
            star.z += star.vz * dt;
            
            if (star.age >= star.maxAge) {
                it.remove();
                continue;
            }
            
            // Render Trail
            float lifeRatio = 1.0F - (star.age / star.maxAge);
            float alphaHead = lifeRatio;
            float alphaTail = 0.0F;
            
            // Trail length based on velocity
            double trailLen = 0.2D; // fraction of velocity vector
            double tx = star.x - star.vx * trailLen;
            double ty = star.y - star.vy * trailLen;
            double tz = star.z - star.vz * trailLen;
            
            buffer.pos(star.x, star.y, star.z).color((float)star.r, (float)star.g, (float)star.b, alphaHead).endVertex();
            buffer.pos(tx, ty, tz).color((float)star.r, (float)star.g, (float)star.b, alphaTail).endVertex();
        }
        
        tess.draw();
        
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

//    // NIGHT SPHERE
//    private void renderNightWaveSphere(float partialTicks, long worldTime, float nightStrength) {
//        GlStateManager.disableTexture2D();
//        GlStateManager.enableBlend();
//        GlStateManager.depthMask(false);
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GlStateManager.shadeModel(GL11.GL_SMOOTH);
//
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder buffer = tessellator.getBuffer();
//
//        double time = (worldTime + partialTicks) * 0.05D;
//        double radius = 95.0D; // Large sphere encircling everything
//        
//        int stacks = 20; // Latitude
//        int slices = 40; // Longitude
//
//        for (int i = 0; i < stacks; i++) {
//            double lat0 = Math.PI * (-0.5 + (double) (i) / stacks);
//            double z0 = Math.sin(lat0);
//            double zr0 = Math.cos(lat0);
//
//            double lat1 = Math.PI * (-0.5 + (double) (i + 1) / stacks);
//            double z1 = Math.sin(lat1);
//            double zr1 = Math.cos(lat1);
//
//            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
//            for (int j = 0; j <= slices; j++) {
//                double lng = 2 * Math.PI * (double) (j - 1) / slices;
//                double x = Math.cos(lng);
//                double y = Math.sin(lng);
//
//                // Add wave distortion to radius
//                double wave1 = Math.sin(lng * 6.0 + time + lat0 * 4.0) * 2.0;
//                double wave2 = Math.cos(lat0 * 10.0 - time * 0.5) * 1.5;
//                double r0 = radius + wave1 + wave2;
//
//                double wave3 = Math.sin(lng * 6.0 + time + lat1 * 4.0) * 2.0;
//                double wave4 = Math.cos(lat1 * 10.0 - time * 0.5) * 1.5;
//                double r1 = radius + wave3 + wave4;
//
//                float alpha = 0.15F * nightStrength; // Fades in at night
//                
//                // Light blue color
//                buffer.pos(x * zr0 * r0, y * zr0 * r0, z0 * r0)
//                      .color(0.4F, 0.7F, 1.0F, alpha).endVertex();
//                
//                buffer.pos(x * zr1 * r1, y * zr1 * r1, z1 * r1)
//                      .color(0.4F, 0.6F, 0.9F, alpha).endVertex();
//            }
//            tessellator.draw();
//        }
//
//        GlStateManager.shadeModel(GL11.GL_FLAT);
//        GlStateManager.depthMask(true);
//        GlStateManager.enableTexture2D();
//    }
    
    // Render Pink Rainbow independent code
    private void renderPinkRainbow(float partialTicks, long worldTime, float brightness, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull(); 
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Geometry
        double innerRadius = 60.0D; 
        double outerRadius = 90.0D; 
        
        // Ring time logic used to be here
        
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        int steps = 180;
        for (int i = 0; i <= steps; i++) {
            double angle = (double)i / (double)steps * (Math.PI * 2.0);
            
            // Calculate Inner Position (flat on XZ plane, y=0)
            double xIn = Math.cos(angle) * innerRadius;
            double zIn = Math.sin(angle) * innerRadius;
            
            // Calculate Outer Position
            double xOut = Math.cos(angle) * outerRadius;
            double zOut = Math.sin(angle) * outerRadius;

            double hue = 0.85 + (Math.sin(angle * 2.0) * 0.15); 
            int color = java.awt.Color.HSBtoRGB((float)hue, 0.6f, 1.0f);
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;

            buffer.pos(xIn, 0.0D, zIn).color(r, g, b, 0.5F * brightness).endVertex();
            
            buffer.pos(xOut, 0.0D, zOut).color(r, g, b, 0.0F).endVertex(); 
        }

        tessellator.draw();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
    
    // Render Sunset independent code
    
    private void renderSunset(float partialTicks, long worldTime, float brightness) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull(); 
        GlStateManager.shadeModel(GL11.GL_SMOOTH); 

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        double time = (worldTime + partialTicks) * 0.02D; 
        double radius = 75.0D; 
        double ribbonWidth = 75.0D; 
        double yLevel = 7.0D; 

        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        int steps = 180; 
        for (int i = 0; i <= steps; i++) {
            double angle = (double)i / (double)steps * (Math.PI * 2.0);
            double topWave = Math.sin(angle * 4.0 + time) * 4.0 + Math.cos(angle * 12.0 + time * 2.0) * 2.0;
            double bottomWave = Math.sin(angle * 5.0 - time) * 3.0 + Math.cos(angle * 8.0 + time * 1.5) * 2.0;
            
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            double topY = yLevel + (ribbonWidth / 2) + topWave;
            double bottomY = yLevel - (ribbonWidth / 2) + bottomWave;
            float alpha = 0.7F * brightness; 
            buffer.pos(x, topY, z).color(0.4F, 0.1F, 0.6F, 0.0F).endVertex();
            buffer.pos(x, bottomY, z).color(1.0F, 0.6F, 0.2F, alpha).endVertex();
        }
        tessellator.draw();

        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= steps; i++) {
            double angle = (double)i / (double)steps * (Math.PI * 2.0);
            double x = Math.cos(angle) * (radius * 0.95); 
            double z = Math.sin(angle) * (radius * 0.95);
            double topWave = Math.sin(angle * 4.0 + time + 1.0) * 3.0; 
            double bottomWave = Math.sin(angle * 5.0 - time + 1.0) * 2.0;

            double topY = yLevel + (ribbonWidth * 0.3) + topWave;
            double bottomY = yLevel - (ribbonWidth * 0.3) + bottomWave;
            buffer.pos(x, topY, z).color(0.8F, 0.2F, 0.4F, 0.0F).endVertex();
            buffer.pos(x, bottomY, z).color(1.0F, 0.3F, 0.1F, 0.5F * brightness).endVertex();
        }
        tessellator.draw();

        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    //Misc
    public float getSkyBrightness(float par1) {
        final float var2 = FMLClientHandler.instance().getClient().world.getCelestialAngle(par1);
        float var3 = 1.0F - (MathHelper.sin(var2 * Constants.twoPI) * 2.0F + 0.25F);

        if (var3 < 0.0F) {
            var3 = 0.0F;
        }

        if (var3 > 1.0F) {
            var3 = 1.0F;
        }

        return var3 * var3 * 1F;
    }

}