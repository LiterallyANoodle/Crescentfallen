package fracture.mod.objects.blocks;

import fracture.mod.CFMain;
import fracture.mod.init.BlockInit;
import fracture.mod.init.ItemInit;
import fracture.mod.objects.blocks.item.ItemBlockVariants;
import fracture.mod.util.IHasModel;
import fracture.mod.util.handlers.BlockVariant;
import fracture.mod.util.interfaces.IMetaName;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlockOres extends Block implements IHasModel, IMetaName {
	public static final PropertyEnum<BlockVariant.BlockVariantEnum> VARIANT = PropertyEnum.<BlockVariant.BlockVariantEnum>create(
			"varient", BlockVariant.BlockVariantEnum.class);

	private String name, dimension;

	public BlockOres(String name, String dimension) {
		super(Material.ROCK);
		setTranslationKey(name);
		setRegistryName(name);
		setCreativeTab(fracture.mod.CFMain.CrescentfallenBlocks);
		setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockVariant.BlockVariantEnum.copper));

		this.name = name;
		this.dimension = dimension;

		BlockInit.BLOCKS.add(this);
		ItemInit.ITEMS.add(new ItemBlockVariants(this).setRegistryName(this.getRegistryName()));
	}

	@Override
	public int damageDropped(IBlockState state) {
		return ((BlockVariant.BlockVariantEnum) state.getValue(VARIANT)).getMeta();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((BlockVariant.BlockVariantEnum) state.getValue(VARIANT)).getMeta();
	}

	@Override
	public IBlockState getStateFromMeta(int variantId) {
		return this.getDefaultState().withProperty(VARIANT, BlockVariant.BlockVariantEnum.byMetadata(variantId));
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
			EntityPlayer player) {

		return new ItemStack(Item.getItemFromBlock(this), 1, getMetaFromState(world.getBlockState(pos)));
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		for (BlockVariant.BlockVariantEnum variant : BlockVariant.BlockVariantEnum.values()) {
			items.add(new ItemStack(this, 1, variant.getMeta()));
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { VARIANT });
	}

	public String getSpecialName(ItemStack stack) {
		return BlockVariant.BlockVariantEnum.values()[stack.getItemDamage()].getName();
	}

	@Override
	public void registerModels() {
		for (int i = 0; i < BlockVariant.BlockVariantEnum.values().length; i++) {
			CFMain.proxy.registerVariantRenderer(Item.getItemFromBlock(this), i,
					"ore_" + this.dimension + "_" + BlockVariant.BlockVariantEnum.values()[i].getName(), "inventory");
		}
	//Everything inside of the for loop is ran for the number of different variants that are present.
		
	}
}
