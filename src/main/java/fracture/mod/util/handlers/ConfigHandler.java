package fracture.mod.util.handlers;

import java.io.File;

import fracture.mod.CFMain;
import micdoodle8.mods.galacticraft.core.Constants;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler 
{
	public static Configuration config; 
	public static boolean enableDestructionEvent = true;
	
	public static void init(File file)
	{
		config= new Configuration(file);
		
		String catagory;
		
		catagory = "IDs";
		config.addCustomCategoryComment(catagory, "Set ID's for each planet");

	}
	
	//CAMERA TILT TESTING
	
	  private static void loadConfig() {
	        CFMain.enableCameraTilt = config.getBoolean(
	            "enableCameraTilt", 
	            Configuration.CATEGORY_GENERAL, 
	            true,
	            "Set to false to disable camera tilt effect when strafing."
	        );

	        if (config.hasChanged()) {
	            config.save();
	        }
	        
}
}