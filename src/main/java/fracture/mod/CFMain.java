package fracture.mod;

//import fracture.mod.client.render.HidePressureOverlay;
//import fracture.mod.AddonConfig.Dimension;
import fracture.mod.init.CFdimensions;
import fracture.mod.init.CFplanets;
import fracture.mod.init.CFsolarsystems;
import fracture.mod.planets.WorldProviderDreamyard;
import fracture.mod.planets.WorldProviderHollows;
import fracture.mod.planets.WorldProviderTheFracture;
import fracture.mod.proxy.Proxy;
import fracture.mod.tabs.CrescentfallenBlockstab;
import fracture.mod.tabs.CrescentfallenGunstab;
//import fracture.mod.tabs.CresentfallenTab;
import fracture.mod.tabs.CrescentfallenTabitems;
import fracture.mod.util.Reference;
import fracture.mod.util.handlers.CameraTiltHandler;
import fracture.mod.util.handlers.ConfigHandler;
import fracture.mod.util.handlers.DiveHandler;
import fracture.mod.util.handlers.DivePacket;
import fracture.mod.util.handlers.FlowerSpawnHandler;
import fracture.mod.util.handlers.KeybindHandler;
import fracture.mod.util.handlers.PlayerMovementHandler;
import fracture.mod.util.handlers.SlideCancelPacket;
import fracture.mod.world.epchanges.CfEuropaChunkgen;
import fracture.mod.world.epchanges.CfIoChunkgen;
import fracture.mod.world.epchanges.CfOberonChunkgen;
//import fracture.mod.world.epchanges.WorldGenEuropaIce;
//import fracture.mod.world.epchanges.IoTerrainDecorator;
//import fracture.mod.world.epchanges.WorldGenIoOptimized;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION)
public class CFMain {

	public static final CrescentfallenBlockstab CrescentfallenBlocks = new CrescentfallenBlockstab("crescentfallenblocks");
	public static final CreativeTabs CrescentfallenItems = new CrescentfallenTabitems("crescentfallenitems");
	public static final CreativeTabs CrescentfallenGuns = new fracture.mod.tabs.CrescentfallenGunstab("crescentfallen_guns");
	
	
	
	
	//I think most of the bloat here can be condensed and organized using the mod proxies? but this is the testing branch 
	//and will be fixed later. note this down.
	
	
	//MOVEMENT SYSTEM TESTING
	public static SimpleNetworkWrapper NETWORK;

	//CAMERA TILT TESTING
    public static boolean enableCameraTilt = true;
	
	@Instance
	public static CFMain instance;

	@SidedProxy(clientSide = "fracture.mod.proxy.ClientProxy", serverSide = "Reference.COMMON")

	public static Proxy proxy;

	// this is where the working 'system' code was previously
	// if needed put it back here
	// see tutorial for details
	// edit 6/10/25. issue is fixed.

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		
		
		//CAMERA TILT TESTING
	    ConfigHandler.init(event.getSuggestedConfigurationFile());

		  if (event.getSide().isClient()) {
	            MinecraftForge.EVENT_BUS.register(new CameraTiltHandler());
	        }
	    
		// Call this in the preInit (make sure you register any blocks or items first)
		CFdimensions.init();

		new CFsolarsystems();
		new CFplanets();
		// AddonPlanets.this.registerPlanets();
		//public static final int dreamyardDIM = ConfigManagerCore. 1010;
		
		//MinecraftForge.EVENT_BUS.register(new HidePressureOverlay());
		
		// new AddonDimensions();
		
		// --- DEBUG START ---
	    //System.out.println("--------------------------------------------------");
	    //System.out.println("[Fracture] PRE-INIT STARTED");
	    
	    // Register the Generator here (Safer than init)
	    MinecraftForge.EVENT_BUS.register(CfIoChunkgen.getInstance());	    
	    MinecraftForge.EVENT_BUS.register(CfEuropaChunkgen.getInstance());
//	    MinecraftForge.EVENT_BUS.register(new CfOberonChunkgen());
	    //GameRegistry.registerWorldGenerator(new WorldGenEuropaIce(), 2000); // Higher weight runs later
	    //System.out.println("[Fracture] Generator Registered successfully!");
	    //System.out.println("--------------------------------------------------");
	    // --- DEBUG END ---
	    
		proxy.preInit(event);
		// GalacticraftCore.satelliteSpaceStation = (Satellite) new
		// Satellite("spacestation.fracture").setParentBody(AddonPlanets.planetTwoS1).setRelativeSize(0.2667F).setRelativeDistanceFromCenter(new
		// CelestialBody.ScalableDistance(9F, 9F)).setRelativeOrbitTime(1 / 0.05F);
		// non functional; fix later.
		//DimensionManager.registerDimension(dreamyardDIM, WorldProviderDreamyard.class);

		//MinecraftForge.EVENT_BUS.register(new DiveHandler());
		
		
		//MOVEMENT SYSTEM TESTING
		MinecraftForge.EVENT_BUS.register(new PlayerMovementHandler());
		
		
		//MOVEMENT SYSTEM TESTING
		 NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);

	        // Register packet: id=0, handler class, message class, side
	       // NETWORK.registerMessage(DivePacket.Handler.class, DivePacket.class, 0, Side.SERVER);
		 	//DivePacket.Handler, DivePacket.class
		
	        
            //TESTING 9/21/25

	        
	        
	       // NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);
	     // existing DivePacket registration (id 0)
	     NETWORK.registerMessage(DivePacket.Handler.class, DivePacket.class, 0, Side.SERVER);

	     // NEW: register SlideCancelPacket (id 1) (not working)
	     NETWORK.registerMessage(SlideCancelPacket.Handler.class, SlideCancelPacket.class, 1, Side.SERVER);
		
	     
	     
	     //..
		
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {

		// new AddonSolarSystems();
		// new AddonPlanets();
		// new AddonDimensions();

        //proxy.init();
        MinecraftForge.EVENT_BUS.register(new fracture.mod.util.handlers.CommonEventHandler());            

		//MOVEMENT SYSTEM TESTING
        // register keybinds client-side only
        if (event.getSide().isClient()) {
            KeybindHandler.register();   
        }
		
		
        	//this has to do with the dash HUD icon (not working)
            if (event.getSide().isClient()) {
                MinecraftForge.EVENT_BUS.register(new fracture.mod.util.handlers.HudOverlayHandler());
            }
            
          //TESTING 1/3/26
            //fracture.mod.util.handlers.DiveHandler.register();
            
            //TESTING 9/21/25
            if (event.getSide().isClient()) {
                KeybindHandler.register();   
                MinecraftForge.EVENT_BUS.register(new fracture.mod.util.handlers.HudOverlayHandler());
                // client-side slide visuals(not working)
                MinecraftForge.EVENT_BUS.register(new fracture.mod.util.handlers.SlideClientHandler());
            }
            MinecraftForge.EVENT_BUS.register(new fracture.mod.util.handlers.GanymedeInjector());
		//...
            // Replacer test
            //MinecraftForge.EVENT_BUS.register(new IoTerrainDecorator());
            //GameRegistry.registerWorldGenerator(new WorldGenIoOptimized(), 1000);
            
            //GameRegistry.registerWorldGenerator(new WorldGenIoOptimized(), 1000);
            //MinecraftForge.EVENT_BUS.register(new IoFastReplacer());
            //alpha rose spawner(not working)
            
            MinecraftForge.EVENT_BUS.register(new FlowerSpawnHandler());
            // Register IO World Load Tweaker
            //MinecraftForge.EVENT_BUS.register(new IoWorldTweaker());
            
    		proxy.init(event);

	}

	@EventHandler
	public static void receiveIMC(final IMCEvent event) {
		proxy.receiveIMC(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// Register addon dimensions used by planets/moons/etc.. in postInit
		GalacticraftRegistry.registerDimension("CF.hollows", "addon.hollowsdim", CFConfig.AddonDimensions.hollowsID,
				WorldProviderHollows.class, false);
		CFdimensions.hollowsDIM = WorldUtil.getDimensionTypeById(CFConfig.AddonDimensions.hollowsID);

		GalacticraftRegistry.registerDimension("CF.fracture", "addon.fracturedim", CFConfig.AddonDimensions.fractureID,
				WorldProviderTheFracture.class, false);
		CFdimensions.fractureDIM = WorldUtil.getDimensionTypeById(CFConfig.AddonDimensions.fractureID);

		
		GalacticraftRegistry.registerDimension("CF.dreamyard", "addon.dreamyarddim", CFConfig.AddonDimensions.dreamyardID,
                WorldProviderDreamyard.class, false);
        CFdimensions.dreamyardDIM = WorldUtil.getDimensionTypeById(CFConfig.AddonDimensions.dreamyardID);
		//CFdimensions.init();
        //fracture.mod.world.epchanges.IoBiomeTweaker.tweak();
        //MinecraftForge.EVENT_BUS.register(new fracture.mod.world.epchanges.IoTerrainEventHandler());        
        
        
        

        
		proxy.postInit(event);
		//new CFsolarsystems();
		//new CFplanets();

		
		
		// @Override
		// fracture.mod.init.AddonPlanets.registerPlanets();
		// AddonPlanets planets = new AddonPlanets();
	    //CFsolarsystems.registerSolarSyst();

	    //CFplanets.createPlanets();
		//.createPlanets();
	    //CFplanets.registerPlanetTeleportTypes();
	    

		//.registerPlanets();
		//this.createMoons();
		// this.createPlanets();
		// this.registerPlanetTeleportTypes();
		// this.registerPlanets();
		// this.createMoons();
		// ???
		
		// Register dimensions for Hollow and The Fracture planets


		// AddonDimensions.init();

//		CODE old 6/30/25		
//		GalacticraftRegistry.registerDimension("AddonDimensions.dimPlanetOneS1", "_addonDimensions.dimplanetones1,",
//				AddonConfig.AddonDimensions.p1s1Id, WorldProviderHollows.class, false);
//		AddonDimensions.dimPlanetOneS1 = WorldUtil.getDimensionTypeById(AddonConfig.AddonDimensions.p1s1Id);
//
//		GalacticraftRegistry.registerDimension("AddonDimensions.dimPlanetTwoS1", "_addonDimensions.dimplanettwos1,",
//				AddonConfig.AddonDimensions.p2s1Id, WorldProviderTheFracture.class, false);
//
//		AddonDimensions.dimPlanetTwoS1 = WorldUtil.getDimensionTypeById(AddonConfig.AddonDimensions.p2s1Id);
//		AddonDimensions.dimPlanetOneS1 = WorldUtil.getDimensionTypeById(AddonConfig.AddonDimensions.p1s1Id);

		// without thiss code, the planet ids show "fracture" and "hollows" with thier
		// proper dim ids in the config file.
		// when attempting to load them, the game crashes.

		// Not yet defined
		// GalacticraftRegistry.getTeleportTypeForDimension(AddonConfig.Dimension.p2s1Id
		// <WorldProviderPlanetTwoS1.class>);

	}

	public static World getWorld() {
		return proxy.getWorld();
	}
}
