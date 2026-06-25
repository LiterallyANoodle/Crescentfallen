package fracture.mod.client.vhandlers;

import fracture.mod.client.vhandlers.ScreenShakeManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketContinuousShake implements IMessage {
    
    private float intensity;

    public PacketContinuousShake() {}

    public PacketContinuousShake(float intensity) {
        this.intensity = intensity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.intensity = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(this.intensity);
    }

    public static class Handler implements IMessageHandler<PacketContinuousShake, IMessage> {
        @Override
        public IMessage onMessage(PacketContinuousShake message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    ScreenShakeManager.setContinuousShake(message.intensity);
                });
            }
            return null;
        }
    }
}