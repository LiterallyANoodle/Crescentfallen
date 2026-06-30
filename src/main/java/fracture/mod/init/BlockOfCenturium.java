package fracture.mod.init;

import net.minecraft.block.Block;
import fracture.mod.CFMain;
import fracture.mod.client.particle.ParticleGeyserTest;
import fracture.mod.util.IHasModel;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;

import java.util.Random;

public class BlockOfCenturium extends Block implements IHasModel {
	
    public BlockOfCenturium(String name, Material material) {
        super(material);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(CFMain.CrescentfallenBlocks);
        setHardness(50.1f);
        setResistance(1200.0f);
        setLightLevel(0.0f);
        setLightOpacity(0);
        setHarvestLevel("pickaxe", 3);
        
        setSoundType(net.minecraft.block.SoundType.METAL); 
        
        BlockInit.BLOCKS.add(this);
        ItemInit.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
    }
	
    @Override
    public void registerModels() {
        CFMain.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory");
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double spawnX = (double)pos.getX() + 0.5D; 
        double spawnY = (double)pos.getY() + 1.0D; 
        double spawnZ = (double)pos.getZ() + 0.5D; 


        int pillarCount = 5 + rand.nextInt(6);
        for (int i = 0; i < pillarCount; i++) {
            double offsetX = rand.nextDouble() - 0.5D;
            double offsetZ = rand.nextDouble() - 0.5D;
            double motionY = 0.35D + (rand.nextDouble() * 0.2D);

            ParticleGeyserTest pillarParticle = new ParticleGeyserTest(
                worldIn, 
                spawnX + offsetX, spawnY, spawnZ + offsetZ, 
                0.0D, motionY, 0.0D, 
                false
            );
            Minecraft.getMinecraft().effectRenderer.addEffect(pillarParticle);
        }

        if (rand.nextFloat() < 0.45F) {
            int spurtCount = 1 + rand.nextInt(3);
            for (int k = 0; k < spurtCount; k++) {
                double offsetX = rand.nextDouble() - 0.5D;
                double offsetZ = rand.nextDouble() - 0.5D;
                
                double motionX = (rand.nextDouble() * 0.3D) - 0.15D;
                double motionY = 0.15D + (rand.nextDouble() * 0.2D);
                double motionZ = (rand.nextDouble() * 0.3D) - 0.15D;

                ParticleGeyserTest spurtParticle = new ParticleGeyserTest(
                    worldIn, 
                    spawnX + offsetX, spawnY, spawnZ + offsetZ, 
                    motionX, motionY, motionZ, 
                    true
                );
                Minecraft.getMinecraft().effectRenderer.addEffect(spurtParticle);
            }
        }
    }
    }