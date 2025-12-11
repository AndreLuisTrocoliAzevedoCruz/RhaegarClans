package br.com.mod.rhaegarClans.core;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Clan {

    private UUID ownerId; // O único Dono do Clã
    private String name;
    private String tag;
    private Set<UUID> leaders; // Set para Líderes
    private Set<UUID> members; // Membros comuns (incluindo Líderes e Dono)

    // --- Diplomacia ---
    private Set<String> allies; // Tags dos clãs aliados
    private Set<String> wars; // Tags dos clãs em guerra
    private Set<String> peaceTreaties; // Tags dos clãs em Tratado de Não-Agressão (TNA)

    // --- Convites Pendentes ---
    private Set<String> pendingAllyInvites; // Tags de clãs que nos convidaram para aliança
    private Set<String> pendingPeaceInvites; // Tags de clãs que nos convidaram para TNA

    // Construtor principal (criação de novo clã)
    public Clan(UUID ownerId, String name, String tag) {
        this.ownerId = ownerId;
        this.name = name;
        this.tag = tag;
        this.leaders = new HashSet<>();
        this.members = new HashSet<>();
        this.members.add(ownerId); // O Dono é o primeiro membro
        this.allies = new HashSet<>();
        this.wars = new HashSet<>();
        this.peaceTreaties = new HashSet<>();
        this.pendingAllyInvites = new HashSet<>();
        this.pendingPeaceInvites = new HashSet<>();
    }

    // Construtor usado para carregar dados
    public Clan(UUID ownerId, String name, String tag, Set<UUID> leaders, Set<UUID> members,
                Set<String> allies, Set<String> wars, Set<String> peaceTreaties,
                Set<String> pendingAllyInvites, Set<String> pendingPeaceInvites) {
        this.ownerId = ownerId;
        this.name = name;
        this.tag = tag;
        this.leaders = leaders;
        this.members = members;
        this.allies = allies;
        this.wars = wars;
        this.peaceTreaties = peaceTreaties;
        this.pendingAllyInvites = pendingAllyInvites;
        this.pendingPeaceInvites = pendingPeaceInvites;

        // Garante a integridade: Dono deve ser sempre um membro
        if (!this.members.contains(ownerId)) {
            this.members.add(ownerId);
        }

        // Garante que líderes também sejam membros
        for (UUID leaderId : leaders) {
            if (!this.members.contains(leaderId)) {
                this.members.add(leaderId);
            }
        }
    }

    // --- Getters e Setters Básicos ---

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public Set<UUID> getLeaders() {
        return new HashSet<>(leaders);
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnerId(UUID newOwnerId) {
        this.ownerId = newOwnerId;
    }

    // --- Gerenciamento de Membros e Cargos ---

    public void addLeader(UUID playerId) {
        if (isMember(playerId) && !isOwner(playerId) && !isLeader(playerId)) {
            leaders.add(playerId);
        }
    }

    public void removeLeader(UUID playerId) {
        leaders.remove(playerId);
    }

    // --- Checagem de Permissões ---

    public boolean isOwner(UUID playerId) {
        return this.ownerId.equals(playerId);
    }

    public boolean isLeader(UUID playerId) {
        return leaders.contains(playerId);
    }

    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }

    /** Retorna true se o jogador for Dono OU Líder (possuir permissões de gestão) */
    public boolean hasManagementPermissions(UUID playerId) {
        return isOwner(playerId) || isLeader(playerId);
    }

    // --- Gerenciamento de Membros Comuns ---

    public void addMember(UUID playerId) {
        if (!isMember(playerId)) {
            members.add(playerId);
        }
    }

    public void removeMember(UUID playerId) {
        // Remove de todas as listas: Líderes, Membros
        leaders.remove(playerId);
        members.remove(playerId);
    }

    /**
     * Retorna o número total de membros no clã
     */
    public int getMemberCount() {
        return members.size();
    }

    /**
     * Retorna o número de líderes (excluindo o dono)
     */
    public int getLeaderCount() {
        return leaders.size();
    }

    /**
     * Retorna o número de membros comuns (não líderes e não dono)
     */
    public int getRegularMemberCount() {
        int count = 0;
        for (UUID memberId : members) {
            if (!isOwner(memberId) && !isLeader(memberId)) {
                count++;
            }
        }
        return count;
    }

    // --- Gerenciamento de Diplomacia ---

    public Set<String> getAllies() {
        return new HashSet<>(allies);
    }

    public Set<String> getWars() {
        return new HashSet<>(wars);
    }

    public Set<String> getPeaceTreaties() {
        return new HashSet<>(peaceTreaties);
    }

    public Set<String> getPendingAllyInvites() {
        return new HashSet<>(pendingAllyInvites);
    }

    public Set<String> getPendingPeaceInvites() {
        return new HashSet<>(pendingPeaceInvites);
    }

    public boolean isAlly(String tag) {
        return allies.contains(tag);
    }

    public boolean isAtWarWith(String tag) {
        return wars.contains(tag);
    }

    public boolean hasPeaceTreaty(String tag) {
        return peaceTreaties.contains(tag);
    }

    public boolean hasPendingAllyInvite(String tag) {
        return pendingAllyInvites.contains(tag);
    }

    public boolean hasPendingPeaceInvite(String tag) {
        return pendingPeaceInvites.contains(tag);
    }

    public void addAlly(String tag) {
        // Aliança: Remove Paz e Guerra
        allies.add(tag);
        removePeaceTreaty(tag);
        endWar(tag);
    }

    public void addPeaceTreaty(String tag) {
        // Paz: Remove Aliança e Guerra
        peaceTreaties.add(tag);
        removeAlly(tag);
        endWar(tag);
    }

    public void declareWar(String tag) {
        // Guerra: Remove Aliança e Paz
        wars.add(tag);
        removeAlly(tag);
        removePeaceTreaty(tag);
    }

    public void removeAlly(String tag) {
        allies.remove(tag);
    }

    public void endWar(String tag) {
        wars.remove(tag);
    }

    public void removePeaceTreaty(String tag) {
        peaceTreaties.remove(tag);
    }

    public void addPendingAllyInvite(String tag) {
        pendingAllyInvites.add(tag);
    }

    public void removePendingAllyInvite(String tag) {
        pendingAllyInvites.remove(tag);
    }

    public void addPendingPeaceInvite(String tag) {
        pendingPeaceInvites.add(tag);
    }

    public void removePendingPeaceInvite(String tag) {
        pendingPeaceInvites.remove(tag);
    }

    // --- Métodos Utilitários ---

    /**
     * Retorna se o clã está ativo (tem membros)
     */
    public boolean isActive() {
        return members.size() > 0;
    }

    /**
     * Retorna se o clã está vazio (sem membros)
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }

    /**
     * Limpa todas as relações diplomáticas
     */
    public void clearAllDiplomacy() {
        allies.clear();
        wars.clear();
        peaceTreaties.clear();
        pendingAllyInvites.clear();
        pendingPeaceInvites.clear();
    }

    @Override
    public String toString() {
        return "Clan{name='" + name + "', tag='" + tag + "', members=" + members.size() + "}";
    }
}