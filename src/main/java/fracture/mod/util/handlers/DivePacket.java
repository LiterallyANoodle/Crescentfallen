package fracture.mod.util.handlers;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DivePacket implements IMessage {
    private float forward, strafe;

    public DivePacket() {} 

    public DivePacket(float forward, float strafe) {
        this.forward = forward;
        this.strafe = strafe;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        forward = buf.readFloat();
        strafe = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(forward);
        buf.writeFloat(strafe);
    }

    public float getForward() { return forward; }
    public float getStrafe() { return strafe; }

    public static class Handler implements IMessageHandler<DivePacket, IMessage> {
        @Override
        public IMessage onMessage(DivePacket message, MessageContext ctx) {
            ctx.getServerHandler().player.server.addScheduledTask(() -> {
                fracture.mod.util.handlers.DiveHandler.performServerDiveStats(
                        ctx.getServerHandler().player
                );
            });
            return null;
        }
    }
}