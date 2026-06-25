//package fracture.mod.world.epchanges.callisto;
//
//import net.minecraft.world.biome.Biome;
//import net.minecraftforge.fml.common.registry.ForgeRegistries;
//import net.minecraftforge.fml.relauncher.ReflectionHelper;
//import java.lang.reflect.Field;
//
//public class CfCallistoBiomeTweaker {
//
//    public void tweakBiomes() {
//        for (Biome biome : ForgeRegistries.BIOMES) {
//            String name = biome.getRegistryName().toString();
//
//            if (name.equals("fracture:callisto_black_desert")) {
//                setBiomeHeights(biome, 0.4f, 0.3f); 
//            }
//            else if (name.equals("fracture:callisto_oil_shale")) {
//                setBiomeHeights(biome, 0.1f, 0.05f);
//            }
//            else if (name.equals("fracture:callisto_gravel_bed")) {
//                setBiomeHeights(biome, -0.5f, 0.0f);
//            }
//            else if (name.equals("fracture:callisto_rockies")) {
//                setBiomeHeights(biome, 1.2f, 0.3f); 
//            }
//            else if (name.equals("extraplanets:callisto")) {
//                setBiomeHeights(biome, 0.2f, 0.2f);
//            }
//        }
//    }
//
//    
//    //This may be redundant in current code version
//    private void setBiomeHeights(Biome biome, float minHeight, float maxHeight) {
//        try {
//            // field_76748_c is minHeight, field_76749_d is maxHeight in 1.12.2
//            Field minField = ReflectionHelper.findField(Biome.class, "minHeight", "field_76748_c");
//            Field maxField = ReflectionHelper.findField(Biome.class, "maxHeight", "field_76749_d");
//            
//            minField.setAccessible(true);
//            maxField.setAccessible(true);
//            
//            minField.set(biome, minHeight);
//            maxField.set(biome, maxHeight);
//        } catch (Exception e) { 
//            // Log specifically so we know if it still fails
//            System.err.println("[Fracture] Failed to tweak heights for: " + biome.getRegistryName());
//            e.printStackTrace(); 
//        }
//    }
//}