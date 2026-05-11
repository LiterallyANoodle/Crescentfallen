package fracture.mod.world.epchanges;

import java.util.Random;
import fracture.mod.world.epchanges.callisto.CFEPBiomeInit; 
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CfCallistoChunkgen {

    private static final int CALLISTO_DIM_ID = -1505;

    private IBlockState oilSandState;
    private IBlockState whiteRockState;
    private IBlockState darkRockState;
    private IBlockState ironOreState;
    private IBlockState coalOreState;
    private IBlockState goldOreState;

    private boolean initialized = false;
    private final Random rand = new Random();

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != CALLISTO_DIM_ID)
            return;

        if (!initialized) initBlocks();

        Chunk chunk = event.getChunk();
        boolean modified = false;

        int anchorX = chunk.x * 16;
        int anchorZ = chunk.z * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = anchorX + x;
                int worldZ = anchorZ + z;
                
                BlockPos stablePos = new BlockPos(worldX, 0, worldZ);
                Biome biome = event.getWorld().getBiome(stablePos);
                
                int distToLow = getDistanceToLowBiome(event.getWorld(), worldX, worldZ, 8);
                if (distToLow != -1 && biome == CFEPBiomeInit.CALLISTO_ROCKIES) {
                    // Larger distance = higher terrain
                    int shaveHeight = 65 + (distToLow * 4); 
                    for (int y = 120; y > shaveHeight; y--) {
                        ExtendedBlockStorage storage = chunk.getBlockStorageArray()[y >> 4];
                        if (storage != null) {
                            storage.set(x, y & 15, z, Blocks.AIR.getDefaultState());
                            modified = true;
                        }
                    }
                }

                int topY = chunk.getHeightValue(x, z);

                for (int y = 120; y > 0; y--) {
                    ExtendedBlockStorage storage = chunk.getBlockStorageArray()[y >> 4];
                    if (storage == null) continue;

                    IBlockState current = storage.get(x, y & 15, z);
                    
                    if (current.getMaterial().isLiquid()) {
                        storage.set(x, y & 15, z, Blocks.AIR.getDefaultState());
                        modified = true;
                        continue; 
                    }

                    if (current.getMaterial().isSolid()) {
                        if (current.getBlock() == Blocks.BEDROCK) break;

                        if (biome == CFEPBiomeInit.CALLISTO_OIL_SHALE && y >= topY - 3) {
                            storage.set(x, y & 15, z, oilSandState);
                            modified = true;
                        } else if (biome == CFEPBiomeInit.CALLISTO_BLACK_DESERT) {
                            int band = y % 14; 
                            if (band > 11) storage.set(x, y & 15, z, whiteRockState);
                            else if (band >= 4 && band <= 6) storage.set(x, y & 15, z, darkRockState);
                            modified = true;
                        } else if (biome == CFEPBiomeInit.CALLISTO_ROCKIES && y == topY) {
                            if (rand.nextInt(100) < 5) {
                                storage.set(x, y & 15, z, getRandomOre());
                                modified = true;
                            }
                        }
                        
                        if (y < topY - 10) break;
                    }
                }
            }
        }
        if (modified) chunk.markDirty();
    }


    private int getDistanceToLowBiome(World world, int x, int z, int maxRadius) {
        for (int r = 1; r <= maxRadius; r++) {
            if (isLow(world, x + r, z) || isLow(world, x - r, z) || 
                isLow(world, x, z + r) || isLow(world, x, z - r)) {
                return r;
            }
        }
        return -1;
    }

    private boolean isLow(World world, int x, int z) {
        Biome b = world.getBiome(new BlockPos(x, 0, z));
        return b != CFEPBiomeInit.CALLISTO_ROCKIES && b != CFEPBiomeInit.CALLISTO_FOOTHILLS;
    }
    
    private IBlockState getRandomOre() {
        int r = rand.nextInt(10);
        if (r < 6) return coalOreState; 
        if (r < 9) return ironOreState; 
        return goldOreState;            
    }

    private void initBlocks() {
        oilSandState = Blocks.SOUL_SAND.getDefaultState();

        net.minecraft.block.Block callisto = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        if (callisto != null) {
            whiteRockState = callisto.getStateFromMeta(0); 
            darkRockState = callisto.getStateFromMeta(2);  
        } else {
            whiteRockState = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(0); 
            darkRockState = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(15); 
        }

        net.minecraft.block.Block iron = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto_iron_ore"));
        ironOreState = (iron != null) ? iron.getDefaultState() : Blocks.IRON_ORE.getDefaultState();

        net.minecraft.block.Block coal = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto_coal_ore"));
        coalOreState = (coal != null) ? coal.getDefaultState() : Blocks.COAL_ORE.getDefaultState();
        
        net.minecraft.block.Block gold = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto_gold_ore"));
        goldOreState = (gold != null) ? gold.getDefaultState() : Blocks.GOLD_ORE.getDefaultState();
        
        initialized = true;
    }
}