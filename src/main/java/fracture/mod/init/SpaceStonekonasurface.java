package fracture.mod.init;

import java.util.Random;

import fracture.mod.CFMain;
import fracture.mod.init.BlockInit;
import fracture.mod.init.ItemInit;
import fracture.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpaceStonekonasurface extends Block implements IHasModel {

    public static final PropertyBool TRASHY = PropertyBool.create("trashy");

    public SpaceStonekonasurface(String name, Material material) {
        super(material);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(CFMain.CrescentfallenBlocks);
        setHardness(1.7f);
        setResistance(6.0f);
        setLightLevel(0.0f);
        setLightOpacity(2222);
        setHarvestLevel("pickaxe", 0);
        setSoundType(SoundType.STONE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TRASHY, false));
        
        BlockInit.BLOCKS.add(this);
        ItemInit.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {TRASHY});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TRASHY, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TRASHY) ? 1 : 0;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0; 
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            if (!state.getValue(TRASHY)) {
                if (worldIn.getBlockState(pos.up()).getBlock() == BlockInit.TRASH_PILE) {
                    worldIn.setBlockState(pos, state.withProperty(TRASHY, true), 3);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        if (state.getValue(TRASHY) && random.nextInt(10) == 0) {
            double x = (double)pos.getX() + random.nextDouble();
            double y = (double)pos.getY() + 1.05D; 
            double z = (double)pos.getZ() + random.nextDouble();

            world.spawnParticle(EnumParticleTypes.TOWN_AURA, x, y, z, 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    public void registerModels() {
        CFMain.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory"); 
    }
}