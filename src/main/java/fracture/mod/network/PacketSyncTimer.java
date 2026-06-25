//package fracture.mod.network;
//
////import fracture.mod.world.data.DestructionEventData;
//import io.netty.buffer.ByteBuf;
//import net.minecraft.client.Minecraft;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
//import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
//
//public class PacketSyncTimer implements IMessage {
//    
//    private int timerTicks;
//
//    public PacketSyncTimer() {} // Required empty constructor
//
//    public PacketSyncTimer(int timerTicks) {
//        this.timerTicks = timerTicks;
//    }
//
//    @Override
//    public void fromBytes(ByteBuf buf) {
//        this.timerTicks = buf.readInt();
//    }
//
//    @Override
//    public void toBytes(ByteBuf buf) {
//        buf.writeInt(this.timerTicks);
//    }
//
//    public static class Handler implements IMessageHandler<PacketSyncTimer, IMessage> {
//        @Override
//        public IMessage onMessage(PacketSyncTimer message, MessageContext ctx) {
//            Minecraft.getMinecraft().addScheduledTask(() -> {
//                if (Minecraft.getMinecraft().world != null) {
//                    DestructionEventData data = DestructionEventData.get(Minecraft.getMinecraft().world);
//                    data.setTimer(message.timerTicks);
//                }
//            });
//            return null;
//        }
//    }
//}