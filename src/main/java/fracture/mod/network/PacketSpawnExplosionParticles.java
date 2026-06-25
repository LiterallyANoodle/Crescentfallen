package fracture.mod.network;

import fracture.mod.util.CustomExplosion;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketSpawnExplosionParticles implements IMessage {
    
    private double x, y, z;
    private float radius;
    private boolean causesScreenShake;

    public PacketSpawnExplosionParticles() {}

    public PacketSpawnExplosionParticles(double x, double y, double z, float radius, boolean causesScreenShake) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.causesScreenShake = causesScreenShake;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.radius = buf.readFloat();
        this.causesScreenShake = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.radius);
        buf.writeBoolean(this.causesScreenShake);
    }

    public static class Handler implements IMessageHandler<PacketSpawnExplosionParticles, IMessage> {
        @Override
        public IMessage onMessage(PacketSpawnExplosionParticles message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    World world = Minecraft.getMinecraft().world;
                    if (world != null) {
                        CustomExplosion visualExplosion = new CustomExplosion(world, null, message.x, message.y, message.z, message.radius)
                                .setCausesScreenShake(message.causesScreenShake);
                        visualExplosion.spawnParticles();
                    }
                });
            }
            return null;
        }
    }
}