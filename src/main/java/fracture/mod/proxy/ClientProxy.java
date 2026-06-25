package fracture.mod.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import fracture.mod.client.event.HidePressureScissor;
import fracture.mod.client.sky.AsteroidSkyHandler;
//import fracture.mod.client.event.HidePressureTick;
import fracture.mod.util.Reference;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import fracture.mod.util.handlers.ClientEventHandler;
import fracture.mod.util.handlers.GunEventHandler;
import com.mjr.extraplanets.Config;

public class ClientProxy extends Proxy {

    private boolean skyRendererSet = false;

    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
    }

    @Override
    public void registerVariantRenderer(Item item, int meta, String filename, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(new ResourceLocation(Reference.MODID, filename), id));
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        // init OBJ loader
        OBJLoader.INSTANCE.addDomain("fracture");
        MinecraftForge.EVENT_BUS.register(new AsteroidSkyHandler());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        MinecraftForge.EVENT_BUS.register(new HidePressureScissor());
        //MinecraftForge.TERRAIN_GEN_BUS.register(new fracture.mod.world.epchanges.callisto.CfCallistoTerrainSculptor());
        // GUN SYSTEM HANDLERS TESTING
        
        // Register the Logic Handler (Used for firing animation)
        // Registers mouse click and sets the NBT tag
        MinecraftForge.EVENT_BUS.register(new GunEventHandler());

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        // Register Render Handler (Optional if you used @EventBusSubscriber)
        // MinecraftForge.EVENT_BUS.register(new fracture.mod.client.render.GunRenderHandler());
        
        //..
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        // sky renderer
        MinecraftForge.EVENT_BUS.register(this); 
    }
}