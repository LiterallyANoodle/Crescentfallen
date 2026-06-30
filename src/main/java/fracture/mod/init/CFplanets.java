package fracture.mod.init;

import fracture.mod.CFConfig;
import fracture.mod.CFInfo;
import fracture.mod.planets.WorldProviderDreamyard;
import fracture.mod.planets.WorldProviderHollows;
import fracture.mod.planets.WorldProviderKona;
import fracture.mod.planets.WorldProviderTheFracture;
import fracture.mod.planets.dreamyard.biome.DreamyardBiomes;
import fracture.mod.planets.hollows.hollows.biome.HollowsBiomes;
import fracture.mod.planets.thefracture.thefracture.biome.TheFractureBiomes;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.world.AtmosphereInfo;
import micdoodle8.mods.galacticraft.api.world.EnumAtmosphericGas;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.dimension.TeleportTypeMoon;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedCreeper;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedEnderman;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSkeleton;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;
import micdoodle8.mods.galacticraft.planets.mars.dimension.TeleportTypeMars;
import micdoodle8.mods.galacticraft.planets.venus.dimension.TeleportTypeVenus;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.common.DimensionManager;

/**
 * Class AddonCelestialBodies
 * 
 * This is where we will store the public static Fields for all of our
 * CelestalObjects: - SolarSystems - Planets - Moons - SpaceStations (aka:
 * Satellites)
 * 
 * Each field can be called from other classes directly
 * 
 */
public class CFplanets {

	// Planets for addonSystem1(Triopas system)
	public static Planet hollows;
	public static Planet fracture;
	public static Planet dreamyard;
	public static Moon helius;
	public static Moon kona;


	// Planets for addonSystem2(Artemis 1)
	// public static Planet planetOneS2;

	/**
	 * this is the initialization method that will be called in the mods main class
	 * to build our celestial bodies
	 * 
	 * the order these are in are an important part of this compiling correctly
	 */
	public CFplanets() {
		this.createPlanets();
		this.registerPlanetTeleportTypes();
		this.registerPlanets();
		this.createMoons();
	}

	public void createPlanets() {

		// Triopas system Planets
		
		//"Hollows" register here

		hollows = new Planet("Hollows").setParentSolarSystem(CFsolarsystems.starTriopas);
		hollows.setTierRequired(3);
		hollows.setRingColorRGB(0.1F, 0.9F, 0.6F);
		hollows.setPhaseShift((float) (Math.PI * 0.5F));
		hollows.setRelativeDistanceFromCenter(new CelestialBody.ScalableDistance(1.4F, 1.4F));
		//used to be 0.9
		hollows.setRelativeOrbitTime(1.0F);
		hollows.setBodyIcon(new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/hollows.png"));
		hollows.atmosphereComponent(EnumAtmosphericGas.HYDROGEN);
		hollows.setDimensionInfo(CFConfig.AddonDimensions.hollowsID, WorldProviderHollows.class);
		hollows.setAtmosphere(new AtmosphereInfo(false, false, false, 5.0F, 0.0F, 0.1F));
		hollows.addChecklistKeys("space_suit", "equip_oxygen_suit", "equip_parachute");
		hollows.setBiomeInfo(HollowsBiomes.biomes);

		//DimensionManager.registerDimension(CFConfig.AddonDimensions.hollowsID, CFdimensions.hollowsDIM);

		//"Helius" Moon of Hollows, unreachable
		
		helius = new Moon("helius").setParentPlanet(hollows);
		helius.setUnreachable();
		helius.setRingColorRGB(0.1F, 0.9F, 0.6F);
		helius.setPhaseShift(10.8748F);
		helius.setRelativeDistanceFromCenter(new CelestialBody.ScalableDistance(16.9F, 16.9F));
		helius.setRelativeOrbitTime(13.0F);
		helius.setBodyIcon(new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/helius.png"));
		GalaxyRegistry.registerMoon(helius);
		
		
		//"Fracture" register here

		fracture = new Planet("Fracture").setParentSolarSystem(CFsolarsystems.starTriopas);
		fracture.setTierRequired(3);
		fracture.setRingColorRGB(0.1F, 0.9F, 0.6F);
		fracture.setPhaseShift((float) (Math.PI / 5.8748F));
		fracture.setRelativeDistanceFromCenter(new CelestialBody.ScalableDistance(2.3F, 2.3F));
		fracture.setRelativeOrbitTime(1.6F);
		fracture.setBodyIcon(new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/fracture.png"));
		fracture.atmosphereComponent(EnumAtmosphericGas.HYDROGEN);
		fracture.setDimensionInfo(CFConfig.AddonDimensions.fractureID, WorldProviderTheFracture.class);
		fracture.setAtmosphere(new AtmosphereInfo(false, false, false, 5.0F, 0.0F, 0.1F));
		fracture.addChecklistKeys("space_suit", "equip_oxygen_suit", "equip_parachute");
		fracture.setBiomeInfo(TheFractureBiomes.biomes);

		//DimensionManager.registerDimension(CFConfig.AddonDimensions.fractureID, CFdimensions.fractureDIM);

		//"Kona" Moon of Fracture, currently work in progress
		
		kona = new Moon("kona").setParentPlanet(fracture); 
		kona.setTierRequired(CFConfig.CF_planet_settings.konaTier);
		kona.setPhaseShift(0.0F);
		kona.setRelativeDistanceFromCenter(new CelestialBody.ScalableDistance(9F, 9F));
		kona.setRelativeOrbitTime(1 / 0.05F);
		kona.setRelativeSize(0.2667F);
		kona.setBodyIcon(new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/kona.png"));
		kona.atmosphereComponent(EnumAtmosphericGas.METHANE);
		kona.setDimensionInfo(CFConfig.AddonDimensions.konaID, WorldProviderKona.class);
		kona.addChecklistKeys("space_suit", "equip_oxygen_suit", "equip_parachute");


		//"Dreamyard" register here
        //DimensionManager.registerDimension(CFConfig.AddonDimensions.dreamyardID, WorldProviderDreamyard.DIM_TYPE); // Make sure DIM_TYPE is declared in WorldProvider

		dreamyard = new Planet("Dreamyard").setParentSolarSystem(CFsolarsystems.starTriopas);
		dreamyard.setTierRequired(3);
		dreamyard.setRingColorRGB(0.1F, 0.9F, 0.6F);
		dreamyard.setPhaseShift((float) (Math.PI / 5.8748F));
		dreamyard.setRelativeDistanceFromCenter(new CelestialBody.ScalableDistance(0.7F, 0.7F));
		dreamyard.setRelativeOrbitTime(0.6F);
		dreamyard.setBodyIcon(new ResourceLocation(CFInfo.ID, "textures/gui/celestialbodies/dreamyard.png"));
		dreamyard.atmosphereComponent(EnumAtmosphericGas.HYDROGEN);
		dreamyard.setDimensionInfo(CFConfig.AddonDimensions.dreamyardID, WorldProviderDreamyard.class);
		dreamyard.setAtmosphere(new AtmosphereInfo(false, false, false, 5.0F, 0.0F, 0.1F));
		dreamyard.addChecklistKeys("space_suit", "equip_oxygen_suit", "equip_parachute");
		dreamyard.setBiomeInfo(DreamyardBiomes.biomes);
		
		//GalaxyRegistry.registerTeleportType(WorldProviderDreamyard.class, new TeleportTypeCF());
		//GalaxyRegistry.registerRocketGui(WorldProviderDreamyard.class, new ResourceLocation("fracture:textures/gui/planet_selection_dreamyard.png"));

		//DimensionManager.registerDimension(CFConfig.AddonDimensions.dreamyardID, CFdimensions.dreamyardDIM);
		//Artemis 1 Planets here
		
		}

	
	


	private static void setMobInfo(CelestialBody body) {
		body.addMobInfo(new SpawnListEntry(EntityEvolvedZombie.class, 8, 2, 3));
		body.addMobInfo(new SpawnListEntry(EntityEvolvedSpider.class, 8, 2, 3));
		body.addMobInfo(new SpawnListEntry(EntityEvolvedSkeleton.class, 8, 2, 3));
		body.addMobInfo(new SpawnListEntry(EntityEvolvedCreeper.class, 8, 2, 3));
		body.addMobInfo(new SpawnListEntry(EntityEvolvedEnderman.class, 10, 1, 4));
	}

	private void registerPlanetTeleportTypes() {

		//GalacticraftRegistry.registerTeleportType(WorldProviderHollows.class, new TeleportTypeCF());
		//GalacticraftRegistry.registerTeleportType(WorldProviderTheFracture.class, new TeleportTypeCF());
		//GalacticraftRegistry.registerTeleportType(WorldProviderDreamyard.class, new TeleportTypeCF());
		
		GalacticraftRegistry.registerTeleportType(WorldProviderHollows.class, new TeleportTypeCF());
		GalacticraftRegistry.registerTeleportType(WorldProviderTheFracture.class, new TeleportTypeCF());
		GalacticraftRegistry.registerTeleportType(WorldProviderDreamyard.class, new TeleportTypeCF());
		GalacticraftRegistry.registerTeleportType(WorldProviderKona.class, new TeleportTypeCF());

		
	}

	private void registerPlanets() {

		GalaxyRegistry.registerPlanet(hollows);
		GalaxyRegistry.registerPlanet(fracture);
		GalaxyRegistry.registerPlanet(dreamyard);
		//GalaxyRegistry.registerPlanet(planetOneS2);
	//	GalaxyRegistry.registerTeleportType(WorldProviderDreamyard.class, new TeleportTypeDreamyard());

	}
	
	private void createMoons() {
		GalaxyRegistry.registerMoon(helius);
		GalaxyRegistry.registerMoon(kona);
	}

}

// private void registerPlanetTeleportTypes() {

// GalacticraftRegistry.registerTeleportType(WorldProviderPlanetOneS1.class, new
// TeleportTypeMars());
// MOON REGESTER TELEPORT TYPE GOES HERE. REMOVE NOTE TAG TO IMPLEMENT(not functional)
// GalacticraftRegistry.registerTeleportType(WorldProviderPlanetTwoS1.class, new
// TeleportTypeMoon());

// }

// private void registerPlanets() {

// GalaxyRegistry.registerPlanet(hollows);
// GalaxyRegistry.registerPlanet(fracture);

// GalaxyRegistry.registerPlanet(planetOneS2);

/// }

//}