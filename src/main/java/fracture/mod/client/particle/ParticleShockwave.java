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
public class ParticleShockwave extends Particle {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CFInfo.ID, "textures/particle/shockwave.png");
    
    private final float maxExpansionScale = 15.0F; 

    public ParticleShockwave(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.particleMaxAge = 15; 
        this.particleScale = 1.0F; 
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
        float ageWithPartial = (float)this.particleAge + partialTicks;
        float agePercent = ageWithPartial / (float)this.particleMaxAge;
        if (agePercent > 1.0F) agePercent = 1.0F;

        float currentScale = this.particleScale + (agePercent * maxExpansionScale);
        float currentAlpha = 1.0F - agePercent; // Fades from 1.0 down to 0.0

        float minU = 0.0F;
        float maxU = 1.0F;
        float minV = 0.0F;
        float maxV = 1.0F;
        
        float x = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float y = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float z = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);
        
        GlStateManager.disableCull(); 
        
        int i = 15728880; 
        int j = i >> 16 & 65535;
        int k = i & 65535;

        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        
        buffer.pos((double)(x - currentScale), (double)y, (double)(z - currentScale)).tex((double)minU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, currentAlpha).lightmap(j, k).endVertex();
        
        buffer.pos((double)(x - currentScale), (double)y, (double)(z + currentScale)).tex((double)minU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, currentAlpha).lightmap(j, k).endVertex();
        
        buffer.pos((double)(x + currentScale), (double)y, (double)(z + currentScale)).tex((double)maxU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, currentAlpha).lightmap(j, k).endVertex();
        
        buffer.pos((double)(x + currentScale), (double)y, (double)(z - currentScale)).tex((double)maxU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, currentAlpha).lightmap(j, k).endVertex();
              
        Tessellator.getInstance().draw();
        GlStateManager.enableCull(); 
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }
}