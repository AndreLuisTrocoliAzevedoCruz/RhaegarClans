package br.com.mod.rhaegarClans.integration;

import br.com.mod.rhaegarClans.core.Clan;
import br.com.mod.rhaegarClans.core.ClanManager;
import br.com.mod.rhaegarClans.RhaegarClans;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanExpansion extends PlaceholderExpansion implements Relational {

    private final RhaegarClans plugin;
    private final ClanManager clanManager;

    public ClanExpansion(RhaegarClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rhaegarclans";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Rhaegar";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean register() {
        boolean registered = super.register();
        if (registered) {
            plugin.getLogger().info("✅ Expansão do PlaceholderAPI registrada com sucesso!");
        } else {
            plugin.getLogger().warning("❌ Falha ao registrar expansão do PlaceholderAPI!");
        }
        return registered;
    }

    // Placeholder normal (um jogador)
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());

        // %rhaegarclans_tag% - Retorna a tag formatada com espaço antes
        if (params.equalsIgnoreCase("tag")) {
            if (clan != null) {
                String formattedTag = clanManager.getFormattedTag(clan);
                // Adiciona espaço antes para uso em chat/formatação
                return formattedTag.isEmpty() ? "" : " " + formattedTag;
            } else {
                return "";
            }
        }

        // %rhaegarclans_rank% - Retorna o cargo do jogador no clã
        if (params.equalsIgnoreCase("rank")) {
            if (clan == null) return "";

            if (clan.isOwner(player.getUniqueId())) {
                return "§f[Dono]";
            } else if (clan.isLeader(player.getUniqueId())) {
                return "§c[Líder]";
            } else {
                return "§a[Membro]";
            }
        }

        // %rhaegarclans_name% - Retorna o nome do clã
        if (params.equalsIgnoreCase("name")) {
            return clan != null ? clan.getName() : "";
        }

        // %rhaegarclans_hasclan% - Retorna true/false se tem clã
        if (params.equalsIgnoreCase("hasclan") || params.equalsIgnoreCase("has_clan")) {
            return clan != null ? "true" : "false";
        }

        // %rhaegarclans_membercount% - Retorna quantidade de membros
        if (params.equalsIgnoreCase("membercount") || params.equalsIgnoreCase("members")) {
            return clan != null ? String.valueOf(clan.getMembers().size()) : "0";
        }

        return null;
    }

    // --- Placeholder Relacional (%rel_rhaegarclans_color%) ---
    @Override
    public String onPlaceholderRequest(Player viewer, Player target, String identifier) {
        if (viewer == null || target == null || identifier == null) {
            return "";
        }

        // Identificador: %rel_rhaegarclans_color%
        if (identifier.equalsIgnoreCase("color")) {
            String colorCode = getRelationalColor(viewer, target);
            return ChatColor.translateAlternateColorCodes('&', colorCode);
        }

        // %rel_rhaegarclans_relation% - Retorna o tipo de relação
        if (identifier.equalsIgnoreCase("relation")) {
            return getRelationType(viewer, target);
        }

        return null;
    }

    /**
     * Determina a cor relacional entre dois jogadores
     */
    private String getRelationalColor(Player viewer, Player target) {
        Clan viewerClan = clanManager.getClanByPlayer(viewer.getUniqueId());
        Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());

        // Se algum não tiver clã, retorna branco
        if (viewerClan == null || targetClan == null) {
            return "&f";
        }

        String targetTag = targetClan.getTag();
        String viewerTag = viewerClan.getTag();

        // Mesmo clã (aliados por padrão)
        if (viewerTag.equals(targetTag)) {
            return "&a";
        }

        // Aliados
        if (viewerClan.isAlly(targetTag)) {
            return "&b";
        }

        // Tratado de Paz (Não-Agressão)
        if (viewerClan.hasPeaceTreaty(targetTag)) {
            return "&e";
        }

        // Guerra
        if (viewerClan.isAtWarWith(targetTag)) {
            return "&c";
        }

        // Neutro
        return "&f";
    }

    /**
     * Retorna o tipo de relação textual
     */
    private String getRelationType(Player viewer, Player target) {
        Clan viewerClan = clanManager.getClanByPlayer(viewer.getUniqueId());
        Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());

        if (viewerClan == null || targetClan == null) {
            return "Neutro";
        }

        String targetTag = targetClan.getTag();
        String viewerTag = viewerClan.getTag();

        if (viewerTag.equals(targetTag)) {
            return "Clanmate";
        }

        if (viewerClan.isAlly(targetTag)) {
            return "Aliado";
        }

        if (viewerClan.hasPeaceTreaty(targetTag)) {
            return "Paz";
        }

        if (viewerClan.isAtWarWith(targetTag)) {
            return "Guerra";
        }

        return "Neutro";
    }
}