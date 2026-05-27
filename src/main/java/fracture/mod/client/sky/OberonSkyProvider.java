package fracture.mod.client.sky;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mjr.mjrlegendslib.util.MCUtilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;

import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;


public class OberonSkyProvider extends IRenderHandler {
    private static final ResourceLocation overworldTexture = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/uranus.png");
    private static final ResourceLocation sunTexture = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/planets/orbitalsun.png");

    public int starList;
    public int glSkyList;
    public int glSkyList2;
    private float sunSize;
    
    // Not currently functioning, wip

    public OberonSkyProvider(IGalacticraftWorldProvider ceresProvider) {
        this.sunSize = 17.5F * ceresProvider.getSolarSize();

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
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Vec3d skyColorVec = world.getSkyColor(mc.getRenderViewEntity(), partialTicks);
        float skyR = (float) skyColorVec.x;
        float skyG = (float) skyColorVec.y;
        float skyB = (float) skyColorVec.z;

        GL11.glColor3f(skyR, skyG, skyB);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glCallList(this.glSkyList);
        GL11.glDisable(GL11.GL_FOG);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();

        GL11.glPushMatrix();
        GL11.glRotatef(110.0F, 0.0F, 1.0F, 0.0F); 
        GL11.glRotatef(95.0F, 1.0F, 0.0F, 0.0F); 
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0); 
        
        renderThickCascadingDisc(tess, buffer);
        
        GL11.glPopMatrix();

        OpenGlHelper.glBlendFunc(770, 771, 1, 0); 
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        float celestialAngle = world.getCelestialAngle(partialTicks);
        GL11.glPushMatrix();
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);

        // Render Sun Aura
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        float starBright = world.getStarBrightness(partialTicks);
        float sunAlpha = 1.0F - starBright;
        
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 100.0D, 0.0D).color(1.0f, 0.8f, 0.6f, 0.3f * sunAlpha).endVertex();
        float auraSize = 25.0F;
        for (int i = 0; i <= 8; i++) {
            float a = (float) (i * Math.PI * 2.0 / 8.0);
            buffer.pos(MathHelper.cos(a) * auraSize, 100.0D, MathHelper.sin(a) * auraSize).color(1.0f, 0.7f, 0.4f, 0.0f).endVertex();
        }
        tess.draw();
        GL11.glShadeModel(GL11.GL_FLAT);

        // Render Sun Texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.renderEngine.bindTexture(sunTexture);
        float s = this.sunSize;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-s, 100.0D, -s).tex(0.0D, 0.0D).endVertex();
        buffer.pos(s, 100.0D, -s).tex(1.0D, 0.0D).endVertex();
        buffer.pos(s, 100.0D, s).tex(1.0D, 1.0D).endVertex();
        buffer.pos(-s, 100.0D, s).tex(0.0D, 1.0D).endVertex();
        tess.draw();
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-20.0F, 1.0F, 0.0F, 0.0F); 
        mc.renderEngine.bindTexture(overworldTexture);
        float uSize = 32.0F; 
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-uSize, 100.0D, -uSize).tex(0, 0).endVertex();
        buffer.pos(uSize, 100.0D, -uSize).tex(1, 0).endVertex();
        buffer.pos(uSize, 100.0D, uSize).tex(1, 1).endVertex();
        buffer.pos(-uSize, 100.0D, uSize).tex(0, 1).endVertex();
        tess.draw();
        GL11.glPopMatrix();

        if (starBright > 0.0F) {
            GL11.glPushMatrix();
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
            GL11.glColor4f(starBright, starBright, starBright, starBright);
            GL11.glCallList(this.starList);
            GL11.glPopMatrix();
        }

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void renderThickCascadingDisc(Tessellator tess, BufferBuilder buffer) {
        float radius = 95.0F; 
        float ringHeight = 10.0F; // 17% Thinner
        int segments = 120;
        long time = System.currentTimeMillis();
        Random rand = new Random(3333L);

        float r = 0.1f, g = 0.6f, b = 1.0f;
        float peakAlpha = 0.5f;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (i * Math.PI * 2.0 / segments);
            float a2 = (float) ((i + 1) * Math.PI * 2.0 / segments);

            float sideFade = MathHelper.sin((float)i / segments * (float)Math.PI);
            sideFade = MathHelper.clamp(sideFade, 0.0f, 1.0f);

            float x1 = MathHelper.cos(a1) * radius;
            float z1 = MathHelper.sin(a1) * radius;
            float x2 = MathHelper.cos(a2) * radius;
            float z2 = MathHelper.sin(a2) * radius;

            buffer.pos(x1, -ringHeight, z1).color(r, g, b, 0.0f).endVertex();
            buffer.pos(x2, -ringHeight, z2).color(r, g, b, 0.0f).endVertex();
            buffer.pos(x2, 0, z2).color(r, g, b, peakAlpha * sideFade).endVertex();
            buffer.pos(x1, 0, z1).color(r, g, b, peakAlpha * sideFade).endVertex();
            

            buffer.pos(x1, 0, z1).color(r, g, b, peakAlpha * sideFade).endVertex();
            buffer.pos(x2, 0, z2).color(r, g, b, peakAlpha * sideFade).endVertex();
            buffer.pos(x2, ringHeight, z2).color(r, g, b, 0.0f).endVertex();
            buffer.pos(x1, ringHeight, z1).color(r, g, b, 0.0f).endVertex();
        }
        tess.draw();

        float pSize = 0.35f; 
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 75; i++) {
            float angle = rand.nextFloat() * (float)Math.PI * 2.0f;
            float sideFade = MathHelper.sin(angle);
            sideFade = MathHelper.clamp(sideFade, 0.0f, 1.0f);

            float yOff = (rand.nextFloat() * ringHeight * 1.8F) - (ringHeight * 0.9F);
            float x = MathHelper.cos(angle) * (radius - 0.2f);
            float z = MathHelper.sin(angle) * (radius - 0.2f);

            float speed = 18000.0f;
            float cycle = ((time + (i * 700)) % (long)speed) / speed;
            
            float brightness = 0.15f; 
            if (cycle < 0.15f) brightness += (cycle / 0.15f) * 0.85f;
            else if (cycle < 0.3f) brightness += (1.0f - ((cycle - 0.15f) / 0.15f)) * 0.85f;

            float finalAlpha = brightness * sideFade;

            buffer.pos(x - pSize, yOff - pSize, z).color(0.5f, 0.8f, 1.0f, finalAlpha).endVertex();
            buffer.pos(x + pSize, yOff - pSize, z).color(0.5f, 0.8f, 1.0f, finalAlpha).endVertex();
            buffer.pos(x + pSize, yOff + pSize, z).color(0.5f, 0.8f, 1.0f, finalAlpha).endVertex();
            buffer.pos(x - pSize, yOff + pSize, z).color(0.5f, 0.8f, 1.0f, finalAlpha).endVertex();
        }
        tess.draw();
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
            final double var10 = 0.15F + rand.nextFloat() * 0.1F;
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

        return var3 * var3 * 1F;
    }
}