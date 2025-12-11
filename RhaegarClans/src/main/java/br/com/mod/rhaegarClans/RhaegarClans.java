package br.com.mod.rhaegarClans;

import br.com.mod.rhaegarClans.chat.AllyChatCommand;
import br.com.mod.rhaegarClans.chat.ClanChatCommand;
import br.com.mod.rhaegarClans.command.ClanCommand;
import br.com.mod.rhaegarClans.core.ClanConfig;
import br.com.mod.rhaegarClans.core.ClanManager;
import br.com.mod.rhaegarClans.integration.ClanExpansion;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class RhaegarClans extends JavaPlugin implements Listener {

    private static RhaegarClans instance;
    private ClanManager clanManager;
    private ClanConfig clanConfig;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("----------------------------------------");
        getLogger().info("⚔️  RhaegarClans carregado com sucesso! ⚔️");
        getLogger().info("----------------------------------------");

        // 1. Config e Manager
        this.clanConfig = new ClanConfig(this);
        this.clanManager = new ClanManager(this);

        // 2. Registra Eventos
        Bukkit.getPluginManager().registerEvents(this, this);

        // 3. Comandos
        ClanCommand clanCommandExecutor = new ClanCommand();
        this.getCommand("clan").setExecutor(clanCommandExecutor);
        this.getCommand("clan").setTabCompleter(clanCommandExecutor);

        // Corrigido: Verificar se os comandos existem antes de setar executor
        if (getCommand(".") != null) {
            getCommand(".").setExecutor(new ClanChatCommand(clanManager));
        }
        if (getCommand("ally") != null) {
            getCommand("ally").setExecutor(new AllyChatCommand(clanManager, clanConfig));
        }

        // 4. PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanExpansion(this).register();
            getLogger().info("✅ Expansão do PlaceholderAPI registrada.");
        }

        // 5. Popula o cache inicial
        clanManager.refreshAllCaches();
    }

    @Override
    public void onDisable() {
        if (this.clanConfig != null) {
            this.clanConfig.saveClans();
        }
        getLogger().info("❌ RhaegarClans desativado.");
    }

    // --- EVENTOS DE CACHE ---
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        clanManager.refreshPlayerCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clanManager.removePlayerCache(event.getPlayer().getUniqueId());
    }

    // --- Getters ---
    public static RhaegarClans getInstance() {
        return instance;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public ClanConfig getClanConfig() {
        return clanConfig;
    }
}