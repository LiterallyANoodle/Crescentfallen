package fracture.mod;

import net.minecraftforge.common.config.Config;

/**
 * Configuration Options Layout is from More-Planets addon for its simplicity
 * since it uses the annotation base Config options from Forge
 * 
 * Even though its modified to fit this tutorial all credit for the design go to
 * him.
 * 
 * https://github.com/SteveKunG/MorePlanets/blob/1.12.2/src/main/java/stevekung/mods/moreplanets/core/config/ConfigManagerMP.java
 */
@Config(modid = CFInfo.ID)
public class CFConfig {

	// @Config.LangKey(value = "addon_general")
	// @Config.Comment(value = "Base Addon Configuration: Version Checker, Debug,
	// Mod Option. etc.")
	// public static final General addon_general = new General();

	// @Config.LangKey(value = "addon_dimension")
	// @Config.Comment(value = "Planet or Moon Dimension IDs Configuration.")
	// public static final Dimension addon_dimension = new Dimension();

	// @Config.LangKey(value = "addon_dimension")
	// @Config.Comment(value = "Solar System Configuration.")
	// public static final SolarSystemSettings addon_solarsystem = new
	// SolarSystemSettings();

	@Config.LangKey(value = "Crescentfallen planet settings")
	@Config.Comment(value = "Planet Configuration.")
	public static final PlanetSettings CF_planet_settings = new PlanetSettings();

	// @Config.LangKey(value = "addon_spacestation_settings")
	// @Config.Comment(value = "SpaceStation Configuration.")
	// public static final SpaceStationSettings addon_spacestation_settings = new
	// SpaceStationSettings();

	// @Config.LangKey(value = "addon_world_gen")
	// @Config.Comment(value = "World Gen Configuration.")
	// public static final WorldGenSettings addon_world_gen_settings = new
	// WorldGenSettings();

	// @Config.LangKey(value = "addon_misc")
	// @Config.Comment(value = "Miscellaneous Configuration.")
	// public static final Misc addon_other = new Misc();

	public static class General {
		//@Config.Name(value = "Enable Debug Logging")
		//public boolean enableDebug = false;

		//@Config.Name(value = "Use Colored Star in the Sky")
		//@Config.RequiresWorldRestart
		//public boolean useColoredStar = true;

		//@Config.Name(value = "Use Fancy Star in the Sky")
		//@Config.RequiresWorldRestart
		//public boolean useFancyStar = true;
	}

	// public static final int dimensionIdFracture = -36;

	public static class SolarSystemSettings {

	}

	public static class AddonDimensions {

		@Config.Name(value = "Hollows Dimension ID")
		public static final int hollowsID = -566;
		
		@Config.Name(value = "Fracture Dimension ID")
		public static final int fractureID = -536;
		
		@Config.Name(value = "Dreamyard Dimension ID")
        public static final int dreamyardID = -546;
		
		//P1S1 IS HOLLOWS
		//P2S1 IS THE FRACTURE
		
		// @Config.Name(value = "PlanetOneS2 Dimension ID")
		// public static final int p1s2Id = -660;

		//@Config.Name(value = "Fracture Moon Dimension ID")
		//public static final int konaID = -4402;

		// @Config.Name(value = "PlanetTwo Space Station Dimension ID")
		// public int idSpaceStation = -4450;

		// @Config.Name(value = "PlanetTwo Space Station Dimension Static ID")
		// public int StaticidSpaceStation = -4451;

	}

	public static class PlanetSettings {

		@Config.Name(value = "Hollows rocket tier")
		@Config.RequiresMcRestart
		public int planetOneTier = 3;

		@Config.Name(value = "Fracture rocket tier")
		@Config.RequiresMcRestart
		public int planetTwoTier = 3;
		
		@Config.Name(value = "Dreamyard rocket tier")
		@Config.RequiresMcRestart
		public int planetThreeTier = 3;


	}

	public static class WorldGenSettings {

		// @Config.Name(value = "Disable Vanilla Ore Gen on all planets")
		// @Config.Comment(value = "Disables all non-modded ores on worlds: Lapis, Coal,
		// Iron, etc")
		// public boolean disableVanillaOreGenAllPlanets = false;

		// @Config.Name(value = "Disable Vanilla Ore on PlanetOne")
		// public boolean disableVanillaPlanetOneOre = false;

		// @Config.Name(value = "Disable Vanilla Ore on PlanetTwo")
		// public boolean disableVanillaPlanetTwoOre = false;

		// @Config.Name(value = "Disable Vanilla Ore on PlanetTwo Moon")
		// public boolean disableVanillaplTwoMoonOre = false;
	}

	public static class SpaceStationSettings {

		//@Config.Name(value = "PlanetTwo Space Station Dimension Static ID")
		//public int staticIdSpaceStation = -4451;

	}
	
	public static class BlockRegistrarSettings {
		
		@Config.Name(value = "Block JSON Data File Path")
		public String blockDataPath;
		
	}

	public static class Misc {

		@Config.Name(value = "Base Schematic ID")
		public int idBaseSchematic = 750;

		@Config.Name(value = "Base Schematic GUI ID")
		public int idBaseSchematicGui = 780;

	}

}
