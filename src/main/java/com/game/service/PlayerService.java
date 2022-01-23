package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;

import java.util.List;

public interface PlayerService {

    Player savePlayer(Player player);

    Player getPlayer(Long id);

    Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalArgumentException;

    void deletePlayer(Player player);

    List<Player> getPlayers(
            String name,
            String title,
            String race,
            String profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel
    );

    List<Player> sortPlayers(List<Player> players, PlayerOrder order);

    List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize);

    boolean isPlayerValid(Player player);

    Integer computeUntilLevelExperience(Integer exp, Integer level);

    Integer computeLevel(Integer experience);
}
