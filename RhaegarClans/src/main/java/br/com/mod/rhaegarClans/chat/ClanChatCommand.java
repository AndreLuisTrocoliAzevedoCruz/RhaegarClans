package br.com.mod.rhaegarClans.chat;

import br.com.mod.rhaegarClans.core.Clan;
import br.com.mod.rhaegarClans.core.ClanManager;
import br.com.mod.rhaegarClans.RhaegarClans;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClanChatCommand implements CommandExecutor {

    private final ClanManager clanManager;

    public ClanChatCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado por jogadores.");
            return true;
        }

        Player player = (Player) sender;
        Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());

        if (playerClan == null) {
            player.sendMessage("§cVocê precisa estar em um clã para usar o chat de clã (/.).");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUso: /. <mensagem>");
            player.sendMessage("§7Exemplo: /. vamos atacar agora!");
            return true;
        }

        // Constrói a mensagem a partir de todos os argumentos
        StringBuilder messageBuilder = new StringBuilder();
        for (String arg : args) {
            messageBuilder.append(arg).append(" ");
        }
        String clanMessage = messageBuilder.toString().trim();

        // Determina o cargo do jogador para a formatação
        String rankPrefix = getRankPrefix(playerClan, player.getUniqueId());

        // Formato final da mensagem: [C] [Cargo] Nick: Mensagem
        String formattedMessage = "§9[C] " + rankPrefix + player.getName() + ": §a" + clanMessage;

        // Envia a mensagem para todos os membros online do clã
        int sentCount = 0;
        for (UUID memberId : playerClan.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);

            if (member != null && member.isOnline()) {
                member.sendMessage(formattedMessage);
                sentCount++;
            }
        }

        // Log da mensagem no console (opcional)
        if (sentCount > 1) {
            RhaegarClans.getInstance().getLogger().info("[Chat-Clã] " + player.getName() + " (" + playerClan.getTag() + "): " + clanMessage + " [Enviado para " + sentCount + " membros]");
        } else {
            RhaegarClans.getInstance().getLogger().info("[Chat-Clã] " + player.getName() + " (" + playerClan.getTag() + "): " + clanMessage);
        }

        return true;
    }

    private String getRankPrefix(Clan clan, UUID playerId) {
        if (clan.isOwner(playerId)) {
            return ChatColor.WHITE + "[Dono] ";
        } else if (clan.isLeader(playerId)) {
            return ChatColor.RED + "[Líder] ";
        } else {
            return ChatColor.GREEN + "[Membro] ";
        }
    }
}