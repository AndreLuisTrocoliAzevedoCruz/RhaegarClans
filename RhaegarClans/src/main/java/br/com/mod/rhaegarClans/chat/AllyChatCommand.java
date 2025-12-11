package br.com.mod.rhaegarClans.chat;

import br.com.mod.rhaegarClans.core.Clan;
import br.com.mod.rhaegarClans.core.ClanConfig;
import br.com.mod.rhaegarClans.core.ClanManager;
import br.com.mod.rhaegarClans.RhaegarClans;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AllyChatCommand implements CommandExecutor {

    private final ClanManager clanManager;
    private final ClanConfig clanConfig;

    public AllyChatCommand(ClanManager clanManager, ClanConfig clanConfig) {
        this.clanManager = clanManager;
        this.clanConfig = clanConfig;
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
            player.sendMessage("§cVocê precisa estar em um clã para usar o chat de aliança (/ally).");
            return true;
        }

        // Verifica se o clã tem algum aliado
        if (playerClan.getAllies().isEmpty()) {
            player.sendMessage("§cSeu clã não possui nenhum aliado para usar o chat de aliança.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUso: /ally <mensagem>");
            player.sendMessage("§7Exemplo: /ally precisamos de ajuda na base!");
            player.sendMessage("§7Seus aliados: §a" + String.join("§7, §a", playerClan.getAllies()));
            return true;
        }

        // Constrói a mensagem
        StringBuilder messageBuilder = new StringBuilder();
        for (String arg : args) {
            messageBuilder.append(arg).append(" ");
        }
        String allyMessage = messageBuilder.toString().trim();

        // Determina a tag formatada e o cargo do jogador
        String clanTag = clanManager.getFormattedTag(playerClan);
        String rankPrefix = getRankPrefix(playerClan, player.getUniqueId());

        // Formato final da mensagem: [A] [TAG] [Cargo] Nick: Mensagem
        String formattedMessage = "§d[A] " + clanTag + " " + rankPrefix + player.getName() + ": §d" + allyMessage;

        // 1. Coleta todos os membros para enviar a mensagem (inclui o clã próprio)
        Set<UUID> recipients = new HashSet<>(playerClan.getMembers());

        // 2. Adiciona membros dos clãs aliados
        int allyClanCount = 0;
        for (String allyTag : playerClan.getAllies()) {
            Clan allyClan = clanConfig.getClanByCleanTag(allyTag);

            // Checagem extra de NPE (caso o clã aliado tenha sido deletado)
            if (allyClan != null) {
                recipients.addAll(allyClan.getMembers());
                allyClanCount++;
            }
        }

        // Contadores para estatísticas
        int totalRecipients = recipients.size();
        int onlineRecipients = 0;

        // 3. Envia a mensagem apenas para os membros online
        for (UUID memberId : recipients) {
            Player member = Bukkit.getPlayer(memberId);

            if (member != null && member.isOnline()) {
                member.sendMessage(formattedMessage);
                onlineRecipients++;
            }
        }

        // Feedback para o remetente
        if (onlineRecipients > 1) {
            String feedback = "§7[Mensagem enviada para §a" + onlineRecipients + "§7 jogadores online de " + allyClanCount + " aliados]";

            // Se o próprio jogador recebeu a mensagem (está online), mostra feedback
            if (player.isOnline()) {
                player.sendMessage(feedback);
            }
        }

        // Log para debugging
        RhaegarClans.getInstance().getLogger().info("[Chat-Aliança] " + player.getName() + " (" + playerClan.getTag() + "): " +
                allyMessage + " [Enviado para " + onlineRecipients + "/" + totalRecipients + " jogadores de " + allyClanCount + " aliados]");

        return true;
    }

    /**
     * Retorna o prefixo colorido do cargo do jogador.
     */
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