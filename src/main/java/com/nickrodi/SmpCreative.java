package com.nickrodi;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

public class SmpCreative extends JavaPlugin {

    private LocationManager locationManager;
    private PlayerDataManager playerDataManager;
    private AdvancementTracker advancementTracker;
    private TeleportRequestManager teleportRequestManager;
    private WorldEditManager worldEditManager;
    private WarpManager warpManager;
    public static String WORLD_NAME = "world_creative";
    private static final String DEFAULT_WORLD_NAME = "world_creative";
    private static final int DEFAULT_WE_BLOCK_LIMIT = 50000;
    private int worldEditBlockLimit = DEFAULT_WE_BLOCK_LIMIT;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        WORLD_NAME = getConfig().getString("creative_world_name", DEFAULT_WORLD_NAME);
        worldEditBlockLimit = getConfig().getInt("worldedit_block_limit", DEFAULT_WE_BLOCK_LIMIT);

        // Auto generating world_creative if none is there yet
        if (Bukkit.getWorld(WORLD_NAME) == null) {
            File worldFolder = new File(getServer().getWorldContainer(), WORLD_NAME);
            
            if (worldFolder.exists()) {
                getLogger().log(Level.INFO, "Found existing world \"{0}\". Loading...", WORLD_NAME);
                new WorldCreator(WORLD_NAME).createWorld();
            } else {
                getLogger().log(Level.INFO, "World \"{0}\" not found. Generating a new creative world...", WORLD_NAME);
                
                WorldCreator creator = new WorldCreator(WORLD_NAME);
                creator.type(WorldType.FLAT);
                creator.generateStructures(false); 
                
                World createdWorld = creator.createWorld();

                if (createdWorld != null) {
                    createdWorld.setDifficulty(Difficulty.NORMAL);
                    createdWorld.setGameRule(GameRules.ADVANCE_TIME, false);
                    createdWorld.setGameRule(GameRules.ADVANCE_WEATHER, false);
                    createdWorld.setGameRule(GameRules.SPAWN_MOBS, false);
                    createdWorld.setGameRule(GameRules.SPAWN_MONSTERS, false);
                    
                    createdWorld.setTime(6000); 
                    getLogger().info("World generation completed successfully!");
                }
            }
        }
        
        // init managers
        this.locationManager = new LocationManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.advancementTracker = new AdvancementTracker(this);
        this.teleportRequestManager = new TeleportRequestManager();
        this.worldEditManager = new WorldEditManager(this, worldEditBlockLimit);
        this.warpManager = new WarpManager(this);

        getServer().getPluginManager().registerEvents(new CreativeWorldListener(this), this);
        getServer().getPluginManager().registerEvents(advancementTracker, this);

        // Commands
        if (getCommand("creative") != null) {
            getCommand("creative").setExecutor(new GameModeCommands(this));
        }
        if (getCommand("survival") != null) {
            getCommand("survival").setExecutor(new GameModeCommands(this));
        }
        if (getCommand("rtp") != null) {
            getCommand("rtp").setExecutor(new RandomTeleportCommand());
        }
        if (getCommand("tp") != null) {
            getCommand("tp").setExecutor(new TeleportRequestCommand(this.teleportRequestManager));
        }
        if (getCommand("tpaccept") != null) {
            getCommand("tpaccept").setExecutor(new TeleportAcceptCommand(this.teleportRequestManager));
        }
        if (getCommand("tpdecline") != null) {
            getCommand("tpdecline").setExecutor(new TeleportDeclineCommand(this.teleportRequestManager));
        }
        if (getCommand("warp") != null) {
            WarpCommand warpCommand = new WarpCommand(this.warpManager);
            getCommand("warp").setExecutor(warpCommand);
            getCommand("warp").setTabCompleter(warpCommand);
        }
        if (getCommand("spawn") != null) {
            getCommand("spawn").setExecutor(new SpawnCommand());
        }

        // give worldedit access to players in creative
        if (worldEditManager.isAvailable()) {
            getServer().getOnlinePlayers().forEach(player -> {
                if (player.getWorld().getName().equals(WORLD_NAME)) {
                    worldEditManager.enableFor(player);
                }
            });
        }

        getLogger().info("SMPCreative enabled!");
    }

    @Override
    public void onDisable() {
        if (worldEditManager != null) {
            worldEditManager.disableAll();
        }
        getLogger().info("SMPCreative disabled.");
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public AdvancementTracker getAdvancementTracker() {
        return advancementTracker;
    }

    public TeleportRequestManager getTeleportRequestManager() {
        return teleportRequestManager;
    }

    public WorldEditManager getWorldEditManager() {
        return worldEditManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }
}
