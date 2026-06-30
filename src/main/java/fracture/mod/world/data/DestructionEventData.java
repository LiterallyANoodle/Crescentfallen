//package fracture.mod.world.data;
//
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.world.World;
//import net.minecraft.world.storage.MapStorage;
//import net.minecraft.world.storage.WorldSavedData;
//
//public class DestructionEventData extends WorldSavedData {
//
//    private static final String DATA_NAME = "CF_DestructionEventData";
//    
//    // Timer tracks from 0 to 72000 (3 in-game days)
//    private int destructionTimer = 0; 
//
//    public DestructionEventData(String name) {
//        super(name);
//    }
//
//    public DestructionEventData() {
//        super(DATA_NAME);
//    }
//
//    @Override
//    public void readFromNBT(NBTTagCompound nbt) {
//        this.destructionTimer = nbt.getInteger("DestructionTimer");
//    }
//
//    @Override
//    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
//        nbt.setInteger("DestructionTimer", this.destructionTimer);
//        return nbt;
//    }
//    
//    public void setTimer(int ticks) {
//        this.destructionTimer = Math.max(0, Math.min(ticks, 72000));
//        this.markDirty();
//    }
//
//    public int getTimer() {
//        return this.destructionTimer;
//    }
//
//    public void incrementTimer() {
//        this.destructionTimer++;
//        this.markDirty(); // Tells Forge to save this data during the next world save tick
//    }
//
//    /**
//     * Retrieves the singleton instance of this data from the world.
//     */
//    public static DestructionEventData get(World world) {
//        // getMapStorage() ensures this is saved to the global data folder, not just a specific dimension folder
//        MapStorage storage = world.getMapStorage(); 
//        DestructionEventData instance = (DestructionEventData) storage.getOrLoadData(DestructionEventData.class, DATA_NAME);
//
//        if (instance == null) {
//            instance = new DestructionEventData();
//            storage.setData(DATA_NAME, instance);
//        }
//        return instance;
//    }
//}