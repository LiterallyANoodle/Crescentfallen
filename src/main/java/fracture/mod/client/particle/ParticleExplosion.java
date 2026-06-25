package fracture.mod.client.particle;

import fracture.mod.CFInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleExplosion extends Particle {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CFInfo.ID, "textures/particle/explosion.png");
    private final int totalFrames = 9;

    public ParticleExplosion(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.particleMaxAge = 36; 
        this.particleScale = 3.0F; 
        this.canCollide = false; 
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        int currentFrame = (int) (((float) this.particleAge / this.particleMaxAge) * totalFrames);
        if (currentFrame >= totalFrames) {
            currentFrame = totalFrames - 1; // Cap it at the last frame
        }

        float minU = 0.0F;
        float maxU = 1.0F;
        float minV = (float) currentFrame / totalFrames;
        float maxV = (float) (currentFrame + 1) / totalFrames;

        float scale = this.particleScale;
        
        float x = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float y = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float z = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);
        
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;

        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos((double)(x - rotationX * scale - rotationXY * scale), (double)(y - rotationZ * scale), (double)(z - rotationYZ * scale - rotationXZ * scale))
              .tex((double)maxU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)(x - rotationX * scale + rotationXY * scale), (double)(y + rotationZ * scale), (double)(z - rotationYZ * scale + rotationXZ * scale))
              .tex((double)maxU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)(x + rotationX * scale + rotationXY * scale), (double)(y + rotationZ * scale), (double)(z + rotationYZ * scale + rotationXZ * scale))
              .tex((double)minU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)(x + rotationX * scale - rotationXY * scale), (double)(y - rotationZ * scale), (double)(z + rotationYZ * scale - rotationXZ * scale))
              .tex((double)minU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }
}