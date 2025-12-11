package br.com.mod.rhaegarClans.command;

import br.com.mod.rhaegarClans.core.Clan;
import br.com.mod.rhaegarClans.core.ClanConfig;
import br.com.mod.rhaegarClans.core.ClanManager;
import br.com.mod.rhaegarClans.RhaegarClans;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;

public class ClanCoreCommand {

    private final ClanManager clanManager;
    private final ClanConfig clanConfig;

    // Constantes de Limite
    private static final int MIN_TAG_LENGTH = 2;
    private static final int MAX_TAG_LENGTH = 4;
    private static final int MIN_NAME_LENGTH = 4;
    private static final int MAX_NAME_LENGTH = 16;
    private static final int MAX_MEMBERS = 30; // Limite máximo de membros por clã

    // Construtor para receber as dependências
    public ClanCoreCommand(ClanManager clanManager, ClanConfig clanConfig) {
        this.clanManager = clanManager;
        this.clanConfig = clanConfig;
    }

    /**
     * Envia mensagem de ajuda completa
     */
    public void sendHelpMessage(Player player) {
        player.sendMessage("§7" + "=".repeat(40));
        player.sendMessage("§a📖 Central de Ajuda - Sistema de Clãs");
        player.sendMessage("§7" + "-".repeat(40));
        player.sendMessage("§e📋 Básicos:");
        player.sendMessage(" §f/clan create <tag> <nome> §7- Cria um novo clã");
        player.sendMessage(" §f/clan accept §7- Aceita convite pendente");
        player.sendMessage(" §f/clan profile [tag] §7- Vê perfil de um clã");
        player.sendMessage(" §f/clan roster [tag] §7- Lista membros de um clã");
        player.sendMessage(" §f/clan info §7- Informações do seu clã");
        player.sendMessage(" §f/clan leave [confirm] §7- Sai do clã (exceto Dono)");

        player.sendMessage("§e👑 Gestão (Líder+):");
        player.sendMessage(" §f/clan invite <jogador> §7- Convida um jogador");
        player.sendMessage(" §f/clan kick <jogador> §7- Expulsa um membro");

        player.sendMessage("§e👑 Gestão (Dono+):");
        player.sendMessage(" §f/clan disband [confirm] §7- Desmembra o clã");
        player.sendMessage(" §f/clan promote <jogador> §7- Promove a Líder");
        player.sendMessage(" §f/clan demote <jogador> §7- Rebaixa de Líder");
        player.sendMessage(" §f/clan posse <jogador> [confirm] §7- Transfere a posse");
        player.sendMessage(" §f/clan rename <nome> §7- Renomeia o clã");
        player.sendMessage(" §f/clan settag <tag> §7- Altera a Tag");

        player.sendMessage("§e🤝 Diplomacia:");
        player.sendMessage(" §f/clan ally <start/accept/end> <tag> §7- Gerencia Alianças");
        player.sendMessage(" §f/clan peace <start/accept/end> <tag> §7- Gerencia Paz");
        player.sendMessage(" §f/clan war <start/end> <tag> §7- Gerencia Guerras");

        player.sendMessage("§e💬 Chats:");
        player.sendMessage(" §f/. <mensagem> §7- Chat do clã");
        player.sendMessage(" §f/ally <mensagem> §7- Chat da aliança");

        player.sendMessage("§7" + "=".repeat(40));
    }

    /**
     * Executa a lógica para os comandos de criação, informação e gestão.
     */
    public boolean execute(Player player, String[] args) {
        Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
        UUID playerId = player.getUniqueId();
        String subCommand = args[0].toLowerCase();

        // --- 1. COMANDOS SEM CLÃ / BÁSICOS ---

        // /CLAN CREATE
        if (subCommand.equals("create")) {
            return handleCreateCommand(player, playerClan, args);
        }

        // /CLAN ACCEPT
        if (subCommand.equals("accept")) {
            return handleAcceptCommand(player, playerClan);
        }

        // /CLAN HELP
        if (subCommand.equals("help") || subCommand.equals("ajuda")) {
            sendHelpMessage(player);
            return true;
        }

        // /CLAN PROFILE [TAG]
        if (subCommand.equals("profile")) {
            return handleProfileCommand(player, playerClan, args);
        }

        // /CLAN ROSTER / MEMBROS
        if (subCommand.equals("roster") || subCommand.equals("membros")) {
            return handleRosterCommand(player, playerClan, args);
        }

        // /CLAN INFO
        if (subCommand.equals("info")) {
            return handleInfoCommand(player, playerClan);
        }

        // --- 2. COMANDOS QUE REQUEREM CLÃ (LEAVE, KICK, PROMOTE, etc.) ---
        if (playerClan == null) {
            player.sendMessage("§cVocê precisa estar em um clã para usar o comando: /clan " + subCommand);
            return true;
        }

        // /CLAN LEAVE (SAIR)
        if (subCommand.equals("leave") || subCommand.equals("sair")) {
            return handleLeaveCommand(player, playerClan, args);
        }

        // /CLAN DISBAND (DESMEMBRAR)
        if (subCommand.equals("disband")) {
            return handleDisbandCommand(player, playerClan, args);
        }

        // --- COMANDOS QUE REQUEREM PERMISSÃO DE GESTÃO (Dono/Líder) ---
        if (!playerClan.hasManagementPermissions(playerId)) {
            player.sendMessage("§cVocê precisa ser Dono ou Líder para usar este comando.");
            return true;
        }

        // /CLAN INVITE
        if (subCommand.equals("invite")) {
            return handleInviteCommand(player, playerClan, args);
        }

        // /CLAN KICK (EXPULSAR)
        if (subCommand.equals("kick")) {
            return handleKickCommand(player, playerClan, args);
        }

        // /CLAN PROMOTE
        if (subCommand.equals("promote")) {
            return handlePromoteCommand(player, playerClan, args);
        }

        // /CLAN DEMOTE
        if (subCommand.equals("demote")) {
            return handleDemoteCommand(player, playerClan, args);
        }

        // /CLAN POSSE (Troca de Dono)
        if (subCommand.equals("posse")) {
            return handlePosseCommand(player, playerClan, args);
        }

        // /CLAN RENAME
        if (subCommand.equals("rename")) {
            return handleRenameCommand(player, playerClan, args);
        }

        // /CLAN SETTAG
        if (subCommand.equals("settag") || subCommand.equals("tag")) {
            return handleSetTagCommand(player, playerClan, args);
        }

        player.sendMessage("§cComando de clã inválido. Use /clan help para ver a lista de comandos.");
        return true;
    }

    // ============ MÉTODOS DE MANIPULAÇÃO DE COMANDOS ============

    private boolean handleCreateCommand(Player player, Clan playerClan, String[] args) {
        if (playerClan != null) {
            player.sendMessage("§cVocê já está em um clã! Saia primeiro para criar um novo.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§cUso: /clan create <tag> <nome>");
            player.sendMessage("§7A Tag deve ter 2-4 caracteres, o Nome 4-16 caracteres.");
            player.sendMessage("§7Use & para cores (ex: &6&lTST)");
            return true;
        }

        String tag = args[1];
        String name = args[2];

        // Converter & para § para processamento interno
        String processedTag = tag.replace('&', '§');
        String processedName = name.replace('&', '§');

        // Obter versão sem cores para validação de comprimento
        String cleanTag = ChatColor.stripColor(processedTag);
        String cleanName = ChatColor.stripColor(processedName);

        // Validação da Tag (sem cores)
        if (cleanTag.length() < MIN_TAG_LENGTH || cleanTag.length() > MAX_TAG_LENGTH) {
            player.sendMessage("§cA Tag do clã deve ter entre " + MIN_TAG_LENGTH + " e " + MAX_TAG_LENGTH + " caracteres (sem contar as cores).");
            player.sendMessage("§7Tag atual sem cores: '" + cleanTag + "' (" + cleanTag.length() + " caracteres)");
            return true;
        }

        // Validação do Nome (sem cores)
        if (cleanName.length() < MIN_NAME_LENGTH || cleanName.length() > MAX_NAME_LENGTH) {
            player.sendMessage("§cO nome do clã deve ter entre " + MIN_NAME_LENGTH + " e " + MAX_NAME_LENGTH + " caracteres (sem contar as cores).");
            player.sendMessage("§7Nome atual sem cores: '" + cleanName + "' (" + cleanName.length() + " caracteres)");
            return true;
        }

        // Converter de volta para & para armazenamento
        String storedTag = processedTag.replace('§', '&');
        String storedName = processedName.replace('§', '&');

        // Verificar se a Tag já existe (sem cores)
        if (clanConfig.tagExists(storedTag)) {
            player.sendMessage("§cJá existe um clã com a Tag '" + cleanTag + "'.");
            return true;
        }

        // Criação do Clã
        Clan newClan = new Clan(player.getUniqueId(), storedName, storedTag);
        clanManager.saveClan(newClan);

        // Atualiza o cache do jogador após a criação
        clanManager.refreshPlayerCache(player.getUniqueId());

        player.sendMessage("§aVocê criou o clã " + clanManager.getFormattedTag(newClan) + "§a com sucesso!");
        player.sendMessage("§7Tag armazenada: " + storedTag);
        player.sendMessage("§7Use §f/clan invite <jogador> §7para convidar membros.");
        return true;
    }

    private boolean handleAcceptCommand(Player player, Clan playerClan) {
        if (playerClan != null) {
            player.sendMessage("§cVocê já está em um clã. Saia primeiro.");
            return true;
        }

        Clan invitedClan = clanManager.getPendingInvite(player.getUniqueId());
        if (invitedClan == null) {
            player.sendMessage("§cVocê não tem convites pendentes de clã.");
            return true;
        }

        // Verifica limite de membros
        if (invitedClan.getMemberCount() >= MAX_MEMBERS) {
            player.sendMessage("§cO clã " + clanManager.getFormattedTag(invitedClan) + "§c atingiu o limite máximo de " + MAX_MEMBERS + " membros.");
            return true;
        }

        invitedClan.addMember(player.getUniqueId());
        clanManager.removeInvite(player.getUniqueId());
        clanManager.saveClan(invitedClan);

        // Atualiza o cache do jogador após aceitar
        clanManager.refreshPlayerCache(player.getUniqueId());

        player.sendMessage("§aVocê aceitou o convite e se juntou ao clã " + clanManager.getFormattedTag(invitedClan) + "§a!");
        clanManager.notifyClan(invitedClan, "§eO jogador " + player.getName() + "§e se juntou ao clã!");
        return true;
    }

    private boolean handleProfileCommand(Player player, Clan playerClan, String[] args) {
        Clan targetClan = playerClan; // Por padrão, mostra o próprio clã

        if (args.length == 2) {
            // Se houver argumento, busca o clã pelo Tag
            String searchTag = args[1];
            targetClan = clanConfig.getClanByCleanTag(searchTag);

            if (targetClan == null) {
                player.sendMessage("§cClã com a Tag '" + searchTag + "' não encontrado.");
                return true;
            }
        } else if (playerClan == null) {
            player.sendMessage("§cUso: /clan profile [tag]. Você não está em um clã.");
            return true;
        }

        // Exibe o Perfil do Clã
        String tagFormatted = clanManager.getFormattedTag(targetClan);
        player.sendMessage("§7" + "=".repeat(40));
        player.sendMessage("§a📋 Perfil do Clã: " + tagFormatted);
        player.sendMessage("§7" + "-".repeat(40));
        player.sendMessage("§eNome: §f" + targetClan.getName());
        player.sendMessage("§eTag: §f" + tagFormatted);
        player.sendMessage("§eMembros: §f" + targetClan.getMemberCount() + "/" + MAX_MEMBERS);
        player.sendMessage("§eDono: §f" + Bukkit.getOfflinePlayer(targetClan.getOwnerId()).getName());

        // Lista de líderes
        List<String> leaderNames = targetClan.getLeaders().stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!leaderNames.isEmpty()) {
            player.sendMessage("§eLíderes: §f" + String.join("§7, §f", leaderNames));
        }

        // Relações Diplomáticas - APENAS CLÃS EXISTENTES E FORMATADAS
        List<String> validAllies = new ArrayList<>();
        for (String allyTag : targetClan.getAllies()) {
            Clan allyClan = clanConfig.getClanByCleanTag(allyTag);
            if (allyClan != null) { // Só mostra aliados que ainda existem
                validAllies.add(clanManager.getFormattedTag(allyClan));
            }
        }

        if (!validAllies.isEmpty()) {
            player.sendMessage("§eAliados (§a" + validAllies.size() + "§e): §a" +
                    String.join("§7, §a", validAllies));
        }

        List<String> validWars = new ArrayList<>();
        for (String warTag : targetClan.getWars()) {
            Clan warClan = clanConfig.getClanByCleanTag(warTag);
            if (warClan != null) { // Só mostra guerras com clãs existentes
                validWars.add(clanManager.getFormattedTag(warClan));
            }
        }

        if (!validWars.isEmpty()) {
            player.sendMessage("§eGuerras (§c" + validWars.size() + "§e): §c" +
                    String.join("§7, §c", validWars));
        }

        List<String> validPeace = new ArrayList<>();
        for (String peaceTag : targetClan.getPeaceTreaties()) {
            Clan peaceClan = clanConfig.getClanByCleanTag(peaceTag);
            if (peaceClan != null) { // Só mostra paz com clãs existentes
                validPeace.add(clanManager.getFormattedTag(peaceClan));
            }
        }

        if (!validPeace.isEmpty()) {
            player.sendMessage("§ePaz (§2" + validPeace.size() + "§e): §2" +
                    String.join("§7, §2", validPeace));
        }

        player.sendMessage("§7" + "=".repeat(40));
        return true;
    }

    private boolean handleRosterCommand(Player player, Clan playerClan, String[] args) {
        Clan targetClan = playerClan;

        if (args.length == 2) {
            String searchTag = args[1];
            targetClan = clanConfig.getClanByCleanTag(searchTag);
        } else if (playerClan == null) {
            player.sendMessage("§cUso: /clan roster [tag]. Você não está em um clã.");
            return true;
        }

        if (targetClan == null) {
            player.sendMessage("§cClã não encontrado.");
            return true;
        }

        // Lista de Membros Formatada
        player.sendMessage("§7" + "=".repeat(40));
        player.sendMessage("§a👥 Membros de " + clanManager.getFormattedTag(targetClan) + " §7(" + targetClan.getMemberCount() + ")");
        player.sendMessage("§7" + "-".repeat(40));

        // Cria cópia final da variável para uso em lambda
        final Clan finalTargetClan = targetClan;

        // Lista Dono
        finalTargetClan.getMembers().stream()
                .filter(finalTargetClan::isOwner)
                .map(Bukkit::getOfflinePlayer)
                .filter(Objects::nonNull)
                .forEach(p -> {
                    String onlineStatus = p.isOnline() ? "§a●" : "§7○";
                    player.sendMessage("§f👑 Dono: §f" + p.getName() + " " + onlineStatus);
                });

        // Lista Líderes
        finalTargetClan.getMembers().stream()
                .filter(memberId -> finalTargetClan.isLeader(memberId) && !finalTargetClan.isOwner(memberId))
                .map(Bukkit::getOfflinePlayer)
                .filter(Objects::nonNull)
                .forEach(p -> {
                    String onlineStatus = p.isOnline() ? "§a●" : "§7○";
                    player.sendMessage("§c⚔ Líder: §c" + p.getName() + " " + onlineStatus);
                });

        // Lista Membros Comuns
        finalTargetClan.getMembers().stream()
                .filter(memberId -> !finalTargetClan.isLeader(memberId) && !finalTargetClan.isOwner(memberId))
                .map(Bukkit::getOfflinePlayer)
                .filter(Objects::nonNull)
                .forEach(p -> {
                    String onlineStatus = p.isOnline() ? "§a●" : "§7○";
                    player.sendMessage("§a✓ Membro: §a" + p.getName() + " " + onlineStatus);
                });

        player.sendMessage("§7" + "=".repeat(40));
        return true;
    }

    private boolean handleInfoCommand(Player player, Clan playerClan) {
        if (playerClan == null) {
            player.sendMessage("§cVocê não está em nenhum clã.");
            return true;
        }

        // Informações detalhadas do clã do jogador
        player.sendMessage("§7" + "=".repeat(40));
        player.sendMessage("§a📊 Informações do Seu Clã");
        player.sendMessage("§7" + "-".repeat(40));
        player.sendMessage("§eNome: §f" + playerClan.getName());
        player.sendMessage("§eTag: §f" + clanManager.getFormattedTag(playerClan));
        player.sendMessage("§eSeu Cargo: §f" + getPlayerRank(playerClan, player.getUniqueId()));
        player.sendMessage("§eMembros: §f" + playerClan.getMemberCount() + "/" + MAX_MEMBERS);
        player.sendMessage("§eLíderes: §f" + playerClan.getLeaderCount());

        // Estatísticas de relações
        player.sendMessage("§eAliados: §a" + playerClan.getAllies().size());
        player.sendMessage("§eGuerras: §c" + playerClan.getWars().size());
        player.sendMessage("§eTratados de Paz: §2" + playerClan.getPeaceTreaties().size());

        // Convites pendentes
        if (!playerClan.getPendingAllyInvites().isEmpty()) {
            player.sendMessage("§eConvites de Aliança: §b" + playerClan.getPendingAllyInvites().size());
        }

        if (!playerClan.getPendingPeaceInvites().isEmpty()) {
            player.sendMessage("§eConvites de Paz: §2" + playerClan.getPendingPeaceInvites().size());
        }

        player.sendMessage("§7" + "=".repeat(40));
        return true;
    }

    private boolean handleLeaveCommand(Player player, Clan playerClan, String[] args) {
        if (playerClan.isOwner(player.getUniqueId())) {
            player.sendMessage("§cO dono não pode sair do clã. Use /clan disband para desmembrar ou /clan posse para transferir a posse.");
            return true;
        }

        // Confirmação
        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            playerClan.removeMember(player.getUniqueId());
            playerClan.removeLeader(player.getUniqueId());

            // Limpa o cache do jogador
            clanManager.removePlayerCache(player.getUniqueId());

            player.sendMessage("§aVocê saiu do clã " + clanManager.getFormattedTag(playerClan) + "§a com sucesso!");
            clanManager.notifyClan(playerClan, "§eO membro " + player.getName() + "§e deixou o clã.");
            clanManager.saveClan(playerClan);
            return true;
        } else {
            player.sendMessage("§e⚠ Tem certeza que deseja sair do clã " + clanManager.getFormattedTag(playerClan) + "§e?");
            player.sendMessage("§7Use §f/clan leave confirm §7para confirmar.");
            return true;
        }
    }

    private boolean handleDisbandCommand(Player player, Clan playerClan, String[] args) {
        if (!playerClan.isOwner(player.getUniqueId())) {
            player.sendMessage("§cApenas o Dono pode desmembrar o clã.");
            return true;
        }

        // Confirmação
        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            // Notifica os membros antes de deletar
            clanManager.notifyClan(playerClan, "§4⚠ O clã " + clanManager.getFormattedTag(playerClan) + "§4 será desmembrado em 10 segundos!");

            // Agenda a deleção após 10 segundos
            Bukkit.getScheduler().runTaskLater(RhaegarClans.getInstance(), () -> {
                // Deleta o clã (isso já limpa as referências automaticamente)
                clanManager.deleteClan(playerClan);
                player.sendMessage("§aVocê desmembrou o clã " + clanManager.getFormattedTag(playerClan) + "§a e todos os dados foram apagados.");
            }, 200L); // 10 segundos (20 ticks = 1 segundo)

            return true;
        } else {
            player.sendMessage("§c⚠ ATENÇÃO: Esta ação é IRREVERSÍVEL!");
            player.sendMessage("§cTodos os dados do clã serão PERDIDOS permanentemente.");
            player.sendMessage("§7Use §f/clan disband confirm §7para confirmar a desmontagem do clã.");
            return true;
        }
    }

    private boolean handleInviteCommand(Player player, Clan playerClan, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUso: /clan invite <jogador>");
            return true;
        }

        // Verifica limite de membros
        if (playerClan.getMemberCount() >= MAX_MEMBERS) {
            player.sendMessage("§cO clã atingiu o limite máximo de " + MAX_MEMBERS + " membros.");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cJogador não encontrado ou offline.");
            return true;
        }

        if (clanManager.getClanByPlayer(target.getUniqueId()) != null) {
            player.sendMessage("§c" + target.getName() + " já está em um clã.");
            return true;
        }

        if (clanManager.getPendingInvite(target.getUniqueId()) != null) {
            player.sendMessage("§c" + target.getName() + " já possui um convite pendente.");
            return true;
        }

        clanManager.sendInvite(playerClan, target);
        player.sendMessage("§aConvite enviado para " + target.getName() + "§a!");

        // Mensagem formatada para o convidado
        String formattedTag = clanManager.getFormattedTag(playerClan);
        target.sendMessage("§7" + "=".repeat(40));
        target.sendMessage("§a🎉 Você recebeu um convite para um clã!");
        target.sendMessage("§7" + "-".repeat(40));
        target.sendMessage("§eClã: §f" + playerClan.getName() + " " + formattedTag);
        target.sendMessage("§eConvidado por: §f" + player.getName());
        target.sendMessage("§eMembros: §f" + playerClan.getMemberCount());
        target.sendMessage("§7" + "-".repeat(40));
        target.sendMessage("§aPara aceitar: §f/clan accept");
        target.sendMessage("§7" + "=".repeat(40));

        return true;
    }

    private boolean handleKickCommand(Player player, Clan playerClan, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUso: /clan kick <jogador>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cJogador não encontrado ou offline.");
            return true;
        }

        UUID targetId = target.getUniqueId();

        if (!playerClan.isMember(targetId)) {
            player.sendMessage("§c" + target.getName() + " não é membro do seu clã.");
            return true;
        }

        // Prevenção: Dono não pode expulsar a si mesmo, nem ser expulso por Líder/Membro
        if (playerClan.isOwner(targetId)) {
            player.sendMessage("§cVocê não pode expulsar o Dono do clã.");
            return true;
        }

        // Líderes só podem ser expulsos por Donos
        if (playerClan.isLeader(targetId) && !playerClan.isOwner(player.getUniqueId())) {
            player.sendMessage("§cApenas o Dono pode expulsar outros Líderes.");
            return true;
        }

        // Remove o membro
        clanManager.removeInvite(targetId); // Remove convites pendentes
        playerClan.removeMember(targetId);
        playerClan.removeLeader(targetId);

        // Limpeza de Cache para o jogador expulso
        clanManager.removePlayerCache(targetId);

        player.sendMessage("§aVocê expulsou " + target.getName() + "§a do clã com sucesso.");
        target.sendMessage("§cVocê foi expulso do clã " + clanManager.getFormattedTag(playerClan) + "§c por " + player.getName() + ".");
        clanManager.notifyClan(playerClan, "§eO membro " + target.getName() + "§e foi expulso por " + player.getName() + ".");
        clanManager.saveClan(playerClan);
        return true;
    }

    private boolean handlePromoteCommand(Player player, Clan playerClan, String[] args) {
        if (!playerClan.isOwner(player.getUniqueId())) {
            player.sendMessage("§cApenas o Dono pode promover a Líder.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUso: /clan promote <jogador>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !playerClan.isMember(target.getUniqueId())) {
            player.sendMessage("§cJogador não encontrado ou não é membro do clã.");
            return true;
        }

        if (playerClan.isOwner(target.getUniqueId()) || playerClan.isLeader(target.getUniqueId())) {
            player.sendMessage("§cEste jogador já é Líder ou Dono.");
            return true;
        }

        playerClan.addLeader(target.getUniqueId());
        clanManager.saveClan(playerClan);

        player.sendMessage("§a" + target.getName() + " foi promovido a Líder!");
        target.sendMessage("§a🎉 Parabéns! Você foi promovido a Líder do clã " + clanManager.getFormattedTag(playerClan) + "!");
        clanManager.notifyClan(playerClan, "§e" + target.getName() + "§e foi promovido a Líder por " + player.getName() + ".");
        return true;
    }

    private boolean handleDemoteCommand(Player player, Clan playerClan, String[] args) {
        if (!playerClan.isOwner(player.getUniqueId())) {
            player.sendMessage("§cApenas o Dono pode rebaixar um Líder.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUso: /clan demote <jogador>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !playerClan.isLeader(target.getUniqueId())) {
            player.sendMessage("§cJogador não encontrado ou não é Líder do clã.");
            return true;
        }

        if (playerClan.isOwner(target.getUniqueId())) {
            player.sendMessage("§cVocê não pode rebaixar o Dono.");
            return true;
        }

        playerClan.removeLeader(target.getUniqueId());
        clanManager.saveClan(playerClan);

        player.sendMessage("§a" + target.getName() + " foi rebaixado para Membro.");
        target.sendMessage("§cVocê foi rebaixado para Membro do clã " + clanManager.getFormattedTag(playerClan) + ".");
        clanManager.notifyClan(playerClan, "§e" + target.getName() + "§e foi rebaixado por " + player.getName() + ".");
        return true;
    }

    private boolean handlePosseCommand(Player player, Clan playerClan, String[] args) {
        if (!playerClan.isOwner(player.getUniqueId())) {
            player.sendMessage("§cApenas o Dono pode transferir a posse do clã.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUso: /clan posse <novo_dono>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !playerClan.isMember(target.getUniqueId())) {
            player.sendMessage("§cJogador não encontrado ou não é membro do clã.");
            return true;
        }

        UUID targetId = target.getUniqueId();

        if (player.getUniqueId().equals(targetId)) {
            player.sendMessage("§cVocê já é o Dono do clã.");
            return true;
        }

        // Confirmação
        if (args.length > 2 && args[2].equalsIgnoreCase("confirm")) {
            // Lógica de Transferência
            playerClan.setOwnerId(targetId);
            // O antigo dono vira líder se já não for
            if (!playerClan.isLeader(player.getUniqueId())) {
                playerClan.addLeader(player.getUniqueId());
            }

            playerClan.removeLeader(targetId); // Garante que o novo dono não é líder (pois agora é Owner)

            clanManager.saveClan(playerClan);

            player.sendMessage("§aVocê transferiu a posse do clã para " + target.getName() + "§a.");
            target.sendMessage("§a👑 Parabéns! Você é o novo Dono do clã " + clanManager.getFormattedTag(playerClan) + "§a!");
            clanManager.notifyClan(playerClan, "§e👑 A posse do clã foi transferida de " + player.getName() + "§e para " + target.getName() + "§e.");
            return true;
        } else {
            player.sendMessage("§c⚠ ATENÇÃO: Você está prestes a transferir a posse do clã!");
            player.sendMessage("§cO jogador " + target.getName() + "§c se tornará o novo Dono.");
            player.sendMessage("§cVocê se tornará um Líder automaticamente.");
            player.sendMessage("§7Use §f/clan posse " + target.getName() + " confirm §7para confirmar.");
            return true;
        }
    }

    private boolean handleRenameCommand(Player player, Clan playerClan, String[] args) {
        if (!playerClan.isOwner(player.getUniqueId())) {
            player.sendMessage("§cApenas o Dono pode renomear o clã.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUso: /clan rename <novo_nome>");
            player.sendMessage("§7Use & para cores (ex: &6&lTeste)");
            return true;
        }

        String newName = args[1];

        // Converter & para § para processamento
        String processedName = newName.replace('&', '§');
        String cleanName = ChatColor.stripColor(processedName);

        // Converter de volta para & para armazenamento
        String storedName = processedName.replace('§', '&');

        if (cleanName.length() < MIN_NAME_LENGTH || cleanName.length() > MAX_NAME_LENGTH) {
            player.sendMessage("§cO nome do clã deve ter entre " + MIN_NAME_LENGTH + " e " + MAX_NAME_LENGTH + " caracteres (sem contar as cores).");
            player.sendMessage("§7Nome atual sem cores: '" + cleanName + "' (" + cleanName.length() + " caracteres)");
            return true;
        }

        String oldName = playerClan.getName();
        playerClan.setName(storedName);
        clanManager.saveClan(playerClan);

        player.sendMessage("§aO nome do clã foi alterado de '§f" + oldName + "§a' para '§f" + storedName + "§a'.");
        clanManager.notifyClan(playerClan, "§eO nome do clã foi alterado para '§f" + storedName + "§e' por " + player.getName() + ".");
        return true;
    }

    private boolean handleSetTagCommand(Player player, Clan playerClan, String[] args) {
        if (!playerClan.isOwner(player.getUniqueId())) {
            player.sendMessage("§cApenas o Dono pode alterar a Tag do clã.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUso: /clan settag <nova_tag>");
            player.sendMessage("§7Use & para cores (ex: &6&lTST)");
            return true;
        }

        String newTag = args[1];

        // Converter & para § para processamento
        String processedTag = newTag.replace('&', '§');
        String cleanTag = ChatColor.stripColor(processedTag);

        // Converter de volta para & para armazenamento
        String storedTag = processedTag.replace('§', '&');

        // Validação do comprimento (sem cores)
        if (cleanTag.length() < MIN_TAG_LENGTH || cleanTag.length() > MAX_TAG_LENGTH) {
            player.sendMessage("§cA Tag do clã deve ter entre " + MIN_TAG_LENGTH + " e " + MAX_TAG_LENGTH + " caracteres (sem contar as cores).");
            player.sendMessage("§7Tag atual sem cores: '" + cleanTag + "' (" + cleanTag.length() + " caracteres)");
            return true;
        }

        // Verificar se a Tag já existe (ignorando a própria tag)
        Clan existingClan = clanConfig.getClanByCleanTag(storedTag);
        if (existingClan != null && !existingClan.getTag().equals(playerClan.getTag())) {
            player.sendMessage("§cJá existe um clã com a Tag '" + cleanTag + "'.");
            return true;
        }

        String oldTag = playerClan.getTag();
        playerClan.setTag(storedTag);

        // Atualiza no config
        clanConfig.removeClan(playerClan); // Remove com a tag antiga
        clanConfig.addClan(playerClan); // Adiciona com a nova tag
        clanConfig.saveClans();

        player.sendMessage("§aA Tag do clã foi alterada de '§f" + oldTag + "§a' para '§f" + storedTag + "§a'.");
        clanManager.notifyClan(playerClan, "§eA Tag do clã foi alterada para '§f" + storedTag + "§e' por " + player.getName() + ".");
        return true;
    }

    // ============ MÉTODOS AUXILIARES ============

    /**
     * Retorna o cargo do jogador no clã
     */
    private String getPlayerRank(Clan clan, UUID playerId) {
        if (clan.isOwner(playerId)) {
            return "§f👑 Dono";
        } else if (clan.isLeader(playerId)) {
            return "§c⚔ Líder";
        } else {
            return "§a✓ Membro";
        }
    }

    /**
     * Lógica para o TabCompletion
     */
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
        String subCommand = args.length > 0 ? args[0].toLowerCase() : "";

        if (args.length == 1) {
            return List.of("create", "invite", "kick", "promote", "demote", "posse", "accept",
                            "leave", "disband", "profile", "roster", "info", "help", "ajuda",
                            "rename", "settag", "tag").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            // Comandos que exigem um jogador como argumento
            if (subCommand.equals("invite")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .filter(name -> !name.equals(player.getName()))
                        .collect(Collectors.toList());
            }

            if (subCommand.equals("kick") || subCommand.equals("promote") ||
                    subCommand.equals("demote") || subCommand.equals("posse")) {
                if (playerClan == null) {
                    return Collections.emptyList();
                }

                return playerClan.getMembers().stream()
                        .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                        .filter(Objects::nonNull)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .filter(name -> !name.equals(player.getName()))
                        .collect(Collectors.toList());
            }

            // Comandos que exigem uma tag de clã
            if (subCommand.equals("profile") || subCommand.equals("roster")) {
                return clanConfig.getAllClanTags().stream()
                        .filter(tag -> clanConfig.getCleanTag(tag).toLowerCase()
                                .startsWith(clanConfig.getCleanTag(args[1]).toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Sugestões para create
            if (subCommand.equals("create")) {
                if (args[1].length() == 0) {
                    return List.of("<tag>");
                }
                return List.of();
            }

            // Sugestões para rename
            if (subCommand.equals("rename")) {
                return List.of("<novo_nome>");
            }

            // Sugestões para settag/tag
            if (subCommand.equals("settag") || subCommand.equals("tag")) {
                return List.of("<nova_tag>");
            }

            // Confirmações
            if (subCommand.equals("disband") || subCommand.equals("leave") || subCommand.equals("posse")) {
                return List.of("confirm").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && subCommand.equals("posse")) {
            return List.of("confirm").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}