package fracture.mod.init;

import net.minecraft.block.Block;

import java.util.Random;

import fracture.mod.CFMain;
import fracture.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpaceGrass extends Block implements IHasModel 
{
	
	public SpaceGrass(String name, Material material) 
	{

		super(Material.GRASS);
		setTranslationKey(name);
		setRegistryName(name);
		setCreativeTab(CFMain.CrescentfallenBlocks);
		setHardness(0.6f);
		setResistance(0.6f);
		setLightLevel(0.0f);
		setLightOpacity(0);
		//setDefaultSlipperiness(slipperiness);
		setHarvestLevel("shovel", 0);
		setSoundType(blockSoundType.PLANT);
		//setBlockUnbreakable();
		
		BlockInit.BLOCKS.add(this);
		ItemInit.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
	}
	
	@Override
	public void registerModels() {
		CFMain.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory"); {}
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
	net.minecraftforge.common.IPlantable plantable) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
	    super.randomDisplayTick(state, world, pos, random);

	    if (random.nextInt(10) == 0) {
	        
	        double x = (double)pos.getX() + random.nextDouble();
	        double y = (double)pos.getY() + 1.1D; 
	        double z = (double)pos.getZ() + random.nextDouble();

	        world.spawnParticle(EnumParticleTypes.TOWN_AURA, x, y, z, 0.0D, 0.0D, 0.0D);
	    }
	}
}
	
