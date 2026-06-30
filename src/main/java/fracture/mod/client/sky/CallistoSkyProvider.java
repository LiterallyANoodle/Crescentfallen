package fracture.mod.client.sky;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mjr.extraplanets.Constants;
import com.mjr.mjrlegendslib.util.MCUtilities;

import fracture.mod.CFInfo;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;

public class CallistoSkyProvider extends IRenderHandler {
	private static final ResourceLocation overworldTexture = new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/jupiter.png");
	private static final ResourceLocation sunTexture = new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/orbitalsun.png");
	
	private static final ResourceLocation ioTexture = new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/io.png");
	private static final ResourceLocation europaTexture = new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/europa.png");
	//Why does this not work???? They are all drawing from the same resource location
	private static final ResourceLocation ganymedeTexture = new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/ganymede.png");

	
	public int starList;
	public int starList2;
	public int glSkyList;
	public int glSkyList2;
	private float sunSize;

	public CallistoSkyProvider(IGalacticraftWorldProvider callistoProvider) {
		this.sunSize = 17.5F * callistoProvider.getSolarSize();

		int displayLists = GLAllocation.generateDisplayLists(4);
		this.starList   = displayLists;
		this.starList2  = displayLists + 1;
		this.glSkyList  = displayLists + 2;
		this.glSkyList2 = displayLists + 3;

		this.compileStarList(this.starList, 10842L, 1.0F, 12000);
		this.compileStarList(this.starList2, 77777L, 0.70F, 15000);

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

		float nightFactor = world.getStarBrightness(partialTicks);
		float dayFactor = 1.0F - nightFactor;

		Vec3d vec3 = world.getSkyColor(mc.getRenderViewEntity(), partialTicks);
		float f1 = (float) vec3.x;
		float f2 = (float) vec3.y;
		float f3 = (float) vec3.z;
		float f6;

		f1 *= (0.05F + dayFactor * 0.95F);
		f2 *= (0.05F + dayFactor * 0.95F);
		f3 *= (0.05F + dayFactor * 0.95F);
		
		//retint sky color
		f1 = Math.min(1.0F, f1 * 1.05F);
		f2 *= 0.80F;
		f3 *= 0.72F;

		if (mc.gameSettings.anaglyph) {
			float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
			float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
			f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
			f1 = f4;
			f2 = f5;
			f3 = f6;
		}

		GL11.glColor3f(f1, f2, f3);
		Tessellator tessellator1 = Tessellator.getInstance();
		BufferBuilder worldRenderer1 = tessellator1.getBuffer();
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glPushMatrix();
		GL11.glScalef(10.0F, 10.0F, 10.0F);
		GL11.glCallList(this.glSkyList);
		GL11.glPopMatrix();
		
		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		RenderHelper.disableStandardItemLighting();
		float f7;
		float f8;
		float f9;
		float f10;

		float celestialAngle = world.getCelestialAngle(partialTicks);
		float f18 = nightFactor; 

		{
			float layer1Alpha = Math.max(0.10F, f18);
			GL11.glPushMatrix();
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-19.0F, 0.0F, 1.0F, 0.0F);
			GL11.glColor4f(layer1Alpha, layer1Alpha, layer1Alpha, layer1Alpha);
			GL11.glCallList(this.starList);
			GL11.glPopMatrix();

			float layer2Alpha = Math.max(0.06F, f18 * 0.72F);
			GL11.glPushMatrix();
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-28.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(  6.0F, 1.0F, 0.0F, 0.0F);
			GL11.glColor4f(layer2Alpha, layer2Alpha, layer2Alpha, layer2Alpha);
			GL11.glCallList(this.starList2);
			GL11.glPopMatrix();
		}

		float[] afloat = new float[4];
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glPushMatrix();
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
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

		float f18_inv = 1.0F - f18;

		worldRenderer1.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
		float r = f6 * f18_inv;
		float g = f7 * f18_inv;
		float b = f8 * f18_inv;
		float a = afloat[3] * 2 / f18_inv;
		worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, a).endVertex();
		r = afloat[0] * f18_inv;
		g = afloat[1] * f18_inv;
		b = afloat[2] * f18_inv;
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
		worldRenderer1.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
		r = f6 * f18_inv;
		g = f7 * f18_inv;
		b = f8 * f18_inv;
		a = afloat[3] * f18_inv;
		worldRenderer1.pos(0.0D, 100.0D, 0.0D).color(r, g, b, a).endVertex();
		r = afloat[0] * f18_inv;
		g = afloat[1] * f18_inv;
		b = afloat[2] * f18_inv;
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

		renderWesternAtmosphere(tessellator1, dayFactor);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glPushMatrix();
		f7 = 0.0F;
		f8 = 0.0F;
		f9 = 0.0F;
		GL11.glTranslatef(f7, f8, f9);
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);

//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
//		f10 = this.sunSize / 3.5F;
//		worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
//		worldRenderer1.pos(-f10, 99.9D, -f10).endVertex();
//		worldRenderer1.pos(f10, 99.9D, -f10).endVertex();
//		worldRenderer1.pos(f10, 99.9D, f10).endVertex();
//		worldRenderer1.pos(-f10, 99.9D, f10).endVertex();
//		tessellator1.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		f10 = this.sunSize;
		mc.renderEngine.bindTexture(CallistoSkyProvider.sunTexture);
		worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldRenderer1.pos(-f10, 100.0D, -f10).tex(0.0D, 0.0D).endVertex();
		worldRenderer1.pos(f10, 100.0D, -f10).tex(1.0D, 0.0D).endVertex();
		worldRenderer1.pos(f10, 100.0D, f10).tex(1.0D, 1.0D).endVertex();
		worldRenderer1.pos(-f10, 100.0D, f10).tex(0.0D, 1.0D).endVertex();
		tessellator1.draw();
		GL11.glPopMatrix();

		this.renderMoons(tessellator1, celestialAngle);

		GL11.glPushMatrix();
		f10 = (sunSize * 1.5F) * 0.45F;
		GL11.glScalef(0.6F, 0.6F, 0.6F);
		GL11.glRotatef(40.0F, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(200F, 1.0F, 0.0F, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
		MCUtilities.getClient().renderEngine.bindTexture(CallistoSkyProvider.overworldTexture);
		worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldRenderer1.pos(-f10, -100.0D, f10).tex(0, 1).endVertex();
		worldRenderer1.pos(f10, -100.0D, f10).tex(1, 1).endVertex();
		worldRenderer1.pos(f10, -100.0D, -f10).tex(1, 0).endVertex();
		worldRenderer1.pos(-f10, -100.0D, -f10).tex(0, 0).endVertex();
		tessellator1.draw();
		GL11.glPopMatrix();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		float bottomR = 1.0F * dayFactor;
		float bottomG = 0.4F * dayFactor;
		float bottomB = 0.0F * dayFactor;

		GL11.glColor3f(bottomR, bottomG, bottomB);
		double d0 = mc.player.getPosition().getY() - world.getHorizon();

		//WIP remove this
		if (d0 < 0.0D) {
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0F, 12.0F, 0.0F);
			GL11.glCallList(this.glSkyList2);
			GL11.glPopMatrix();
			
			f8 = 512.0F; 
			f9 = -((float) (d0 + 65.0D));
			f10 = -f8;
			
			worldRenderer1.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			worldRenderer1.pos(-f8, f9, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f9, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f10, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(-f8, f10, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(-f8, f10, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f10, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f9, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(-f8, f9, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f10, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f10, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f9, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f9, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(-f8, f9, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(-f8, f9, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(-f8, f10, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(-f8, f10, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(-f8, f10, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(-f8, f10, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f10, f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			worldRenderer1.pos(f8, f10, -f8).color(bottomR, bottomG, bottomB, 1.0F).endVertex();
			tessellator1.draw();
		}

		GL11.glColor3f(bottomR, bottomG, bottomB);

		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, -((float)(d0 - 16.0D)), 0.0F);
		GL11.glCallList(this.glSkyList2);
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glDepthMask(true);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void renderWesternAtmosphere(Tessellator tessellator, float dayFactor) {
		if (dayFactor <= 0.01F) return;
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

			buffer.pos(nextVx, 30.0F, nextVz).color(1.0F, 0.7F, 0.2F, 0.0F).endVertex();
			buffer.pos(vx, 30.0F, vz).color(1.0F, 0.7F, 0.2F, 0.0F).endVertex();
			buffer.pos(vx, -16.0F, vz).color(1.0F, 0.4F, 0.0F, 0.8F * dayFactor).endVertex();
			buffer.pos(nextVx, -16.0F, nextVz).color(1.0F, 0.4F, 0.0F, 0.8F * dayFactor).endVertex();

			//NOTE: remove this
			buffer.pos(nextVx, -16.0F, nextVz).color(1.0F, 0.4F, 0.0F, 0.8F * dayFactor).endVertex();
			buffer.pos(vx, -16.0F, vz).color(1.0F, 0.4F, 0.0F, 0.8F * dayFactor).endVertex();
			buffer.pos(vx, -200.0F, vz).color(1.0F, 0.4F, 0.0F, 0.8F * dayFactor).endVertex();
			buffer.pos(nextVx, -200.0F, nextVz).color(1.0F, 0.4F, 0.0F, 0.8F * dayFactor).endVertex();
		}
		tessellator.draw();

		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

	private void renderMoons(Tessellator tessellator, float celestialAngle) {
		BufferBuilder worldRenderer = tessellator.getBuffer();
		float size;

		GL11.glPushMatrix();
		size = 5.5F; 
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(celestialAngle * 360.0F + 60.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(12.0F, 0.0F, 0.0F, 1.0F);                         
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);                         
		Minecraft.getMinecraft().renderEngine.bindTexture(ioTexture);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldRenderer.pos(-size, 100.0D, -size).tex(0.0D, 0.0D).endVertex();
		worldRenderer.pos(size, 100.0D, -size).tex(1.0D, 0.0D).endVertex();
		worldRenderer.pos(size, 100.0D, size).tex(1.0D, 1.0D).endVertex();
		worldRenderer.pos(-size, 100.0D, size).tex(0.0D, 1.0D).endVertex();
		tessellator.draw();
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		size = 4.0F;
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(celestialAngle * 360.0F + 150.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(-8.0F, 0.0F, 0.0F, 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(europaTexture);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldRenderer.pos(-size, 100.0D, -size).tex(0.0D, 0.0D).endVertex();
		worldRenderer.pos(size, 100.0D, -size).tex(1.0D, 0.0D).endVertex();
		worldRenderer.pos(size, 100.0D, size).tex(1.0D, 1.0D).endVertex();
		worldRenderer.pos(-size, 100.0D, size).tex(0.0D, 1.0D).endVertex();
		tessellator.draw();
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		size = 7.0F;
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(celestialAngle * 360.0F + 280.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(ganymedeTexture);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldRenderer.pos(-size, 100.0D, -size).tex(0.0D, 0.0D).endVertex();
		worldRenderer.pos(size, 100.0D, -size).tex(1.0D, 0.0D).endVertex();
		worldRenderer.pos(size, 100.0D, size).tex(1.0D, 1.0D).endVertex();
		worldRenderer.pos(-size, 100.0D, size).tex(0.0D, 1.0D).endVertex();
		tessellator.draw();
		GL11.glPopMatrix();
	}

	private void compileStarList(int displayList, long seed, float sizeScale, int starCount) {
		final Random rand = new Random(seed);
		final Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		GL11.glPushMatrix();
		GL11.glNewList(displayList, GL11.GL_COMPILE);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

		for (int i = 0; i < starCount; ++i) {
			double dirX = rand.nextFloat() * 2.0F - 1.0F;
			double dirY = rand.nextFloat() * 2.0F - 1.0F;
			double dirZ = rand.nextFloat() * 2.0F - 1.0F;
			
			final double baseSize = (0.15F + rand.nextFloat() * 0.1F) * sizeScale;
			double distance = dirX * dirX + dirY * dirY + dirZ * dirZ;

			if (distance < 1.0D && distance > 0.01D) {
				distance = 1.0D / Math.sqrt(distance);
				dirX *= distance;
				dirY *= distance;
				dirZ *= distance;

				final double distanceScale = rand.nextDouble() * 150D + 130D;
				final double posX = dirX * distanceScale;
				final double posY = dirY * distanceScale;
				final double posZ = dirZ * distanceScale;

				final double polar = Math.atan2(dirX, dirZ);
				final double sinPolar = Math.sin(polar);
				final double cosPolar = Math.cos(polar);

				final double azimuth = Math.atan2(Math.sqrt(dirX * dirX + dirZ * dirZ), dirY);
				final double sinAzimuth = Math.sin(azimuth);
				final double cosAzimuth = Math.cos(azimuth);

				final double roll = rand.nextDouble() * Math.PI * 2.0D;
				final double sinRoll = Math.sin(roll);
				final double cosRoll = Math.cos(roll);

				for (int v = 0; v < 4; ++v) {
					final double xOffset = ((v & 2) - 1) * baseSize;
					final double yOffset = ((v + 1 & 2) - 1) * baseSize;
					
					final double rotX = xOffset * cosRoll - yOffset * sinRoll;
					final double rotY = yOffset * cosRoll + xOffset * sinRoll;
					
					final double axX = cosAzimuth * sinPolar;
					final double axY = -sinAzimuth;
					final double axZ = cosAzimuth * cosPolar;
					final double ayX = cosPolar;
					final double ayY = 0.0;
					final double ayZ = -sinPolar;

					final double translatedX = rotX * axX + rotY * ayX;
					final double translatedY = rotX * axY + rotY * ayY;
					final double translatedZ = rotX * axZ + rotY * ayZ;

					buffer.pos(posX + translatedX, posY + translatedY, posZ + translatedZ).endVertex();
				}
			}
		}

		tessellator.draw();
		GL11.glEndList();
		GL11.glPopMatrix();
	}
}