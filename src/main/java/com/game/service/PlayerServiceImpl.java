package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService{
    private PlayerRepository playerRepository;

    public PlayerServiceImpl(){}

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        super();
        this.playerRepository = playerRepository;
    }



    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public Player getPlayer(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalArgumentException {
        boolean shouldChangeRating = false;

        final String name = newPlayer.getName();
        if (name != null) {
            if (isStringValid(name, "name")) {
                oldPlayer.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }
        final String title = newPlayer.getTitle();
        if (title != null) {
            if (isStringValid(title, "title")) {
                oldPlayer.setTitle(title);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newPlayer.getRace() != null) {
            oldPlayer.setRace(newPlayer.getRace());
        }
        if (newPlayer.getProfession() != null) {
            oldPlayer.setProfession(newPlayer.getProfession());
        }
        final Date birthday = newPlayer.getBirthday();
        if (birthday != null) {
            if (isBirthdayValid(birthday)) {
                oldPlayer.setBirthday(birthday);
                shouldChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newPlayer.isBanned() != null) {
            oldPlayer.setBanned(newPlayer.isBanned());
            shouldChangeRating = true;
        }
        final Integer experience = newPlayer.getExperience();
        if (experience != null) {
            if (isExperienceValid(experience)) {
                oldPlayer.setExperience(experience);
                shouldChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (shouldChangeRating) {
            final Integer level = computeLevel(oldPlayer.getExperience());
            oldPlayer.setLevel(level);

            final Integer untilLevelExperience = computeUntilLevelExperience(oldPlayer.getExperience(), oldPlayer.getLevel());
            oldPlayer.setUntilNextLevel(untilLevelExperience);
        }
        playerRepository.save(oldPlayer);
        return oldPlayer;
    }

    @Override
    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }

    @Override
    public List<Player> getPlayers(String name, String title, String race, String profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Player> list = new ArrayList<>();
        playerRepository.findAll().forEach((player -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != Race.valueOf(race)) return;
            if (profession != null && player.getProfession() != Profession.valueOf(profession)) return;
            if (afterDate != null && player.getBirthday().before(afterDate)) return;
            if (beforeDate != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.isBanned() != banned) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;

            list.add(player);
        }));

        return list;
    }


    @Override
    public List<Player> sortPlayers(List<Player> players, PlayerOrder order) {
        if (order == null) {
            order = PlayerOrder.ID;
        }
        PlayerOrder finalOrder = order;
        players.sort((player1, player2) -> {
                switch (finalOrder) {
                    case ID: return player1.getId().compareTo(player2.getId());
                    case NAME: return player1.getName().compareTo(player2.getName());
                    case EXPERIENCE: return player1.getExperience().compareTo(player2.getExperience());
                    case BIRTHDAY: return player1.getBirthday().compareTo(player2.getBirthday());
                    case LEVEL: return player1.getLevel().compareTo(player2.getLevel());
                    default: return 0;
                }
            });
        return players;
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }

    @Override
    public boolean isPlayerValid(Player player) {
        return player != null && isStringValid(player.getName(), "name") && isStringValid(player.getTitle(), "title")
                && isBirthdayValid(player.getBirthday()) && player.getRace() != null && player.getProfession() != null && isExperienceValid(player.getExperience());

    }

    @Override
    public Integer computeUntilLevelExperience(Integer exp, Integer level) {
        return 50 * (level+1) * (level+2) - exp;
    }

    @Override
    public Integer computeLevel(Integer experience) {
        return (int) ((Math.sqrt(2500 + 200 * experience) - 50) / 100);
    }

    private boolean isExperienceValid(Integer exp) {
        final int minExperience = 0;
        final int maxExperience = 10_000_000;
        return exp != null && exp.compareTo(minExperience) >= 0 && exp.compareTo(maxExperience) <= 0;
    }

    private boolean isStringValid(String value, String parameter) {
        if (parameter.equalsIgnoreCase("name")) {
            return value != null && value.length() <= 12;
        }
        if (parameter.equalsIgnoreCase("title")) {
            return value != null && value.length() <= 30;
        }
        return false;
    }

    private boolean isBirthdayValid(Date birthday) {
        final Date start = getDateForYear(2000);
        final Date end = getDateForYear(3000);
        return birthday != null && birthday.after(start) && birthday.before(end);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }
}
