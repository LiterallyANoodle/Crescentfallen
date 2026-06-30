package fracture.mod.world.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class EventStateManager extends WorldSavedData {
    private static final String DATA_NAME = "crescentfallen_event_data";
    
    private int eventTicks = 0;
    private int currentState = 0;
    public static final int MAX_TICKS = 72000; 

    public EventStateManager(String name) {
        super(name);
    }

    public EventStateManager() {
        super(DATA_NAME);
    }

    public static EventStateManager get(World world) {
        MapStorage storage;
        
        // If on the client, use client's MapStorage
        // If on the server, grab Dimension 0's MapStorage
        if (world.isRemote) {
            storage = world.getMapStorage();
        } else {
            storage = world.getMinecraftServer().getWorld(0).getMapStorage();
        }

        EventStateManager instance = (EventStateManager) storage.getOrLoadData(EventStateManager.class, DATA_NAME);

        if (instance == null) {
            instance = new EventStateManager();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }

    public int getEventTicks() { return eventTicks; }
    
    public void setEventTicks(int ticks) { 
        this.eventTicks = ticks; 
        this.markDirty(); 
    }
    
    public void addTick() { 
        this.eventTicks++; 
        this.markDirty(); 
    }

    public int getCurrentState() { return currentState; }
    
    public void setCurrentState(int state) { 
        this.currentState = state; 
        this.markDirty(); 
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.eventTicks = nbt.getInteger("EventTicks");
        this.currentState = nbt.getInteger("CurrentState");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("EventTicks", this.eventTicks);
        compound.setInteger("CurrentState", this.currentState);
        return compound;
    }
}