package br.com.mod.rhaegarClans.command;

import br.com.mod.rhaegarClans.core.Clan;
import br.com.mod.rhaegarClans.core.ClanConfig;
import br.com.mod.rhaegarClans.core.ClanManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClanDiplomacyCommand {

    private final ClanManager clanManager;
    private final ClanConfig clanConfig;

    public ClanDiplomacyCommand(ClanManager clanManager, ClanConfig clanConfig) {
        this.clanManager = clanManager;
        this.clanConfig = clanConfig;
    }

    /**
     * Executa a lógica para os comandos de diplomacia (ally, peace, war).
     */
    public boolean execute(Player player, String[] args) {
        Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
        UUID playerId = player.getUniqueId();
        String subCommand = args[0].toLowerCase();

        // 1. REQUER CLÃ
        if (playerClan == null) {
            player.sendMessage("§cVocê precisa estar em um clã para usar comandos de diplomacia.");
            return true;
        }

        // 2. REQUER PERMISSÃO DE GESTÃO
        if (!playerClan.hasManagementPermissions(playerId)) {
            player.sendMessage("§cVocê precisa ser Dono ou Líder para gerenciar relações diplomáticas.");
            return true;
        }

        // 3. VERIFICAÇÃO BÁSICA DE ARGUMENTOS
        if (args.length < 2) {
            sendUsageMessage(player, subCommand);
            return true;
        }

        String action = args[1].toLowerCase();

        // 4. VERIFICAÇÃO ESPECÍFICA PARA CADA AÇÃO
        if (action.equals("accept")) {
            if (args.length < 3) {
                player.sendMessage("§cUso: /clan " + subCommand + " accept <TagDoClã>");
                return true;
            }
        } else if (action.equals("start") || action.equals("end") || action.equals("stop")) {
            if (args.length < 3) {
                player.sendMessage("§cUso: /clan " + subCommand + " " + action + " <TagDoClã>");
                return true;
            }
        } else {
            sendUsageMessage(player, subCommand);
            return true;
        }

        // 5. LÓGICA DE ACEITE (ALLY / PEACE) - WAR não tem accept
        if (action.equals("accept")) {
            String targetTag = args[2];

            // War não tem accept
            if (subCommand.equals("war")) {
                player.sendMessage("§cO comando /clan war não possui ação 'accept'. Use 'start' ou 'end'.");
                return true;
            }

            return handleAcceptAction(player, playerClan, subCommand, targetTag);
        }

        // 6. AÇÕES QUE REQUEREM CLÃ ALVO (start/end)
        if (args.length < 3) {
            player.sendMessage("§cUso: /clan " + subCommand + " " + action + " <TagDoClã>");
            return true;
        }

        String targetTag = args[2];
        Clan targetClan = clanConfig.getClanByCleanTag(targetTag);

        if (targetClan == null) {
            player.sendMessage("§cClã com a Tag §e" + targetTag + "§c não encontrado.");
            return true;
        }

        // Não pode ser o próprio clã
        if (playerClan.getTag().equals(targetClan.getTag())) {
            player.sendMessage("§cVocê não pode fazer diplomacia com o seu próprio clã.");
            return true;
        }

        // 7. EXECUTA A AÇÃO
        if (action.equals("start")) {
            return handleStartDiplomacy(player, playerClan, targetClan, subCommand);
        } else if (action.equals("end") || action.equals("stop")) {
            return handleEndDiplomacy(player, playerClan, targetClan, subCommand);
        } else {
            sendUsageMessage(player, subCommand);
            return true;
        }
    }

    // --- MÉTODOS PRIVADOS DE APOIO ---

    private void sendUsageMessage(Player player, String subCommand) {
        if (subCommand.equals("ally") || subCommand.equals("peace")) {
            player.sendMessage("§cUso: /clan " + subCommand + " <start/accept/end> <TagDoClã>");
        } else if (subCommand.equals("war")) {
            player.sendMessage("§cUso: /clan war <start/end> <TagDoClã>");
        }
    }

    private boolean handleAcceptAction(Player player, Clan playerClan, String subCommand, String targetTag) {
        Clan targetClan = clanConfig.getClanByCleanTag(targetTag);
        if (targetClan == null) {
            player.sendMessage("§cClã com a Tag §e" + targetTag + "§c não encontrado.");
            return true;
        }

        if (subCommand.equals("ally")) {
            if (!playerClan.hasPendingAllyInvite(targetClan.getTag())) {
                player.sendMessage("§cO clã " + clanManager.getFormattedTag(targetClan) + "§c não te enviou um convite de aliança.");
                return true;
            }

            // Remove convite e estabelece aliança
            playerClan.removePendingAllyInvite(targetClan.getTag());
            playerClan.addAlly(targetClan.getTag());
            targetClan.addAlly(playerClan.getTag());

            clanManager.saveClan(playerClan);
            clanManager.saveClan(targetClan);

            player.sendMessage("§aVocê §bACEITOU§a a aliança com o clã " + clanManager.getFormattedTag(targetClan) + "§a.");
            clanManager.notifyClan(targetClan, "§bO clã " + clanManager.getFormattedTag(playerClan) + "§b ACEITOU sua proposta de Aliança!");
            clanManager.notifyClan(playerClan, "§bO clã " + clanManager.getFormattedTag(targetClan) + "§b aceitou sua proposta de Aliança!");
            return true;

        } else if (subCommand.equals("peace")) {
            if (!playerClan.hasPendingPeaceInvite(targetClan.getTag())) {
                player.sendMessage("§cO clã " + clanManager.getFormattedTag(targetClan) + "§c não te enviou uma proposta de Tratado de Paz.");
                return true;
            }

            // Remove convite e estabelece paz
            playerClan.removePendingPeaceInvite(targetClan.getTag());
            playerClan.addPeaceTreaty(targetClan.getTag());
            targetClan.addPeaceTreaty(playerClan.getTag());

            clanManager.saveClan(playerClan);
            clanManager.saveClan(targetClan);

            player.sendMessage("§aVocê §2ACEITOU§a o Tratado de Não-Agressão com o clã " + clanManager.getFormattedTag(targetClan) + "§a.");
            clanManager.notifyClan(targetClan, "§2O clã " + clanManager.getFormattedTag(playerClan) + "§2 ACEITOU sua proposta de Tratado de Paz!");
            clanManager.notifyClan(playerClan, "§2O clã " + clanManager.getFormattedTag(targetClan) + "§2 aceitou sua proposta de Tratado de Paz!");
            return true;
        }

        return false;
    }

    private boolean handleStartDiplomacy(Player player, Clan playerClan, Clan targetClan, String subCommand) {
        String formattedTargetTag = clanManager.getFormattedTag(targetClan);

        if (subCommand.equals("ally")) {
            if (playerClan.isAlly(targetClan.getTag())) {
                player.sendMessage("§cO clã §e" + targetClan.getTag() + "§c já é seu aliado.");
                return true;
            }
            if (targetClan.hasPendingAllyInvite(playerClan.getTag())) {
                player.sendMessage("§cVocê já enviou um convite de aliança para este clã.");
                return true;
            }

            targetClan.addPendingAllyInvite(playerClan.getTag());
            clanManager.saveClan(targetClan);

            player.sendMessage("§aVocê enviou uma proposta de §bAliança§a para o clã " + formattedTargetTag + "§a.");
            clanManager.notifyClan(targetClan, "§bO clã " + clanManager.getFormattedTag(playerClan) + "§b deseja fazer uma aliança com você!");
            clanManager.notifyClan(targetClan, "§eUse §b/clan ally accept " + playerClan.getTag() + "§e para aceitar.");
            return true;

        } else if (subCommand.equals("peace")) {
            if (playerClan.hasPeaceTreaty(targetClan.getTag())) {
                player.sendMessage("§cVocê já tem um Tratado de Não-Agressão com o clã §e" + targetClan.getTag() + "§c.");
                return true;
            }
            if (targetClan.hasPendingPeaceInvite(playerClan.getTag())) {
                player.sendMessage("§cVocê já enviou uma proposta de Tratado de Paz para este clã.");
                return true;
            }

            targetClan.addPendingPeaceInvite(playerClan.getTag());
            clanManager.saveClan(targetClan);

            player.sendMessage("§aVocê enviou uma proposta de §2Tratado de Não-Agressão§a para o clã " + formattedTargetTag + "§a.");
            clanManager.notifyClan(targetClan, "§2O clã " + clanManager.getFormattedTag(playerClan) + "§2 deseja iniciar um Tratado de Não-Agressão com o seu clã!");
            clanManager.notifyClan(targetClan, "§eUse §2/clan peace accept " + playerClan.getTag() + "§e para aceitar.");
            return true;

        } else if (subCommand.equals("war")) {
            if (playerClan.isAtWarWith(targetClan.getTag())) {
                player.sendMessage("§cVocê já está em guerra com o clã §e" + targetClan.getTag() + "§c.");
                return true;
            }

            playerClan.declareWar(targetClan.getTag());
            targetClan.declareWar(playerClan.getTag());

            clanManager.saveClan(playerClan);
            clanManager.saveClan(targetClan);

            player.sendMessage("§cVocê §4DECLAROU GUERRA§c ao clã " + formattedTargetTag + "§c!");
            clanManager.notifyClan(targetClan, "§4GUERRA! O clã " + clanManager.getFormattedTag(playerClan) + "§4 declarou guerra ao seu clã!");
            return true;
        }

        return false;
    }

    private boolean handleEndDiplomacy(Player player, Clan playerClan, Clan targetClan, String subCommand) {
        String formattedTargetTag = clanManager.getFormattedTag(targetClan);

        if (subCommand.equals("ally")) {
            if (!playerClan.isAlly(targetClan.getTag())) {
                player.sendMessage("§cO clã §e" + targetClan.getTag() + "§c não é seu aliado.");
                return true;
            }

            playerClan.removeAlly(targetClan.getTag());
            targetClan.removeAlly(playerClan.getTag());

            clanManager.saveClan(playerClan);
            clanManager.saveClan(targetClan);

            player.sendMessage("§aVocê desfez a §bAliança§a com o clã " + formattedTargetTag + "§a.");
            clanManager.notifyClan(targetClan, "§bO clã " + clanManager.getFormattedTag(playerClan) + "§b desfez a Aliança com o seu clã.");
            return true;

        } else if (subCommand.equals("peace")) {
            if (!playerClan.hasPeaceTreaty(targetClan.getTag())) {
                player.sendMessage("§cO clã §e" + targetClan.getTag() + "§c não tem tratado de não-agressão com o seu.");
                return true;
            }

            playerClan.removePeaceTreaty(targetClan.getTag());
            targetClan.removePeaceTreaty(playerClan.getTag());

            clanManager.saveClan(playerClan);
            clanManager.saveClan(targetClan);

            player.sendMessage("§aVocê finalizou o §2Tratado de Não-Agressão§a com o clã " + formattedTargetTag + "§a.");
            clanManager.notifyClan(targetClan, "§2O clã " + clanManager.getFormattedTag(playerClan) + "§2 finalizou o Tratado de Não-Agressão.");
            return true;

        } else if (subCommand.equals("war")) {
            if (!playerClan.isAtWarWith(targetClan.getTag())) {
                player.sendMessage("§cO clã §e" + targetClan.getTag() + "§c não está em guerra com o seu.");
                return true;
            }

            playerClan.endWar(targetClan.getTag());
            targetClan.endWar(playerClan.getTag());

            clanManager.saveClan(playerClan);
            clanManager.saveClan(targetClan);

            player.sendMessage("§aVocê finalizou a §4Guerra§a contra o clã " + formattedTargetTag + "§a.");
            clanManager.notifyClan(targetClan, "§aO clã " + clanManager.getFormattedTag(playerClan) + "§a finalizou a Guerra.");
            return true;
        }

        return false;
    }

    // --- TAB COMPLETION ---

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        String subCommand = args[0].toLowerCase();

        if (args.length == 2) {
            if (subCommand.equals("ally") || subCommand.equals("peace")) {
                return List.of("start", "accept", "end").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (subCommand.equals("war")) {
                return List.of("start", "end").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            if (subCommand.equals("ally") || subCommand.equals("peace") || subCommand.equals("war")) {
                // Sugere tags de clãs
                return clanConfig.getLoadedClans().keySet().stream()
                        .filter(tag -> {
                            String cleanTag = clanConfig.getCleanTag(tag);
                            String cleanInput = clanConfig.getCleanTag(args[2]);
                            return cleanTag.toLowerCase().startsWith(cleanInput.toLowerCase());
                        })
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}