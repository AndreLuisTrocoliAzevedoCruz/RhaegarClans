package br.com.mod.rhaegarClans.command;

import br.com.mod.rhaegarClans.*;
import br.com.mod.rhaegarClans.core.ClanConfig;
import br.com.mod.rhaegarClans.core.ClanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor, TabCompleter {

    private final ClanManager clanManager;
    private final ClanConfig clanConfig;

    // Novas classes especializadas
    private final ClanCoreCommand coreCommand;
    private final ClanDiplomacyCommand diplomacyCommand;

    public ClanCommand() {
        RhaegarClans plugin = RhaegarClans.getInstance();
        this.clanManager = plugin.getClanManager();
        this.clanConfig = plugin.getClanConfig();

        // Inicializa as classes especializadas
        this.coreCommand = new ClanCoreCommand(clanManager, clanConfig);
        this.diplomacyCommand = new ClanDiplomacyCommand(clanManager, clanConfig);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Se não há argumentos, mostra a ajuda
            coreCommand.sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // --- ROTEAMENTO DA LÓGICA POR SUBCOMANDO ---

        // Comandos de Diplomacia
        if (subCommand.equals("ally") || subCommand.equals("peace") || subCommand.equals("war")) {
            return diplomacyCommand.execute(player, args);
        }

        // Comandos de Informação e Criação (profile, roster, create)
        if (subCommand.equals("profile") || subCommand.equals("roster") || subCommand.equals("membros") || subCommand.equals("create") ||
                subCommand.equals("help") || subCommand.equals("ajuda")) {
            return coreCommand.execute(player, args);
        }

        // Comandos de Gestão (leave, disband, invite, kick, promote, demote, posse, accept)
        if (subCommand.equals("leave") || subCommand.equals("sair") || subCommand.equals("disband") ||
                subCommand.equals("invite") || subCommand.equals("accept") || subCommand.equals("kick") ||
                subCommand.equals("promote") || subCommand.equals("demote") || subCommand.equals("posse")) {

            return coreCommand.execute(player, args);
        }

        player.sendMessage("§cComando de clã inválido. Use /clan para ver os comandos.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return List.of("create", "invite", "kick", "promote", "demote", "posse", "accept", "leave", "disband",
                            "profile", "roster", "ally", "peace", "war", "help", "ajuda").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Delega o TabCompletion para as classes filhas
        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("ally") || subCommand.equals("peace") || subCommand.equals("war")) {
            return diplomacyCommand.onTabComplete(sender, args);
        }

        return coreCommand.onTabComplete(sender, args);
    }
}