package br.com.mod.rhaegarClans.core;

import br.com.mod.rhaegarClans.RhaegarClans;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanManager {

    private final RhaegarClans plugin;
    private final Map<UUID, Clan> pendingInvites = new HashMap<>();
    private final Map<UUID, String> playerCache = new HashMap<>(); // Cache para tags

    public ClanManager(RhaegarClans plugin) {
        this.plugin = plugin;
    }

    // --- MÉTODOS DE DADOS E PERSISTÊNCIA ---
    public Clan getClanByPlayer(UUID playerId) {
        for (Clan clan : plugin.getClanConfig().getLoadedClans().values()) {
            if (clan.isMember(playerId)) {
                return clan;
            }
        }
        return null;
    }

    public void saveClan(Clan clan) {
        plugin.getClanConfig().addClan(clan);
        plugin.getClanConfig().saveClans();
    }

    public void deleteClan(Clan clan) {
        if (clan == null) return;

        String clanTag = clan.getTag();

        // 1. Primeiro limpa todas as referências a este clã
        cleanupClanReferences(clanTag);

        // 2. Limpa o cache de todos os membros do clã
        for (UUID memberId : clan.getMembers()) {
            removePlayerCache(memberId);
        }

        // 3. Remove o clã do config
        plugin.getClanConfig().removeClan(clan);
        plugin.getClanConfig().saveClans();

        plugin.getLogger().info("Clã " + clanTag + " deletado com sucesso.");
    }

    // --- MÉTODOS DE CONVITE E ACEITE ---
    public void sendInvite(Clan clan, Player target) {
        pendingInvites.put(target.getUniqueId(), clan);
    }

    public Clan getPendingInvite(UUID playerId) {
        return pendingInvites.get(playerId);
    }

    public void removeInvite(UUID playerId) {
        pendingInvites.remove(playerId);
    }

    // --- SUPORTE A TAGS COLORIDAS ---
    public String getFormattedTag(Clan clan) {
        String tag = clan.getTag();
        if (tag == null || tag.isEmpty()) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', tag);
    }

    /**
     * Formata uma tag string (com &) para exibição.
     * Exemplo: "&6&lTAG" → "§6§lTAG"
     */
    public String formatTagString(String tag) {
        if (tag == null || tag.isEmpty()) {
            return "";
        }
        // Converte & para §
        return ChatColor.translateAlternateColorCodes('&', tag);
    }

    public void notifyClan(Clan clan, String message) {
        for (UUID memberId : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(message);
            }
        }
    }

    // --- MÉTODOS DE CACHE ---
    public void refreshPlayerCache(UUID playerId) {
        Clan clan = getClanByPlayer(playerId);
        if (clan != null) {
            playerCache.put(playerId, getFormattedTag(clan));
        } else {
            playerCache.remove(playerId);
        }
    }

    public void removePlayerCache(UUID playerId) {
        playerCache.remove(playerId);
    }

    public void refreshAllCaches() {
        playerCache.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayerCache(player.getUniqueId());
        }
    }

    // --- MÉTODO RELACIONAL PARA PLACEHOLDERAPI ---
    public String getRelationalColor(Player viewer, Player target) {
        Clan viewerClan = getClanByPlayer(viewer.getUniqueId());
        Clan targetClan = getClanByPlayer(target.getUniqueId());

        if (viewerClan == null || targetClan == null) {
            return "&f"; // Branco se algum não tiver clã
        }

        String targetTag = targetClan.getTag();

        if (viewerClan.isAlly(targetTag)) {
            return "&a"; // Verde para aliados
        } else if (viewerClan.isAtWarWith(targetTag)) {
            return "&c"; // Vermelho para guerra
        } else if (viewerClan.hasPeaceTreaty(targetTag)) {
            return "&e"; // Amarelo para paz
        } else {
            return "&f"; // Branco para neutro
        }
    }

    /**
     * Limpa todas as referências a um clã que será deletado.
     * Remove de aliados, guerras, tratados de paz e convites de outros clãs.
     */
    public void cleanupClanReferences(String deletedClanTag) {
        // Obtém o ClanConfig a partir do plugin
        ClanConfig clanConfig = plugin.getClanConfig();

        for (Clan otherClan : clanConfig.getLoadedClans().values()) {
            // Remove o clã deletado das listas de relações
            otherClan.removeAlly(deletedClanTag);
            otherClan.endWar(deletedClanTag);
            otherClan.removePeaceTreaty(deletedClanTag);
            otherClan.removePendingAllyInvite(deletedClanTag);
            otherClan.removePendingPeaceInvite(deletedClanTag);

            // Salva o clã atualizado
            saveClan(otherClan);
        }

        // Log da limpeza
        plugin.getLogger().info("Limpeza de referências concluída para o clã deletado: " + deletedClanTag);
    }
}