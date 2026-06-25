package fracture.mod.network;

import fracture.mod.client.render.BombardmentRenderer;
import fracture.mod.client.vhandlers.ScreenShakeManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketBombardment implements IMessage {

    private double targetX, targetY, targetZ;
    private double originX, originY, originZ;

    public PacketBombardment() {
    }

    public PacketBombardment(double tX, double tY, double tZ, double oX, double oY, double oZ) {
        this.targetX = tX;
        this.targetY = tY;
        this.targetZ = tZ;
        this.originX = oX;
        this.originY = oY;
        this.originZ = oZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.targetX = buf.readDouble();
        this.targetY = buf.readDouble();
        this.targetZ = buf.readDouble();
        this.originX = buf.readDouble();
        this.originY = buf.readDouble();
        this.originZ = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.targetX);
        buf.writeDouble(this.targetY);
        buf.writeDouble(this.targetZ);
        buf.writeDouble(this.originX);
        buf.writeDouble(this.originY);
        buf.writeDouble(this.originZ);
    }

    public static class Handler implements IMessageHandler<PacketBombardment, IMessage> {
        @Override
        public IMessage onMessage(PacketBombardment message, MessageContext ctx) {
            if (ctx.side != Side.CLIENT) return null;

            Minecraft.getMinecraft().addScheduledTask(() -> {
                
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.world == null || mc.player == null) return;

                double dist = mc.player.getDistance(message.targetX, message.targetY, message.targetZ);
                double shakeRadius = 1.0D; 
                
                if (dist <= shakeRadius) {
                    float intensity = (float) (1.0D - (dist / shakeRadius)) * 7.0F; 
                    ScreenShakeManager.addShake(intensity);
                }

                BombardmentRenderer.addLaser(
                        message.originX, message.originY, message.originZ, 
                        message.targetX, message.targetY, message.targetZ  
                );
            });

            return null; // No reply packet needed
        }
    }
}