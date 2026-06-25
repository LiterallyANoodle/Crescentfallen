package fracture.mod.commands;

import fracture.mod.CFMain;
import fracture.mod.client.vhandlers.PacketContinuousShake;
import fracture.mod.client.vhandlers.PacketScreenShake;
import fracture.mod.util.CustomExplosion;
// import fracture.mod.world.data.DestructionEventData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandCF extends CommandBase {

    @Override
    public String getName() {
        return "cf";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cf <screenshake|explode>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        String subCommand = args[0].toLowerCase();

        // screenshake
        if (subCommand.equals("screenshake")) {
            if (args.length < 2) {
                throw new WrongUsageException("Usage: /cf screenshake <intensity> OR /cf screenshake continuous <intensity|off>");
            }
            
            if (args[1].equalsIgnoreCase("continuous")) {
                if (args.length < 3) {
                    throw new WrongUsageException("Usage: /cf screenshake continuous <number> OR /cf screenshake continuous off");
                }

                float intensity = 0.0f; 
                if (!args[2].equalsIgnoreCase("off")) {
                    intensity = (float) parseDouble(args[2]);
                }

                CFMain.NETWORK.sendTo(new PacketContinuousShake(intensity), player);
                player.sendMessage(new TextComponentString("screenshake updated"));

            } else {
                float intensity = (float) parseDouble(args[1]);
                CFMain.NETWORK.sendTo(new PacketScreenShake(intensity), player);
                player.sendMessage(new TextComponentString("screenshake updated"));
            }
        } 
        
        // explode
        else if (subCommand.equals("explode")) {
            CustomExplosion.ExplosionType type = CustomExplosion.ExplosionType.DEFAULT;
            
            if (args.length >= 2) {
                try {
                    type = CustomExplosion.ExplosionType.valueOf(args[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new WrongUsageException("Invalid explosion type. Available: default, holy");
                }
            }

            CustomExplosion.triggerExplosion(
                    player.world, 
                    player, 
                    player.posX, 
                    player.posY, 
                    player.posZ, 
                    5.0F,   // radius
                    1.0F,   // knockback
                    false,  // damagesBlocks
                    false,  // causesScreenShake -> OFF for test command
                    type    // type
            );

            player.sendMessage(new TextComponentString(TextFormatting.RED + "Explosion (" + type.name() + ") triggered."));
        } 
        
        // --- UNKNOWN ---
        else {
            throw new WrongUsageException("Unknown CF command. " + getUsage(sender));
        }
    }

    // --- TAB COMPLETION LOGIC ---
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        
        // Level 1: /cf <tab>
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "screenshake", "explode");
        } 
        
        // Level 2: /cf command <tab>
        else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("screenshake")) {
                // Hinting some numbers + continuous
                return getListOfStringsMatchingLastWord(args, "continuous", "1.0", "5.0");
            } 
            else if (sub.equals("explode")) {
                return getListOfStringsMatchingLastWord(args, "default", "holy");
            }
        }
        
        // Level 3: /cf command sub-argument <tab>
        else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("screenshake") && args[1].equalsIgnoreCase("continuous")) {
                return getListOfStringsMatchingLastWord(args, "off", "1.0", "5.0");
            }
        }

        return Collections.emptyList();
    }
}