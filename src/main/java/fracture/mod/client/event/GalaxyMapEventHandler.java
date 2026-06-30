package fracture.mod.client.event;

import fracture.mod.client.gui.Gui3DGalaxyMap;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GalaxyMapEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {
        
        if (event.getGui() == null) {
            return;
        }

        //System.out.println("[Crescentfallen] Attempting to open GUI: " + event.getGui().getClass().getName());

        if (event.getGui() instanceof GuiCelestialSelection && !(event.getGui() instanceof Gui3DGalaxyMap)) {
            
            //System.out.println("[Crescentfallen] Success, Intercepted GuiCelestialSelection, Loading 3D Map");
            event.setGui(new Gui3DGalaxyMap(true, null));
            
        } 
        else if (event.getGui().getClass().getName().contains("CustomCelestialSelection")) {
            
            //System.out.println("[Crescentfallen] Success, Intercepted ExtraPlanets Map, Loading 3D Map");
            event.setGui(new Gui3DGalaxyMap(true, null));
            
        }
    }
}