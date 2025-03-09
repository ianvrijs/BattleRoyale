package org.Foxraft.battleRoyale.config;

import org.Foxraft.battleRoyale.BattleRoyale;
import org.Foxraft.battleRoyale.managers.InviteManager;
import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.utils.StartUtils;
/**
 * This class gets used to store game manager dependencies (will convert to an actual DI container in the future)
 */
public class GameManagerConfig {
    private final BattleRoyale plugin;
    private TeamManager teamManager;
    private StartUtils startUtils;
    private GulagManager gulagManager;
    private PlayerManager playerManager;
    private InviteManager inviteManager;
    private GameManager gameManager;

    public GameManagerConfig(BattleRoyale plugin) {
        this.plugin = plugin;
    }

    public BattleRoyale getPlugin() {
        return plugin;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public void setTeamManager(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    public StartUtils getStartUtils() {
        return startUtils;
    }

    public void setStartUtils(StartUtils startUtils) {
        this.startUtils = startUtils;
    }

    public GulagManager getGulagManager() {
        return gulagManager;
    }

    public void setGulagManager(GulagManager gulagManager) {
        this.gulagManager = gulagManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public void setPlayerManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public InviteManager getInviteManager() {
        return inviteManager;
    }

    public void setInviteManager(InviteManager inviteManager) {
        this.inviteManager = inviteManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public GameState getCurrentGameState() {
        if (gameManager != null) {
            return gameManager.getCurrentState();
        }
        return GameState.LOBBY; //default
    }

}