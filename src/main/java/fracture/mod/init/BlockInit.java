package fracture.mod.init;

import java.util.ArrayList;
import java.util.List;
import fracture.mod.util.IHasModel;
import fracture.mod.annotation.GeneratorTarget;
import fracture.mod.objects.blocks.BlockBase;
import fracture.mod.objects.blocks.BlockOres;
//import fracture.mod.objects.blocks.BlockOres;
//import fracture.mod.objects.blocks.BlockOres;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockInit {
	public static final List<Block> BLOCKS = new ArrayList<Block>();
	
	//--------------------------------MATERIAL
	
	public static final Block BLOCK_CENTURIUM = new BlockOfCenturium("block_centurium", Material.IRON);
	public static final Block BLOCK_TERRAMINIUM = new Blockofmetal("block_terraminium", Material.IRON);
	public static final Block BLOCK_SERITONIUM = new Blockofmetal("block_seritonium", Material.IRON);

	//--------------------------------TURF
	
	public static final Block DRIED_DIRT = new SpaceDirtNoPlant("dried_dirt", Material.GROUND);
	public static final Block DREAMYARD_DIRT = new SpaceDirt("dreamyard_dirt", Material.GROUND);
	//public static final Block SAND_FRACTURE_BROKEN = new SpaceDirt("sand_fracture_broken", Material.GROUND);
	
	public static final Block GRAVEL_FRACTURE = new SpaceSand("gravel_fracture", Material.SAND);
	public static final Block GRAVEL_DREAMYARD = new SpaceSand("gravel_dreamyard", Material.SAND);
	//public static final Block GRAVEL_KONA = new SpaceSand("gravel_kona", Material.SAND);
	public static final Block SAND_FRACTURE = new SpaceSand("sand_fracture", Material.SAND);
	public static final Block SAND_FRACTURE_HOT = new SpaceSand("sand_fracture_hot", Material.SAND);
	public static final Block SALT_DEPOSIT = new SpaceSand("salt_deposit", Material.SAND);
	public static final Block DREAM_SAND = new SpaceSand("dream_sand", Material.SAND);

	//--------------------------------STONE
	
	//the fracture/destruction point
	public static final Block STONE_FRACTURE = new SpaceStone("stone_fracture", Material.ROCK);
	public static final Block ASTROID_FRACTURE = new SpaceStone("astroid_fracture", Material.ROCK);
	public static final Block SURFACE_FRACTURE = new SpaceStone("surface_fracture", Material.ROCK);
	public static final Block BURNT_STONE_FRACTURE = new SpaceStone("burnt_stone_fracture", Material.ROCK);
	public static final Block COBBLESTONE_FRACTURE = new SpaceStone("cobblestone_fracture", Material.ROCK);
	public static final Block PEAK_SEDIMENT_FRACTURE = new SpaceStone("peak_sediment_fracture", Material.ROCK);
	public static final Block STONE_SUBSURFACE_FRACTURE = new SpaceStone("stone_subsurface_fracture", Material.ROCK);
	public static final Block STONEBRICK_FRACTURE = new SpaceStone("stonebrick_fracture", Material.ROCK);

	
	//kona
	public static final Block STONE_KONA = new SpaceStone("stone_kona", Material.ROCK);
	public static final Block STONE_SUBSURFACE_KONA = new SpaceStone("stone_subsurface_kona", Material.ROCK);
	public static final Block SURFACE_KONA = new SpaceStone("surface_kona", Material.ROCK);
	public static final Block COBBLESTONE_KONA = new SpaceStone("cobblestone_kona", Material.ROCK);
	public static final Block STONEBRICK_KONA = new SpaceStone("stonebrick_kona", Material.ROCK);


	//hollows
	public static final Block STONE_HOLLOWS = new SpaceStone("hollows_stone", Material.ROCK);
	public static final Block SURFACE_HOLLOWS = new SpaceStone("surface_hollows", Material.ROCK);


	//dreamyard
	public static final Block DREAMSTONE = new SpaceStone("dreamstone", Material.ROCK);
	public static final Block DREAMYARD_COBBLESTONE = new SpaceStone("dreamyard_cobblestone", Material.ROCK);
	public static final Block STONEBRICK_DREAMYARD = new SpaceStone("stonebrick_dreamyard", Material.ROCK);
	public static final Block STONEBRICK_DREAMYARD_SMALL = new SpaceStone("stonebrick_dreamyard_small", Material.ROCK);

	
	//--------------------------------FAUNA

	
	
	public static final Block OVERGRASS_FULL = new SpaceGrass("overgrass_full", Material.GROUND);
	public static final Block OVERGRASS_BLOCK = new SpaceGrass("overgrass_block", Material.GROUND);
	public static final Block DREAMYARD_GRASS = new Spaceplant("dreamyard_grass", Material.PLANTS);
	
	public static final Block BLOODFLOWER = new Spaceplant("bloodflower", Material.PLANTS);
	public static final Block RED_FLOWERS = new Spaceplant("red_flowers", Material.PLANTS);
	public static final Block BLUE_FLOWERS = new Spaceplant("blue_flowers", Material.PLANTS);
	public static final Block YELLOW_FLOWERS = new Spaceplant("yellow_flowers", Material.PLANTS);
	public static final Block DREAMYARD_LOTUS = new Spaceplant("dreamyard_lotus", Material.PLANTS);
	public static final Block ETHERIAL_MUSHROOM_BLUE  = new SpaceMushroom("etherial_mushroom_blue");
	public static final Block ETHERIAL_MUSHROOM_GREEN = new SpaceMushroom("etherial_mushroom_green");
	public static final Block ETHERIAL_MUSHROOM_PURPLE= new SpaceMushroom("etherial_mushroom_purple");
	public static final Block GUILDED_ALLIUM = new Spaceplant("guilded_allium", Material.PLANTS);
	public static final Block HYACINTH = new Spaceplant("hyacinth", Material.PLANTS);
	public static final Block KING_ALLARIUS = new Spaceplant("king_allarius", Material.PLANTS);
	public static final Block MINIGRASS1 = new Spaceplant("minigrass1", Material.PLANTS);
	public static final Block MINIGRASS2 = new Spaceplant("minigrass2", Material.PLANTS);
	public static final Block PAEONIA = new Spaceplant("paeonia", Material.PLANTS);
	public static final Block RAINBOW_ROD = new Spaceplant("rainbow_rod", Material.PLANTS);
	public static final Block BIRDS_OF_PARADISE = new SpaceDoublePlant("birds_of_paradise", Material.PLANTS);
	public static final Block DREAMYARD_TALLGRASS = new SpaceDoublePlant("dreamyard_tallgrass", Material.PLANTS);
	public static final Block ALIEN_WEEDS  = new Spaceplant("alien_weeds", Material.PLANTS);
	public static final Block ALPHA_ROSE  = new Spaceplant("alpha_rose", Material.PLANTS);


	
	//--------------------------------MISC
	
	public static final Block FLESH_PILE = new FleshPile("flesh_pile", Material.GROUND);
	public static final Block TRASH_PILE = new Trash("trash_pile", Material.GROUND);
	public static final Block DARK_BLOCK = new Darkblock("dark_block", Material.ROCK);
	public static final Block KONA_RUST = new Blockofmetal("kona_rust", Material.IRON);
	public static final Block RUSTY_PIPE2 = new Blockofmetal("rusty_pipe2", Material.IRON);
	public static final Block SPACECRAFT_FLOOR = new Blockofmetal("spacecraft_floor", Material.IRON);
	public static final Block SPACECRAFT_WALL = new Blockofmetal("spacecraft_wall", Material.IRON);

	
	
	
	//--------------------------------ORES
	
	//orebasic
	public static final Block ORE_OVERWORLD = new BlockOres("ore_overworld", "overworld");
	//public static final Block ORE_FRACTURE = new BlockOres("ore_fracture", "CF.fractureDIM");
	//public static final Block ORE_KONA = new BlockOres("ore_kona", "CF.konaDIM");
	//public static final Block ORE_HOLLOWS = new BlockOres("ore_hollows", "CF.hollowsDIM");
	//public static final Block ORE_DREAMYARD = new BlockOres("ore_dreamyard", CF.dreamyardDIM);
	
	//the fracture
	public static final Block ORE_FRACTURE_URANIUM = new SpaceOre0("ore_fracture_uranium", Material.ROCK);
	public static final Block ORE_FRACTURE_IRON = new SpaceOre0("ore_fracture_iron", Material.ROCK);
	public static final Block ORE_FRACTURE_TIN = new SpaceOre0("ore_fracture_tin", Material.ROCK);
	public static final Block ORE_FRACTURE_COPPER = new SpaceOre0("ore_fracture_copper", Material.ROCK);
	
	//kona
	public static final Block ORE_KONA_COPPER = new SpaceOre0("ore_kona_copper", Material.ROCK);
	public static final Block ORE_KONA_IRON = new SpaceOre0("ore_kona_iron", Material.ROCK);
	public static final Block ORE_KONA_TIN = new SpaceOre0("ore_kona_tin", Material.ROCK);
	
	//hollows
	//public static final Block ORE_HOLLOWS_TERRAMINIUM = new SpaceOreIce("ore_hollows_terraminium", Material.ICE);
	//public static final Block ORE_HOLLOWS_PLATINUM = new SpaceOre2("ore_hollows_platinum", Material.ROCK);
	//public static final Block ORE_HOLLOWS_ANTIGRAVITY = new SpaceOreGravity("ore_hollows_antigravity", Material.ROCK);
	
	//dreamyard
	//public static final Block ORE_DREAMYARD_IRON = new SpaceOre1("ore_fracture_uranium", Material.ROCK);
	//public static final Block ORE_DREAMYARD_GOLD = new SpaceOre0("ore_fracture_iron", Material.ROCK);
	//public static final Block ORE_DREAMYARD_URANIUM = new SpaceOre2("ore_fracture_tin", Material.ROCK);
	//public static final Block ORE_DREAMYARD_COPPER = new SpaceOre1("ore_fracture_copper", Material.ROCK);
	//public static final Block ORE_DREAMYARD_TIN = new SpaceOre1("ore_fracture_copper", Material.ROCK);
	//public static final Block ORE_DREAMYARD_ALUMINUM = new SpaceOre1("ore_fracture_copper", Material.ROCK);
	
	//gems
	//public static final Block ORE_DREAMYARD_EMERALD = new SpaceOreEmerald("ore_dreamyard_emerald", Material.ROCK);
	//public static final Block ORE_FRACTURE_EMERALD = new SpaceOreEmerald("ore_fracture_emerald", Material.ROCK);
	//public static final Block ORE_FRACTURE_COAL = new SpaceOreCoal("ore_fracture_coal", Material.ROCK);
	//public static final Block ORE_DREAMYARD_COAL = new SpaceOreCoal("ore_dreamyard_coal", Material.ROCK);
	public static final Block ORE_KONA_TANZANITE = new SpaceOreTanz("ore_kona_tanzanite", Material.ROCK);
	//public static final Block ORE_DREAMYARD_DIAMOND = new SpaceOreDiamond("ore_dreamyard_diamond", Material.ROCK);
	//public static final Block ORE_FRACTURE_DIAMOND = new SpaceOreDiamond("ore_fracture_diamond", Material.ROCK);
	//public static final Block ORE_DREAMYARD_PLUTO = new SpaceOrePlutonium("ore_dreamyard_pluto", Material.ROCK);
	
	//misc
	//public static final Block ORE_CENTURIUM = new SpaceOreCent("ore_centurium", Material.IRON);
	//public static final Block OIL_SHALE = new OilShale("oil_shale", Material.ROCK);
	//public static final Block SERITONIN_SHALE = new SerotoninShale("oil_shale", Material.ROCK);
	
	//--------------------------------MACHINES
	
	//public static final Block tester = new BlockBase("tester", Material.ROCK);
	//public static final Block tester = new Machine2("recycle", Material.IRON);
	//this will be used to recycle things you find on earth to build a rocket. 
	//will spawn static in launch sites.
	
}
