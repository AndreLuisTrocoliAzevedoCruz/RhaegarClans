package br.com.mod.rhaegarClans.core;

import br.com.mod.rhaegarClans.RhaegarClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ClanConfig {

    private final RhaegarClans plugin;
    private final File clanConfigFile;
    private FileConfiguration clanConfig;

    // Mapa que armazena todos os clãs carregados: ClanTag -> Clan Object
    private final Map<String, Clan> loadedClans = new HashMap<>();

    public ClanConfig(RhaegarClans plugin) {
        this.plugin = plugin;
        this.clanConfigFile = new File(plugin.getDataFolder(), "clans.yml");
        this.loadConfig();
        this.loadClans();
    }

    // --- Configuração Básica e Persistência ---

    private void loadConfig() {
        if (!clanConfigFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                clanConfigFile.createNewFile();
                plugin.getLogger().info("Arquivo clans.yml criado com sucesso.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Não foi possível criar o arquivo clans.yml!", e);
            }
        }
        this.clanConfig = YamlConfiguration.loadConfiguration(clanConfigFile);
    }

    public void loadClans() {
        loadedClans.clear(); // Limpa antes de carregar

        if (clanConfig.contains("clans")) {
            for (String tag : clanConfig.getConfigurationSection("clans").getKeys(false)) {
                try {
                    String name = clanConfig.getString("clans." + tag + ".name", "Sem Nome");
                    String ownerIdStr = clanConfig.getString("clans." + tag + ".ownerId");

                    if (ownerIdStr == null) {
                        plugin.getLogger().warning("Clã " + tag + " não tem ownerId! Pulando...");
                        continue;
                    }

                    UUID ownerId = UUID.fromString(ownerIdStr);

                    // Carrega Set de UUIDs (Membros e Líderes)
                    Set<UUID> members = clanConfig.getStringList("clans." + tag + ".members").stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toSet());

                    List<String> leadersList = clanConfig.getStringList("clans." + tag + ".leaders");
                    Set<UUID> leaders = leadersList.stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toSet());

                    // Carrega Sets de Relações (Tags)
                    Set<String> allies = new HashSet<>(clanConfig.getStringList("clans." + tag + ".allies"));
                    Set<String> wars = new HashSet<>(clanConfig.getStringList("clans." + tag + ".wars"));
                    Set<String> peaceTreaties = new HashSet<>(clanConfig.getStringList("clans." + tag + ".peaceTreaties"));

                    // Carrega Sets de Convites
                    Set<String> pendingAllyInvites = new HashSet<>(clanConfig.getStringList("clans." + tag + ".pendingAllyInvites"));
                    Set<String> pendingPeaceInvites = new HashSet<>(clanConfig.getStringList("clans." + tag + ".pendingPeaceInvites"));

                    // Verifica integridade dos dados
                    if (!members.contains(ownerId)) {
                        members.add(ownerId);
                        plugin.getLogger().info("Corrigido: Dono " + ownerId + " adicionado aos membros do clã " + tag);
                    }

                    Clan clan = new Clan(ownerId, name, tag, leaders, members, allies, wars, peaceTreaties, pendingAllyInvites, pendingPeaceInvites);
                    loadedClans.put(tag, clan);

                    plugin.getLogger().info("Clã carregado: " + tag + " (" + name + ") com " + members.size() + " membros");

                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Erro ao carregar o clã com Tag: " + tag, e);
                }
            }
        }

        // Limpa relações com clãs que não existem mais
        cleanupInvalidRelations();

        plugin.getLogger().info("Total de " + loadedClans.size() + " clãs carregados do arquivo.");
    }

    /**
     * Limpa relações com clãs que não existem mais.
     */
    private void cleanupInvalidRelations() {
        int cleanedCount = 0;

        for (Clan clan : loadedClans.values()) {
            // Limpa aliados inexistentes
            Set<String> invalidAllies = clan.getAllies().stream()
                    .filter(allyTag -> getClanByCleanTag(allyTag) == null)
                    .collect(Collectors.toSet());

            invalidAllies.forEach(clan::removeAlly);

            // Limpa guerras inexistentes
            Set<String> invalidWars = clan.getWars().stream()
                    .filter(warTag -> getClanByCleanTag(warTag) == null)
                    .collect(Collectors.toSet());

            invalidWars.forEach(clan::endWar);

            // Limpa tratados de paz inexistentes
            Set<String> invalidPeace = clan.getPeaceTreaties().stream()
                    .filter(peaceTag -> getClanByCleanTag(peaceTag) == null)
                    .collect(Collectors.toSet());

            invalidPeace.forEach(clan::removePeaceTreaty);

            // Limpa convites inexistentes
            Set<String> invalidAllyInvites = clan.getPendingAllyInvites().stream()
                    .filter(inviteTag -> getClanByCleanTag(inviteTag) == null)
                    .collect(Collectors.toSet());

            invalidAllyInvites.forEach(clan::removePendingAllyInvite);

            Set<String> invalidPeaceInvites = clan.getPendingPeaceInvites().stream()
                    .filter(inviteTag -> getClanByCleanTag(inviteTag) == null)
                    .collect(Collectors.toSet());

            invalidPeaceInvites.forEach(clan::removePendingPeaceInvite);

            cleanedCount += invalidAllies.size() + invalidWars.size() + invalidPeace.size() +
                    invalidAllyInvites.size() + invalidPeaceInvites.size();
        }

        if (cleanedCount > 0) {
            plugin.getLogger().info("Limpeza automática: " + cleanedCount + " relações inválidas removidas.");
            saveClans(); // Salva as alterações
        }
    }

    public void saveClans() {
        // Limpa a seção de clãs antes de salvar
        if (clanConfig.contains("clans")) {
            clanConfig.set("clans", null);
        }

        for (Clan clan : loadedClans.values()) {
            String tag = clan.getTag();

            clanConfig.set("clans." + tag + ".name", clan.getName());
            clanConfig.set("clans." + tag + ".ownerId", clan.getOwnerId().toString());

            // Salva listas de UUIDs
            clanConfig.set("clans." + tag + ".members",
                    clan.getMembers().stream()
                            .map(UUID::toString)
                            .collect(Collectors.toList()));

            clanConfig.set("clans." + tag + ".leaders",
                    clan.getLeaders().stream()
                            .map(UUID::toString)
                            .collect(Collectors.toList()));

            // Salva listas de Relações
            clanConfig.set("clans." + tag + ".allies", new ArrayList<>(clan.getAllies()));
            clanConfig.set("clans." + tag + ".wars", new ArrayList<>(clan.getWars()));
            clanConfig.set("clans." + tag + ".peaceTreaties", new ArrayList<>(clan.getPeaceTreaties()));

            // Salva listas de Convites
            clanConfig.set("clans." + tag + ".pendingAllyInvites", new ArrayList<>(clan.getPendingAllyInvites()));
            clanConfig.set("clans." + tag + ".pendingPeaceInvites", new ArrayList<>(clan.getPendingPeaceInvites()));
        }

        try {
            clanConfig.save(clanConfigFile);
            plugin.getLogger().info("✅ " + loadedClans.size() + " clãs salvos com sucesso.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Não foi possível salvar os clãs!", e);
        }
    }

    // --- Métodos de Busca e Utilidade (Busca sem cores) ---

    /**
     * Remove códigos de cor da tag (ex: transforma "&aTAG" em "TAG").
     * Agora também remove formatação (negrito, sublinhado, etc.)
     */
    public String getCleanTag(String tag) {
        if (tag == null) return "";

        // Primeiro converte & para § para o ChatColor entender
        String convertedTag = tag.replace('&', '§');

        // Remove todos os códigos de cor e formatação
        String clean = ChatColor.stripColor(convertedTag);

        // Remove também qualquer § residual
        clean = clean.replace("§", "");

        return clean;
    }

    /**
     * Busca um clã por Tag limpa (sem códigos de cor e formatação).
     */
    public Clan getClanByCleanTag(String searchTag) {
        if (searchTag == null || searchTag.isEmpty()) {
            return null;
        }

        String cleanSearch = getCleanTag(searchTag);

        if (cleanSearch.isEmpty()) {
            return null;
        }

        // 1. Tenta encontrar pelo nome exato (incluindo cores, se for o caso)
        if (loadedClans.containsKey(searchTag)) {
            return loadedClans.get(searchTag);
        }

        // 2. Itera sobre todos os clãs e compara a Tag limpa
        for (Clan clan : loadedClans.values()) {
            String cleanSavedTag = getCleanTag(clan.getTag());

            if (cleanSavedTag.equalsIgnoreCase(cleanSearch)) {
                return clan;
            }

            // Também verifica se a tag original (com cores) bate exatamente
            if (clan.getTag().equalsIgnoreCase(searchTag)) {
                return clan;
            }
        }

        return null;
    }

    /**
     * Busca clã pelo nome (não pela tag)
     */
    public Clan getClanByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        String searchName = name.toLowerCase();

        for (Clan clan : loadedClans.values()) {
            if (clan.getName().toLowerCase().contains(searchName)) {
                return clan;
            }
        }

        return null;
    }

    // --- Métodos de Gerenciamento ---

    public void addClan(Clan clan) {
        if (clan == null || clan.getTag() == null) {
            return;
        }

        loadedClans.put(clan.getTag(), clan);
        plugin.getLogger().info("Clã " + clan.getTag() + " adicionado ao cache.");
    }

    public void removeClan(Clan clan) {
        if (clan != null && clan.getTag() != null) {
            loadedClans.remove(clan.getTag());
            plugin.getLogger().info("Clã " + clan.getTag() + " removido do cache.");
        }
    }

    public Map<String, Clan> getLoadedClans() {
        return Collections.unmodifiableMap(loadedClans);
    }

    /**
     * Retorna lista de tags de todos os clãs carregados
     */
    public List<String> getAllClanTags() {
        return new ArrayList<>(loadedClans.keySet());
    }

    /**
     * Retorna lista de nomes de todos os clãs carregados
     */
    public List<String> getAllClanNames() {
        return loadedClans.values().stream()
                .map(Clan::getName)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se uma tag já existe (considerando cores)
     */
    public boolean tagExists(String tag) {
        return getClanByCleanTag(tag) != null;
    }

    /**
     * Retorna o total de clãs carregados
     */
    public int getTotalClans() {
        return loadedClans.size();
    }
}