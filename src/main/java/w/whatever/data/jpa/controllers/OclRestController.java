package w.whatever.data.jpa.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import w.whatever.data.jpa.domain.Game;
import w.whatever.data.jpa.domain.PlayerWeek;
import w.whatever.data.jpa.domain.TeamWeek;
import w.whatever.data.jpa.service.GameService;
import w.whatever.data.jpa.service.data.CityRepository;
import w.whatever.data.jpa.domain.City;
import w.whatever.data.jpa.service.data.GameRepository;
import w.whatever.data.jpa.util.OclUtility;

import java.math.BigDecimal;
import java.util.*;

import static w.whatever.data.jpa.util.OclUtility.countPerTeam;

/**
 * Created by rich on 10/10/15.
 */
@RestController
public class OclRestController {

    private static final boolean TOP_20 = true;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    GameService gameService;

    //@Autowired
    //OclLoadJobRunner oclLoadJobRunner;

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @RequestMapping(value = "/city", method= RequestMethod.GET, produces = "application/json")
    public City giveMeACity(@RequestParam String name) {
        City city = cityRepository.findByNameAndCountryAllIgnoringCase(name, "USA");
        return city;
    }

    /*
    @RequestMapping(value = "/load", method= RequestMethod.GET, produces = "application/html")
    public String loadOclData() throws Exception {
        oclLoadJobRunner.run();
        return "Done";
    }
    */

    @RequestMapping(value = "/stats", method= RequestMethod.GET, produces = "application/json")
    public @ResponseBody String giveMeStats() {

        StringBuilder sb = new StringBuilder();

        int teamNumber = 1;
        while (teamNumber <= 12) {

            final Map<String, Integer> playerPoints = Maps.newTreeMap();
            Map<String, Integer> playerGames = Maps.newTreeMap();
            Map<String, Integer> playerLastPoints = Maps.newTreeMap();
            Map<String, String> playerNames = Maps.newHashMap();
            Map<String, String> playerPositions = Maps.newHashMap();

            Set<String> currentLineup = Sets.newHashSet();

            Iterable<Game> games = gameRepository.findByTeamNumber(teamNumber);

            System.out.println();
            System.out.println("Team " + teamNumber);

            for (Game game : games) {
                if (game.getSeason() != 2005) {
                    TeamWeek teamWeek = game.getTeamWeek();
                    for (PlayerWeek playerWeek : teamWeek.getPlayerWeeks()) {
                        String playerId = playerWeek.getPlayerId();
                        Integer points = playerWeek.getPoints();
                        Integer basePoints = playerPoints.containsKey(playerId) ? playerPoints.get(playerId) : 0;
                        Integer baseGames = playerGames.containsKey(playerId) ? playerGames.get(playerId) : 0;
                        String playerName = playerWeek.getPlayerName();
                        String playerPosition = playerWeek.getPosition();
                        playerPoints.put(playerId, points + basePoints);
                        playerGames.put(playerId, baseGames + 1);
                        if (game.getSeason() == OclUtility.currentSeason && game.getScoringPeriod() == OclUtility.currentScoringPeriod) {
                            int playerLastPointsBase = playerLastPoints.containsKey(playerId) ? playerLastPoints.get(playerId) : 0;
                            playerLastPoints.put(playerId, points + playerLastPointsBase);
                            currentLineup.add(playerId);
                        }
                        playerNames.put(playerId, playerName);
                        playerPositions.put(playerId, playerPosition);
                    }
                }
            }

            List<PlayerPoints> result = Lists.newArrayList();
            SortedSet<PlayerPoints> currentLineupPlayerPoints = Sets.newTreeSet(new Comparator<PlayerPoints>() {
                @Override
                public int compare(PlayerPoints p1, PlayerPoints p2) {
                    return new CompareToBuilder()
                            .append(p1.positionIndex(), p2.positionIndex())
                            .append(p1.playerName, p2.playerName)
                            .toComparison();
                }
            });

            for (String playerId : playerPoints.keySet()) {
                PlayerPoints pp = new PlayerPoints(playerNames.get(playerId), playerPositions.get(playerId), playerPoints.get(playerId), playerLastPoints.get(playerId), playerGames.get(playerId));
                result.add(pp);
                if (currentLineup.contains(playerId)) {
                    currentLineupPlayerPoints.add(pp);
                }
            }

            Collections.sort(result);

            sb.append("Team ").append(teamNumber).append(":\n");

            if (TOP_20) {
                int i = 1;
                for (PlayerPoints pp : result) {
                    sb.append(i++).append(". ").append(pp).append("\n");
                    if (i > countPerTeam) break;
                }
            } else {

                for (PlayerPoints pp : currentLineupPlayerPoints) {
                    sb.append(pp).append("\n");
                }
            }
            sb.append("\n");
            teamNumber++;
        }

        return sb.toString();
    }

    public static class PlayerPoints implements Comparable<PlayerPoints> {

        private final String playerName;
        private final String playerPosition;
        private final Integer points;
        private final Integer lastPoints;
        private final Integer games;
        private final double average;

        private PlayerPoints(String playerName, String playerPosition, Integer points, Integer lastPoints, Integer games) {
            this.playerName = playerName;
            this.playerPosition = playerPosition;
            this.points = points;
            this.lastPoints = lastPoints;
            this.games = games;
            this.average = (double)(points) / (double)(games);
        }

        public int positionIndex() {
            if (StringUtils.isEmpty(playerPosition)) {
                return 10;
            }
            switch (playerPosition.charAt(0)) {
                case 'Q': return 1;
                case 'R': return 2;
                case 'W': return 3;
                case 'T': return 4;
                case 'D': return 5;
                case 'K': return 6;
                default: return 7;
            }
        }

        @Override
        public int compareTo(PlayerPoints o) {
            return new CompareToBuilder().append(o.points, this.points).build();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof PlayerPoints && ((PlayerPoints) o).playerName.equals(playerName);
        }

        @Override
        public String toString() {
            if (TOP_20) {

                StringBuilder sb = new StringBuilder(String.format("%s: %d", playerName, points));
                if (null != lastPoints) {
                    sb.append(" (");
                    if (lastPoints >= 0) {
                        sb.append("+");
                    }
                    sb.append(lastPoints);
                    sb.append(")");
                }

                return sb.toString();

            } else {

                String average = String.format("%1$,.1f", (double)points / (double)games);
                StringBuilder last = new StringBuilder();
                if (null != lastPoints) {
                    //last.append(" (");
                    if (lastPoints >= 0) {
                        last.append("+");
                    }
                    last.append(lastPoints);
                    //last.append(")");
                }

                return String.format("%s %s: %s =%d /%d ^%s", playerPosition, playerName, last.toString(), points, games, average);
            }
        }
    }

    @RequestMapping(value = "/game", method= RequestMethod.GET, produces = "application/json")
    public @ResponseBody Game giveMeAGame(@RequestParam Integer season, @RequestParam Integer scoringPeriod, @RequestParam Integer team) {
        Game game = gameRepository.findBySeasonAndScoringPeriodAndTeamNumber(season, scoringPeriod, team);
        return game;
    }

    @RequestMapping(value = "/highestPointsGames", method= RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<Game> highestScoringGames() {
        Page<Game> games = gameRepository.findHighestScoringGames(new PageRequest(0, 100, Sort.Direction.DESC, "points"));
        return games.getContent();
    }

    @RequestMapping(value = "/games/highestScores", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<Game> highestScoringGames(
            @RequestParam(required = false) Integer startSeason,
            @RequestParam(required = false) Integer startWeek,
            @RequestParam(required = false) Integer endSeason,
            @RequestParam(required = false) Integer endWeek,
            @RequestParam(required = false) List<Integer> teams,
            @RequestParam(required = false) Boolean wins,
            @RequestParam(required = false) Boolean losses,
            @RequestParam(required = false) Boolean ties
    ) {

        startSeason = startSeason == null ? 2006 : startSeason;
        startWeek = startWeek == null ? 1 : startWeek;
        endSeason = endSeason == null ? 2017 : endSeason;
        endWeek = endWeek == null ? 16 : endWeek;
        teams = teams == null ? Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12) : teams;
        wins = wins == null ? true : wins;
        losses = losses == null ? true : losses;
        ties = ties == null ? true : ties;

        Page<Game> games = gameRepository.findHighestScoringGames(
                startSeason, startWeek, endSeason, endWeek, teams, wins, losses, ties, new PageRequest(0, 100, Sort.Direction.DESC, "points"));
        return games.getContent();
    }

    @RequestMapping(value = "/oneGame", method= RequestMethod.GET, produces = "application/json")
    public @ResponseBody Game oneGame(int number) {
        Page<Game> games = gameRepository.findHighestScoringGames(new PageRequest(number, 1, Sort.Direction.DESC, "points"));
        return games.iterator().next();
    }

    @RequestMapping(value = "/streak", method= RequestMethod.GET, produces = "application/json")
    public @ResponseBody Game findStreaks(int teamNumber) {
        Iterable<Game> games = gameRepository.findAll();
        return games.iterator().next();
    }

    @RequestMapping(value = "/gamesAbovePoints", method= RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<Game> findGamesAbovePoints(int teamNumber, int points) {
        Iterable<Game> games = gameRepository.findGamesAbovePoints(teamNumber, points);
        return Lists.newArrayList(games);
    }
}
