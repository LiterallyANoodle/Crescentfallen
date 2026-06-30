package fracture.mod.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleKonaFilth extends Particle {

    public ParticleKonaFilth(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
        this.setRBGColorF(0.33F, 0.42F, 0.18F); 
        this.setParticleTextureIndex(82);
        this.particleScale *= 1.5F; 
        this.particleMaxAge = (int)(Math.random() * 40.0D) + 40;
        this.particleGravity = 0.0F; 
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        
        this.motionX += (this.rand.nextDouble() - 0.5D) * 0.002D;
        this.motionY += (this.rand.nextDouble() - 0.5D) * 0.002D;
        this.motionZ += (this.rand.nextDouble() - 0.5D) * 0.002D;
    }
}