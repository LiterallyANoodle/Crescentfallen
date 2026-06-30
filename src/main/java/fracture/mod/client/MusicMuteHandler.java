package fracture.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)

// Note: this currently is not working for galacticraft music. PLEASE fix this at some point it is SO annoying.

public class MusicMuteHandler {

    @SubscribeEvent
    public static void onSoundPlay(PlaySoundEvent event) {
        ISound sound = event.getSound();
        
        if (sound != null && sound.getCategory() == SoundCategory.MUSIC) {
            
            float musicVolume = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC);
            
            if (musicVolume <= 0.0F) {
                event.setResultSound(null);
            }
        }
    }
}