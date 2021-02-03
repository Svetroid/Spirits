package me.numin.spirits.command;

import com.projectkorra.projectkorra.command.PKCommand;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpiritsCommand extends PKCommand {

  public SpiritsCommand() {
    super("spirits", "/bending spirits", "Opens up Spirits guide.", new String[]{"s", "sp", "spirit", "spirits"});
  }

  @Override
  public void execute(CommandSender commandSender, List<String> list) {
    if (commandSender instanceof Player) {
      Player player = (Player) commandSender;

      player.sendMessage("You're a player!");
    } else {
      commandSender.sendMessage("You're not a player!");
    }
  }
}
