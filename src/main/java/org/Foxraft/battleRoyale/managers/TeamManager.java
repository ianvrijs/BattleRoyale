package org.Foxraft.battleRoyale.managers;

import org.Foxraft.battleRoyale.events.TeamLeaveEvent;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Manages teams and their players.
 *  depends on: Team, JavaPlugin
 */
public class TeamManager {
    private final Map<String, Team> teams = new ConcurrentHashMap<>();
    private final File teamsFile;
    private final PlayerManager playerManager;

    public TeamManager(JavaPlugin plugin, PlayerManager playerManager) {
        this.teamsFile = new File(plugin.getDataFolder(), "teams.yml");
        this.playerManager = playerManager;
        loadTeams();
    }

    public void createTeam(Player player1, Player player2) {
        Team player1Team = getTeam(player1);
        if (player1Team != null && player1Team.getPlayers().size() == 1) {
            removePlayerFromTeam(player1);
        }

        if (isPlayerInAnyTeam(player1) || isPlayerInAnyTeam(player2)) {
            Bukkit.getLogger().warning("One or both players are already in a team.");
            return;
        }

        String teamId = String.valueOf(teams.size() + 1);
        Team team = new Team(teamId, new ArrayList<>(Arrays.asList(player1.getName(), player2.getName())));
        teams.put(teamId, team);
        saveTeams();
    }

    public void addPlayerToTeam(Player player, String teamId) {
        Team team = teams.get(teamId);
        if (team != null) {
            synchronized (team) {
                if (team.getPlayers().contains(player.getName())) {
                    Bukkit.getLogger().warning("Player is already in the team.");
                    return;
                }
                if (team.getPlayers().size() >= 2) {
                    Bukkit.getLogger().warning("Team is already full.");
                    return;
                }
                team.getPlayers().add(player.getName());
            }
            saveTeams();
        }
    }
    public void removePlayerFromTeam(Player player) {
        synchronized (teams) {
            Iterator<Map.Entry<String, Team>> iterator = teams.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Team> entry = iterator.next();
                Team team = entry.getValue();
                synchronized (team) {
                    if (team.getPlayers().contains(player.getName())) {
                        for (String teammateName : team.getPlayers()) {
                            if (!teammateName.equals(player.getName())) {
                                Player teammate = Bukkit.getPlayer(teammateName);
                                if (teammate != null && teammate.isOnline()) {
                                    teammate.sendMessage(ChatColor.RED + player.getName() + " has left your team.");
                                }
                            }
                        }

                        team.getPlayers().remove(player.getName());
                        if (team.getPlayers().isEmpty()) {
                            iterator.remove();
                        }
                        saveTeams();
                        return;
                    }
                }
            }
        }
    }

    public Map<String, Team> getTeams() {
        return teams;
    }

    private void loadTeams() {
        if (!teamsFile.exists()) {
            return;
        }

        try (FileInputStream inputStream = new FileInputStream(teamsFile)) {
            Yaml yaml = new Yaml(new Constructor(new org.yaml.snakeyaml.LoaderOptions()));
            Map<String, Map<String, Object>> data = yaml.load(inputStream);
            if (data == null) {
                data = new HashMap<>();
            }
            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                String teamId = entry.getKey();
                Map<String, Object> teamData = entry.getValue();
                List<String> players = new ArrayList<>((List<String>) teamData.get("players"));
                Object scoreObj = teamData.get("score");
                long score = (scoreObj instanceof Integer) ? ((Integer) scoreObj).longValue() : (Long) scoreObj;
                teams.put(teamId, new Team(teamId, players, score));
            }

        } catch (IOException | ClassCastException e) {
            Bukkit.getLogger().warning("Failed to load teams: " + e.getMessage());
        }
    }

    private void saveTeams() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        for (Team team : teams.values()) {
            Map<String, Object> teamData = new HashMap<>();
            teamData.put("players", team.getPlayers());
            teamData.put("score", team.getScore());
            data.put(team.getId(), teamData);
        }

        try (FileWriter writer = new FileWriter(teamsFile)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(data, writer);
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to save teams: " + e.getMessage());
        }
    }

    public boolean isPlayerInAnyTeam(Player player) {
        return teams.values().stream().anyMatch(team -> team.getPlayers().contains(player.getName()));
    }
    public void listTeams(CommandSender sender, int page) {
        int teamsPerPage = 10;
        List<Team> teams = new ArrayList<>(getTeams().values());
        int totalPages = (int) Math.ceil((double) teams.size() / teamsPerPage);

        if (page < 1 || page > totalPages) {
            sender.sendMessage(ChatColor.RED + "Page number out of range. There are " + totalPages + " pages.");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "Teams (Page " + page + "/" + totalPages + "):");
        int start = (page - 1) * teamsPerPage;
        int end = Math.min(start + teamsPerPage, teams.size());

        for (int i = start; i < end; i++) {
            Team team = teams.get(i);
            String players = String.join(", ", team.getPlayers());
            sender.sendMessage(ChatColor.YELLOW  +  "ID:" + ChatColor.GRAY + team.getName() + ChatColor.YELLOW +" - Players: " + ChatColor.GRAY +players);
        }
    }
    public void createSoloTeam(Player player) {
        String teamId = String.valueOf(teams.size() + 1);
        Team team = new Team(teamId, new ArrayList<>(List.of(player.getName()))); // Ensures mutability
        teams.put(teamId, team);
        saveTeams();
        player.sendMessage(ChatColor.GREEN + "You have been put into a new solo team.");
    }
    public String getPlayerTeam(Player player) {
        for (Team team : teams.values()) {
            if (team.getPlayers().contains(player.getName())) {
                return team.getId();
            }
        }
        return null;
    }

    public Team getTeam(Player player) {
        for (Team team : teams.values()) {
            if (team.getPlayers().contains(player.getName())) {
                return team;
            }
        }
        return null;
    }

    public boolean isTeamEliminated(String id) {
        Team team = teams.get(id);
        if (team == null) {
            return true;
        }

        for (String playerName : team.getPlayers()) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                PlayerState state = playerManager.getPlayerState(player);
                if (state == PlayerState.ALIVE ||
                        state == PlayerState.RESURRECTED ||
                        state == PlayerState.GULAG) {
                    return false;
                }
            }
        }
        return true;
    }

    public Player getTeammate(Player player) {
        Team team = getTeam(player);
        if (team == null || team.getPlayers().size() != 2) {
            return null;
        }

        for (String playerName : team.getPlayers()) {
            if (!playerName.equals(player.getName())) {
                return Bukkit.getPlayer(playerName);
            }
        }
        return null;
    }


    public void clearTeams() {
        teams.clear();
        saveTeams();
    }
}