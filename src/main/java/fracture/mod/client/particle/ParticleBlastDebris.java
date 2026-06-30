package fracture.mod.client.particle;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBlastDebris extends ParticleDigging {

    public ParticleBlastDebris(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeed, double ySpeed, double zSpeed, IBlockState state) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeed, ySpeed, zSpeed, state);
        
        this.setBlockPos(new BlockPos(xCoordIn, yCoordIn, zCoordIn));
        
        // Apply velocity
        this.motionX = xSpeed;
        this.motionY = ySpeed;
        this.motionZ = zSpeed;
        this.particleGravity = 1.5F; 
        this.particleScale = 1.5F + this.rand.nextFloat() * 2.5F; 
        this.particleMaxAge = 40 + this.rand.nextInt(40);
        this.canCollide = true;
    }
}