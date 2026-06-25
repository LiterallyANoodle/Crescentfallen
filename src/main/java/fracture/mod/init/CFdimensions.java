package fracture.mod.init;

import fracture.mod.CFConfig;
//import fracture.mod.AddonConfig.Dimension;
import fracture.mod.planets.WorldProviderHollows;
import fracture.mod.planets.WorldProviderTheFracture;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class CFdimensions {

	public static DimensionType hollowsDIM;
	public static DimensionType fractureDIM;
	public static DimensionType dreamyardDIM;
	public static DimensionType konaDIM;
	
    //registerDimension( 1, DimensionType.);

	// public static final DimensionType dimPlanetTwoS1 =
	// DimensionType.register("AddonDimensions.dimPlanetTwoS1",
	// "_addondimensions.dimplanettwoS1", -36, WorldProviderTheFracture.class,
	// false);
	// public static DimensionType dimPlanetOneS2;
	// private static DimensionType dimPlanetTwoS1;

	public static void init() {
		// Dimension resolved to addonconfig, may not be correct. If it doesent work,
		// try resolving it to dim from default forge library

		//CFdimensions.hollowsDIM = WorldUtil.getDimensionTypeById(CFConfig.AddonDimensions.hollowsID);
		// AddonDimensions.dimPlanetOneS1 = WorldUtil.getDimensionTypeById(-650);

		// TESTING 5/22/25
		//CFdimensions.fractureDIM = WorldUtil.getDimensionTypeById(CFConfig.AddonDimensions.fractureID);
		// AddonDimensions.dimPlanetOneS2 =
		// WorldUtil.getDimensionTypeById(Dimension.p1s2Id);
		

		//CFdimensions.dreamyardDIM = WorldUtil.getDimensionTypeById(CFConfig.AddonDimensions.dreamyardID);

	}

	//public static void registerDimensions() {

		//DimensionManager.registerDimension(-39, dimPlanetTwoS1);
		
		
	//	DimensionManager.registerDimension(fracture.mod.AddonConfig.Dimension.p1s1Id, dimPlanetOneS1);
      //  DimensionManager.registerDimension(fracture.mod.AddonConfig.Dimension.p2s1Id,dimPlanetTwoS1);

	//}

}