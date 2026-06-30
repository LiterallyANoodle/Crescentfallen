package fracture.mod.planets;

import java.util.LinkedList;
import java.util.List;

import fracture.mod.CFConfig;
import fracture.mod.init.BlockInit;
import fracture.mod.init.CFdimensions;
import fracture.mod.init.CFplanets;
import fracture.mod.planets.dreamyard.ChunkProviderdreamyard;
import fracture.mod.planets.dreamyard.biome.BiomeProviderdreamyard;
import fracture.mod.planets.thefracture.SkyProviderthefracture;
import fracture.mod.planets.dreamyard.SkyProviderdreamyard;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeAdaptive;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
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

public class WorldProviderDreamyard extends WorldProviderSpace implements IGalacticraftWorldProvider, ISolarLevel {

	//public static final DimensionType DIM_TYPE = DimensionType.register("Dreamyard", "_dreamyard",CFConfig.AddonDimensions.dreamyardID, WorldProviderDreamyard.class, false);

	
    @Override
    @SideOnly(Side.CLIENT)
    public IRenderHandler getSkyRenderer() {
		if (super.getSkyRenderer() == null) {
			this.renderSky();
        }
		return super.getSkyRenderer();

    }
		protected void renderSky() {
			this.setSkyRenderer(new SkyProviderdreamyard(this));
		}
		
    @Override
    public Vector3 getFogColor() {
        return new Vector3(0.5, 0.3, 0.8); // Light purple
    }

    @Override
    public Vector3 getSkyColor() {
        return new Vector3(0.15, 0.05, 0.25); // Deep space purple
    }
    
    @Override
    public Class<? extends BiomeProvider> getBiomeProviderClass() {
        BiomeAdaptive.setBodyMultiBiome(CFplanets.dreamyard);
        return BiomeProviderdreamyard.class;
        
    }

    @Override
    public boolean canRainOrSnow() {
        return true;
    }

    @Override
    public void updateWeather() {
        super.updateWeather();
    }

    @Override
    public boolean hasSunset() {
        return true;
    }

    @Override
    public long getDayLength() {
        return 24000L; //24000000L
    }
    @Override
    public String getSaveFolder() {
        return "dreamyard";
    }
    
    @Override
    public boolean shouldForceRespawn() {
        return false;
    }
    public boolean canDoRainSnowIce(Chunk chunk) {
        return canRainOrSnow();
      }
    @Override
    public Class<? extends IChunkGenerator> getChunkProviderClass() {
        return ChunkProviderdreamyard.class;
    }



    @Override
    public int getAverageGroundLevel() {
        return 63;
    }

    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        return true;
    }

    @Override
    public float getGravity() {
        return 0.00F;
    }

    @Override
    public int getHeight() {
        return 800;
    }

    @Override
    public double getMeteorFrequency() {
        return 0.0;
    }

    @Override
    public double getFuelUsageMultiplier() {
        return 1.0;
    }

    @Override
    public boolean canSpaceshipTierPass(int tier) {
        return tier >= CFConfig.CF_planet_settings.planetThreeTier;
    }

    @Override
    public float getFallDamageModifier() {
        return 1.0F;
    }

    @Override
    public float getSoundVolReductionAmount() {
        return 1.0F;
    }

    @Override
    public CelestialBody getCelestialBody() {
        return CFplanets.dreamyard;
    }

    @Override
    public boolean hasBreathableAtmosphere() {
        return true;
    }

    @Override
    public float getThermalLevelModifier() {
        return 0.0F;
    }

    @Override
    public float getWindLevel() {
        return 10.0F;
    }

    @Override
    public double getSolarEnergyMultiplier() {
        return 1.0;
    }

    @Override
    public boolean shouldDisablePrecipitation() {
        return false;
    }

    @Override
    public boolean shouldCorrodeArmor() {
        return false;
    }

    @Override
    public DimensionType getDimensionType() {
        return micdoodle8.mods.galacticraft.core.util.WorldUtil.getDimensionTypeById(CFConfig.AddonDimensions.dreamyardID);
    }

    @Override
    public boolean isDaytime() {
        float angle = this.world.getCelestialAngle(0F);
        return angle < 0.42F || angle > 0.58F;
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
        list.add(BlockInit.OVERGRASS_FULL);
        list.add(BlockInit.OVERGRASS_BLOCK);
        list.add(BlockInit.DREAMYARD_GRASS);

        list.add(BlockInit.BLOODFLOWER);
        list.add(BlockInit.RED_FLOWERS);
        list.add(BlockInit.BLUE_FLOWERS);
        list.add(BlockInit.YELLOW_FLOWERS);
        list.add(BlockInit.DREAMYARD_LOTUS);
        list.add(BlockInit.ETHERIAL_MUSHROOM_BLUE);
        list.add(BlockInit.ETHERIAL_MUSHROOM_GREEN);
        list.add(BlockInit.ETHERIAL_MUSHROOM_PURPLE);
        list.add(BlockInit.GUILDED_ALLIUM);
        list.add(BlockInit.HYACINTH);
        list.add(BlockInit.KING_ALLARIUS);
        list.add(BlockInit.MINIGRASS1);
        list.add(BlockInit.MINIGRASS2);
        list.add(BlockInit.PAEONIA);
        list.add(BlockInit.RAINBOW_ROD);
        list.add(BlockInit.BIRDS_OF_PARADISE);
        list.add(BlockInit.DREAMYARD_TALLGRASS);
        list.add(BlockInit.ALIEN_WEEDS);

        list.add(BlockInit.DREAMSTONE);
        list.add(BlockInit.DREAMYARD_COBBLESTONE);
        list.add(BlockInit.STONEBRICK_DREAMYARD);
        list.add(BlockInit.STONEBRICK_DREAMYARD_SMALL);
        
        list.add(BlockInit.DREAM_SAND);
        list.add(BlockInit.GRAVEL_DREAMYARD);
        list.add(BlockInit.BLOCK_SERITONIUM);
        
        list.add(Blocks.GRASS);
        list.add(Blocks.DIRT);
        list.add(Blocks.STONE);
        return list;
    }
}