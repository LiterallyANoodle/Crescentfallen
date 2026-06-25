package fracture.mod.planets;

import java.util.LinkedList;
import java.util.List;

import fracture.mod.init.CFdimensions;
import fracture.mod.init.CFplanets;
import fracture.mod.CFConfig;
import fracture.mod.init.BlockInit;
import fracture.mod.planets.hollows.ChunkProviderHollows;
//import fracture.mod.planets.hollows.CloudsHollows;
import fracture.mod.planets.hollows.SkyProviderHollows;
import fracture.mod.planets.hollows.hollows.biome.BiomeProviderHollows;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeAdaptive;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.ISolarLevel;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderHollows extends WorldProviderSpace implements ISolarLevel {

	  //zollorns
	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		if (super.getSkyRenderer() == null) {
			this.renderSky();
		}
		return super.getSkyRenderer();
	}

	protected void renderSky() {
		this.setSkyRenderer(new SkyProviderHollows(this));
	}

	//public IRenderHandler getCloudRenderer() {
	 //   return null; 
	//}
	
//	  protected void renderCloud() {
//		    setCloudRenderer((IRenderHandler)new CloudsHollows());
//		  }
//	  
	  public double getYCoordinateToTeleport() {
		    return 150.0D;
		  }
	  //zollorns
	  
	  
	  
	  
	  
//	//@Override
//	@SideOnly(Side.CLIENT)
//	protected void setCloudRenderer() {
//		if (super.getCloudRenderer() == null)
//        this.setCloudRenderer(new CloudsHollows());
//		//non functional
//    }
    @Override
    public boolean canRainOrSnow() {
        return true;
    }
    public boolean canDoRainSnowIce(Chunk chunk) {
        return canRainOrSnow();
        //return true;
      }
	

	@Override
	public Vector3 getSkyColor() {
	    return new Vector3(0.0, 0.0, 0.5);  // navy blue
	}

	@Override
	public Vector3 getFogColor() {
	    return new Vector3(0.95, 0.95, 0.9);  // off white
	}
    //@Override
    //public float getSolarSize() {
    //    return 1.5F;
    //}

    @Override
    public boolean hasSunset() {
        return true;
    }

    @Override
    public long getDayLength() {
        return 18000L;
    }
    
    //This changed to extend IChunkGenerator and not ChunkProviderBase. If nothing else errors, this may be the cause.
    @Override
    public Class<? extends IChunkGenerator> getChunkProviderClass() {
		return ChunkProviderHollows.class;
        //return (Class<? extends IChunkGenerator>) ChunkProviderHollows.class;
    }
    //^Formally:
    //@Override
    //public Class<? extends IChunkGenerator> getChunkProviderClass() {
        //return (Class<? extends IChunkGenerator>) ChunkProviderBase.class;
        //return (Class<? extends IChunkGenerator>) ChunkProviderHollows.class;

    @Override
    public Class<? extends BiomeProvider> getBiomeProviderClass() {
        BiomeAdaptive.setBodyMultiBiome(CFplanets.hollows);
        return BiomeProviderHollows.class;
    }

    @Override
    public double getHorizon() {
        return 44.0D;
    }

    @Override
    public int getAverageGroundLevel() {
        return 44;
    }

    @Override
    public boolean canCoordinateBeSpawn(int var1, int var2) {
        return true;
    }

    @Override
    public float getGravity() {
        return 0.052F;
    }

    @Override
    public int getHeight() {
        return 512;
    }

    @Override
    public double getMeteorFrequency() {
        return 3.0D;
    }

    @Override
    public double getFuelUsageMultiplier() {
        return 1.2D;
    }

    @Override
    public boolean canSpaceshipTierPass(int tier) {
		return tier >= CFConfig.CF_planet_settings.planetOneTier;
		//return tier >=3;
    }

    @Override
    public float getFallDamageModifier() {
        return 0.38F;

    }

    @Override
    public CelestialBody getCelestialBody() {
        return CFplanets.hollows;
    }

    @Override
    public float getThermalLevelModifier() {
        return 0.0F;
    }
    //@Override
    public boolean doesSnowGenerate() {
        return false;
    }
    
    @Override
    public double getSolarEnergyMultiplier() {
        return 3.5D;
    }

    @Override
    public DimensionType getDimensionType() {
        return micdoodle8.mods.galacticraft.core.util.WorldUtil.getDimensionTypeById(CFConfig.AddonDimensions.hollowsID);
    }
    @Override
    public int getDungeonSpacing() {
        return 0;
    }

    @Override
    public ResourceLocation getDungeonChestType() {
        return null;
    }
    
    @Override
    public List<Block> getSurfaceBlocks() {
        List<Block> list = new LinkedList<>();
		  list.add(BlockInit.SURFACE_HOLLOWS);
		  list.add(BlockInit.STONE_HOLLOWS);
		  list.add(Blocks.PACKED_ICE);
		  list.add(BlockInit.DRIED_DIRT);
		  list.add(Blocks.GRAVEL);
		  //...

        return list;
    }


	
}


	//}

	//BlockInit.SURFACE_FRACTURE.getDefaultState(); 
    //this.fillerBlock = BlockInit.STONE_FRACTURE.getDefaultState(); 
	//@Override
	//public float get {
	//return 0.38F;
	
	//}
