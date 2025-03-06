package org.Foxraft.battleRoyale.teams;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TeamHandler {
    private final Map<String, Team> teams = new HashMap<>();
    private final File teamsFile;

    public TeamHandler(JavaPlugin plugin) {
        this.teamsFile = new File(plugin.getDataFolder(), "teams.yml");
        loadTeams();
    }

    public void createTeam(Player player1, Player player2) {
        String teamId = String.valueOf(teams.size() + 1);
        Team team = new Team(teamId, Arrays.asList(player1.getName(), player2.getName()));
        teams.put(teamId, team);
        saveTeams();
    }

    public void addPlayerToTeam(Player player, String teamId) {
        Team team = teams.get(teamId);
        if (team != null) {
            team.getPlayers().add(player.getName());
            saveTeams();
        }
    }

    public void removePlayerFromTeam(Player player) {
        for (Team team : teams.values()) {
            if (team.getPlayers().remove(player.getName())) {
                saveTeams();
                break;
            }
        }
    }

    private void loadTeams() {
        if (!teamsFile.exists()) {
            return;
        }

        try (FileInputStream inputStream = new FileInputStream(teamsFile)) {
            Yaml yaml = new Yaml(new Constructor(new org.yaml.snakeyaml.LoaderOptions()));            Map<String, Map<String, Object>> data = yaml.load(inputStream);
            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                String teamId = entry.getKey();
                Map<String, Object> teamData = entry.getValue();
                List<String> players = (List<String>) teamData.get("players");
                long score = (long) teamData.get("score");
                teams.put(teamId, new Team(teamId, players, score));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}