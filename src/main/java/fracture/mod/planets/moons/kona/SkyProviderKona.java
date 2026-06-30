package fracture.mod.planets.moons.kona;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import fracture.mod.CFInfo;
import fracture.mod.planets.moons.kona.biome.KonaBiomes;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
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
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.IRenderHandler;

public class SkyProviderKona extends IRenderHandler {
	
	private static final ResourceLocation sunTexture = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/triopas.png");
	private static final ResourceLocation hollowsTexture = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/hollows.png"); 

	public int starList;
	public int glSkyList;
	private float sunSize;
	
	public SkyProviderKona(IGalacticraftWorldProvider konaProvider) {
		this.sunSize = 6.5F * konaProvider.getSolarSize() * 7;

		int displayLists = GLAllocation.generateDisplayLists(2);
		this.starList = displayLists;
		this.glSkyList = displayLists + 1;

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
	}

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);

		float dayTime = world.getCelestialAngle(1.0F);
		float dayFactor = Math.max(0.0F, MathHelper.cos(dayTime * (float)Math.PI * 2.0F));
		
		Entity renderEntity = mc.getRenderViewEntity() != null ? mc.getRenderViewEntity() : mc.player;
		Biome biome = world.getBiome(renderEntity.getPosition());

		float nightR = 0.01F;
		float nightG = 0.01F;
		float nightB = 0.03F;

		float dayR, dayG, dayB;

		if (biome == KonaBiomes.BiomeKonaTrashHeap) {
			dayR = 0.12F;
			dayG = 0.15F;
			dayB = 0.08F;
		} else {
			dayR = 0.02F;
			dayG = 0.03F;
			dayB = 0.06F;
		}

		float skyR = nightR + ((dayR - nightR) * dayFactor);
		float skyG = nightG + ((dayG - nightG) * dayFactor);
		float skyB = nightB + ((dayB - nightB) * dayFactor);

		GL11.glColor3f(skyR, skyG, skyB);
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

		float starBrightness = Math.max(0.35F, world.getStarBrightness(partialTicks));
		GL11.glPushMatrix();
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(-19.0F, 0, 1.0F, 0);
		GL11.glColor4f(starBrightness, starBrightness, starBrightness, starBrightness);
		GL11.glCallList(this.starList);
		GL11.glPopMatrix();

		float[] afloat = {0.8F, 0.1F, 0.1F, 0.4F}; 
		float f18 = 1.0F - world.getStarBrightness(partialTicks);
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glPushMatrix();
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);

		worldRenderer1.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
		float r = afloat[0] * f18;
		float g = afloat[1] * f18;
		float b = afloat[2] * f18;
		float a = afloat[3] * 2 / f18;
		worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, a).endVertex();
		a = 0.0F;

		float f10 = 32.0F;
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

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

		GL11.glPushMatrix();
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(sunTexture);
		worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldRenderer1.pos(-this.sunSize, 100.0D, -this.sunSize).tex(0.0D, 0.0D).endVertex();
		worldRenderer1.pos(this.sunSize, 100.0D, -this.sunSize).tex(1.0D, 0.0D).endVertex();
		worldRenderer1.pos(this.sunSize, 100.0D, this.sunSize).tex(1.0D, 1.0D).endVertex();
		worldRenderer1.pos(-this.sunSize, 100.0D, this.sunSize).tex(0.0D, 1.0D).endVertex();
		tessellator1.draw();
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(35.0F, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(150.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
		
		float fracSize = 40.0F; 
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F); 
		mc.renderEngine.bindTexture(hollowsTexture); 
		worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldRenderer1.pos(-fracSize, 100.0D, -fracSize).tex(0.0D, 0.0D).endVertex();
		worldRenderer1.pos(fracSize, 100.0D, -fracSize).tex(1.0D, 0.0D).endVertex();
		worldRenderer1.pos(fracSize, 100.0D, fracSize).tex(1.0D, 1.0D).endVertex();
		worldRenderer1.pos(-fracSize, 100.0D, fracSize).tex(0.0D, 1.0D).endVertex();
		tessellator1.draw();
		GL11.glPopMatrix();

		GlStateManager.enableRescaleNormal();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
	}	
	
	private void renderStars() {
		final Random rand = new Random(10842L);
		final Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		for (int i = 0; i < 40000; ++i) {
			double x = rand.nextFloat() * 2.0F - 1.0F;
			double y = rand.nextFloat() * 2.0F - 1.0F;
			double z = rand.nextFloat() * 2.0F - 1.0F;
			final double size = 0.12F + rand.nextFloat() * 0.15F;
			double mag = x * x + y * y + z * z;

			if (mag < 1.0D && mag > 0.01D) {
				mag = 1.0D / Math.sqrt(mag);
				x *= mag; y *= mag; z *= mag;

				float temp = rand.nextFloat();
				float r, g, b;

				if (temp < 0.33F) {
					r = 0.85F; g = 0.75F; b = 0.75F;
				} else if (temp < 0.66F) {
					r = 0.85F; g = 0.85F; b = 0.75F;
				} else {
					r = 0.75F; g = 0.85F; b = 0.95F;
				}

				final double px = x * 150D;
				final double py = y * 150D;
				final double pz = z * 150D;

				float alpha = 0.3F + rand.nextFloat() * 0.4F;

				final double azimuth = Math.atan2(x, z);
				final double sinAz = Math.sin(azimuth);
				final double cosAz = Math.cos(azimuth);
				final double inclination = Math.atan2(Math.sqrt(x * x + z * z), y);
				final double sinIncl = Math.sin(inclination);
				final double cosIncl = Math.cos(inclination);
				final double angle = rand.nextDouble() * Math.PI * 2.0D;
				final double sinAng = Math.sin(angle);
				final double cosAng = Math.cos(angle);

				for (int j = 0; j < 4; ++j) {
					final double dx = ((j & 2) - 1) * size;
					final double dz = ((j + 1 & 2) - 1) * size;
					final double vx = dx * cosAng - dz * sinAng;
					final double vz = dz * cosAng + dx * sinAng;
					final double vy = vx * sinIncl;
					final double rx = -vx * cosIncl;
					final double fx = rx * sinAz - vz * cosAz;
					final double fz = vz * sinAz + rx * cosAz;
					buffer.pos(px + fx, py + vy, pz + fz).color(r, g, b, alpha).endVertex();
				}
			}
		}
		tessellator.draw();
	}
}