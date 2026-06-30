package fracture.mod.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import fracture.mod.CFInfo;

public class ParticleGeyserTest extends Particle {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CFInfo.ID, "textures/particle/geyser_particle.png");
    
    private final boolean isSpurt;
    private final float initScale;

    public ParticleGeyserTest(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motionXIn, double motionYIn, double motionZIn, boolean isSpurt) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        
        this.isSpurt = isSpurt;
        this.motionX = motionXIn;
        this.motionY = motionYIn;
        this.motionZ = motionZIn;

        if (this.isSpurt) {
            this.particleMaxAge = 10 + this.rand.nextInt(15); 
            this.particleScale = 0.5F + this.rand.nextFloat() * 0.5F;
        } else {
            this.particleMaxAge = 40 + this.rand.nextInt(20); 
            this.particleScale = 1.5F + this.rand.nextFloat() * 1.0F;
        }
        
        this.initScale = this.particleScale;
        this.particleGravity = 0.0F; 
        this.particleAlpha = 1.0F;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
            return;
        }

        this.move(this.motionX, this.motionY, this.motionZ);

        if (this.isSpurt) {
            this.motionY -= 0.04D;
            this.motionX *= 0.92D;
            this.motionZ *= 0.92D;
        } else {
            this.motionY *= 0.96D;

            float threshold = this.particleMaxAge * 0.8F;
            
            if (this.particleAge > threshold) {
                float lifeRemaining = this.particleMaxAge - threshold;
                float dashProgress = (this.particleAge - threshold) / lifeRemaining;
                
                this.particleAlpha = 1.0F - dashProgress;

                double windDashX = 0.12D; 
                double windDashZ = -0.08D; 
                
                this.motionX += windDashX * dashProgress;
                this.motionZ += windDashZ * dashProgress;
                
                this.particleScale = this.initScale * (1.0F + dashProgress * 1.5F);
            }
        }
    }


    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);

        GL11.glPushMatrix();
        GL11.glDepthMask(false); 
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);

        float uMin = 0.0F;
        float uMax = 1.0F;
        float vMin = 0.0F;
        float vMax = 1.0F;

        float renderScale = this.particleScale * 0.1F;

        float renderX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float renderY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float renderZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        
        buffer.pos((double)(renderX - rotationX * renderScale - rotationXY * renderScale), (double)(renderY - rotationZ * renderScale), (double)(renderZ - rotationYZ * renderScale - rotationXZ * renderScale)).tex((double)uMax, (double)vMax).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(0, 240).endVertex();
        buffer.pos((double)(renderX - rotationX * renderScale + rotationXY * renderScale), (double)(renderY + rotationZ * renderScale), (double)(renderZ - rotationYZ * renderScale + rotationXZ * renderScale)).tex((double)uMax, (double)vMin).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(0, 240).endVertex();
        buffer.pos((double)(renderX + rotationX * renderScale + rotationXY * renderScale), (double)(renderY + rotationZ * renderScale), (double)(renderZ + rotationYZ * renderScale + rotationXZ * renderScale)).tex((double)uMin, (double)vMin).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(0, 240).endVertex();
        buffer.pos((double)(renderX + rotationX * renderScale - rotationXY * renderScale), (double)(renderY - rotationZ * renderScale), (double)(renderZ + rotationYZ * renderScale - rotationXZ * renderScale)).tex((double)uMin, (double)vMax).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(0, 240).endVertex();
        
        Tessellator.getInstance().draw();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }
}