package fracture.mod.client.vhandlers;

import fracture.mod.CFInfo;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = CFInfo.ID, value = Side.CLIENT)
public class ScreenShakeManager {

	private static float intensity = 0.0f;
	private static final float MAX_INTENSITY = 10.0f;
	private static final float DECAY_RATE = 0.25f;

	private static float continuousBaseIntensity = 0.0f;
	private static float currentContinuousIntensity = 0.0f;
	private static int tickCounter = 0;

	public static void addShake(float amount) {
		intensity += amount;
		if (intensity > MAX_INTENSITY) {
			intensity = MAX_INTENSITY;
		}
	}

	public static void setContinuousShake(float amount) {
		continuousBaseIntensity = amount;
	}

	public static float getIntensity() {
		return Math.min(MAX_INTENSITY, intensity + currentContinuousIntensity);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			tickCounter++;

			if (intensity > 0.0f) {
				intensity -= DECAY_RATE;
				if (intensity < 0.0f) {
					intensity = 0.0f;
				}
			}

			if (continuousBaseIntensity > 0.0f) {
				float swell1 = (float) (Math.sin(tickCounter * 0.04f) * 0.5f + 0.5f);
				float swell2 = (float) (Math.cos(tickCounter * 0.025f) * 0.5f + 0.5f);

				float surgeMultiplier = 0.2f + (swell1 * swell2 * 0.8f);

				currentContinuousIntensity = continuousBaseIntensity * surgeMultiplier;
			} else {
				currentContinuousIntensity = 0.0f;
			}
		}
	}
}