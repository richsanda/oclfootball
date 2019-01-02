package w.whatever.data.jpa.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.*;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import w.whatever.data.jpa.domain.City;
import w.whatever.data.jpa.domain.Game;
import w.whatever.data.jpa.domain.PlayerWeek;
import w.whatever.data.jpa.domain.TeamWeek;
import w.whatever.data.jpa.service.GameService;
import w.whatever.data.jpa.service.data.CityRepository;
import w.whatever.data.jpa.service.data.GameRepository;
import w.whatever.data.jpa.util.OclUtility;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
            System.out.println(owner(teamNumber, 2018));

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
                        if (game.getSeason() == OclUtility.currentSeason) { // && game.getScoringPeriod() == OclUtility.currentScoringPeriod) {
                            int playerLastPointsBase = playerLastPoints.getOrDefault(playerId, 0);
                            playerLastPoints.put(playerId, points + playerLastPointsBase);
                            currentLineup.add(playerId);
                        }
                        playerNames.put(playerId, playerName);
                        playerPositions.put(playerId, playerPosition);
                    }
                }
            }

            List<PlayerPoints> result = Lists.newArrayList();
            SortedSet<PlayerPoints> currentLineupPlayerPoints = Sets.newTreeSet((p1, p2) -> new CompareToBuilder()
                    .append(p1.positionIndex(), p2.positionIndex())
                    .append(p1.playerName, p2.playerName)
                    .toComparison());

            for (String playerId : playerPoints.keySet()) {
                PlayerPoints pp = new PlayerPoints(null, null, playerNames.get(playerId), playerPositions.get(playerId), playerPoints.get(playerId), playerLastPoints.get(playerId), playerGames.get(playerId));
                result.add(pp);
                if (currentLineup.contains(playerId)) {
                    currentLineupPlayerPoints.add(pp);
                }
            }

            Collections.sort(result);

            sb.append(owner(teamNumber, 2018)).append(":\n");

            if (TOP_20) {
                int i = 0;
                for (PlayerPoints pp : result) {

                    i++;

                    // if (pp.points > 0) continue;

                    if (i <= countPerTeam || pp.lastPoints != null)
                    sb.append(i).append(". ").append(pp).append("\n");
                    if (i == countPerTeam) {
                        sb.append("-----\n");
                    }
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

    @RequestMapping(value = "/seasonStats", method= RequestMethod.GET, produces = "application/json")
    public @ResponseBody String seasonStats() {

        StringBuilder sb = new StringBuilder();

        int teamNumber = 1;
        while (teamNumber <= 12) {

            final Map<String, Integer> playerPoints = Maps.newTreeMap();
            final Map<String, Integer> individualPlayerPoints = Maps.newHashMap();
            Map<String, Integer> playerGames = Maps.newTreeMap();
            Map<String, Integer> playerLastPoints = Maps.newTreeMap();
            Multimap<String, String> individualPlayerIds = HashMultimap.create();
            Map<String, String> individualPlayerNames = Maps.newHashMap();
            Map<String, String> playerPositions = Maps.newHashMap();
            Map<Integer, Integer> wins = Maps.newHashMap();
            Map<Integer, Integer> losses = Maps.newHashMap();
            Map<Integer, Integer> ties = Maps.newHashMap();

            Set<String> currentLineup = Sets.newHashSet();

            Iterable<Game> games = gameRepository.findByTeamNumber(teamNumber);

            System.out.println();
            System.out.println(owner(teamNumber, 2018));

            for (Game game : games) {

                if (game.getSeason() != 2005 && game.isRegularSeasonGame()) {

                    if (game.isWin()) {
                        wins.put(game.getSeason(), wins.getOrDefault(game.getSeason(), 0) + 1);
                    }
                    if (game.isLoss()) {
                        losses.put(game.getSeason(), losses.getOrDefault(game.getSeason(), 0) + 1);
                    }
                    if (game.isTie()) {
                        ties.put(game.getSeason(), ties.getOrDefault(game.getSeason(), 0) + 1);
                    }

                    TeamWeek teamWeek = game.getTeamWeek();
                    for (PlayerWeek playerWeek : teamWeek.getPlayerWeeks()) {

                        // playerId here = season + positionIndex
                        int playerPosition = positionIndex(playerWeek.getPosition());
                        String playerId = String.format("%d:%d:%d", teamNumber, game.getSeason(), playerPosition);

                        Integer points = playerWeek.getPoints();
                        Integer basePoints = playerPoints.getOrDefault(playerId, 0);
                        Integer baseGames = playerGames.getOrDefault(playerId, 0);
                        String playerName = playerWeek.getPlayerName();
                        playerPoints.put(playerId, points + basePoints);
                        playerGames.put(playerId, baseGames + 1);
                        if (game.getSeason() == OclUtility.currentSeason && game.getScoringPeriod() == OclUtility.currentScoringPeriod) {
                            int playerLastPointsBase = playerLastPoints.getOrDefault(playerId, 0);
                            playerLastPoints.put(playerId, points + playerLastPointsBase);
                            currentLineup.add(playerId);
                        }
                        playerPositions.put(playerId, playerWeek.getPosition());

                        String individualPlayerId = String.format("%s:%s", playerId, playerWeek.getPlayerId());
                        individualPlayerIds.put(playerId, individualPlayerId);
                        Integer baseIndividualPoints = individualPlayerPoints.getOrDefault(individualPlayerId, 0);
                        individualPlayerPoints.put(individualPlayerId, points + baseIndividualPoints);
                        individualPlayerNames.put(individualPlayerId, playerWeek.getPlayerName());
                    }
                }
            }

            List<PlayerPoints> result = Lists.newArrayList();
            SortedSet<PlayerPoints> currentLineupPlayerPoints = Sets.newTreeSet(new Comparator<PlayerPoints>() {
                @Override
                public int compare(PlayerPoints p1, PlayerPoints p2) {
                    return new CompareToBuilder()
                            .append(p2.positionIndex(), p1.positionIndex())
                            .append(p2.playerName, p1.playerName)
                            .toComparison();
                }
            });

            for (String playerId : playerPoints.keySet()) {

                String[] playerIdParts = playerId.split(":");
                Integer playerTeam = Integer.valueOf(playerIdParts[0]);
                Integer playerSeason = Integer.valueOf(playerIdParts[1]);

                SortedMap<String, Integer> individuals = new TreeMap<>();

                try {
                    individuals.putAll(individualPlayerIds.get(playerId)
                            .stream()
                            .collect(Collectors.toMap(id -> id, individualPlayerPoints::get)));
                } catch (Exception e) {
                    System.out.println("here");
                }

                PlayerPoints pp = new PlayerPoints(
                        playerTeam,
                        playerSeason,
                        individuals.entrySet()
                                .stream()
                                .sorted((o1, o2) -> new CompareToBuilder().append(o2.getValue(), o1.getValue()).toComparison())
                                .limit(4)
                                .filter(e -> percentage(e.getValue(), playerPoints.get(playerId)) >= 12.5)
                                .map(e -> String.format("%s %s%%", individualPlayerNames.get(e.getKey()), String.format("%1$,.1f", percentage(e.getValue(), playerPoints.get(playerId)))))
                                .collect(Collectors.joining(", ")),
                        playerPositions.get(playerId),
                        playerPoints.get(playerId),
                        playerLastPoints.get(playerId),
                        playerGames.get(playerId));
                result.add(pp);

                if (currentLineup.contains(playerId)) {
                    currentLineupPlayerPoints.add(pp);
                }
            }

            result.sort((p2, p1) -> new CompareToBuilder()
                    .append(p2.positionIndex(), p1.positionIndex())
                    .append(p1.points, p2.points)
                    .append(p2.playerName, p1.playerName)
                    .toComparison());

            if (TOP_20) {
                int i = 0;
                int positionIndex = 0;
                for (PlayerPoints pp : result) {

                    if (pp.positionIndex() != positionIndex) {
                        positionIndex = pp.positionIndex();
                        i = 0;
                        int rank = 1;
                        int currentSeasonPoints = 0;
                        for (PlayerPoints season : result.stream().filter(makeSeasonPredicate(positionIndex)).collect(Collectors.toList())) {
                            if (season.season == 2018) {
                                currentSeasonPoints = season.points;
                                break;
                            }
                            rank++;
                        }
                        sb
                                .append('\n')
                                .append(pp.playerPosition)
                                .append(", ")
                                .append(owner(teamNumber, 2018))
                                .append(" 2018: ")
                                .append(currentSeasonPoints)
                                .append(" (")
                                .append(rank)
                                .append(")\n\n");
                    }

                    i++;

                    // if (pp.points > 0) continue;

                    if (i <= countPerTeam || pp.lastPoints != null) {
                        if (pp.season == 2018) sb.append("* ");
                        sb.append(i).append(". ");
                        sb.append(pp.points);
                        sb.append(" ");
                        sb.append(pp.playerPosition);
                        sb.append(", ");
                        sb.append(owner(pp.team, pp.season));
                        sb.append(" ");
                        sb.append(pp.season);
                        sb.append(" (");
                        sb.append(wins.getOrDefault(pp.season, 0));
                        sb.append('-');
                        sb.append(losses.getOrDefault(pp.season, 0));
                        if (ties.containsKey(pp.season)) {
                            sb.append('-');
                            sb.append(ties.get(pp.season));
                        }
                        sb.append(") ");
                        sb.append(pp.playerName);
                        sb.append("\n");
                    }
                    if (i == countPerTeam) {
                        sb.append("-----\n");
                    }
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

        private final Integer team;
        private final Integer season;
        private final String playerName;
        private final String playerPosition;
        private final Integer points;
        private final Integer lastPoints;
        private final Integer games;
        private final double average;

        private PlayerPoints(Integer team, Integer season, String playerName, String playerPosition, Integer points, Integer lastPoints, Integer games) {
            this.team = team;
            this.season = season;
            this.playerName = playerName;
            this.playerPosition = playerPosition;
            this.points = points;
            this.lastPoints = lastPoints;
            this.games = games;
            this.average = (double)(points) / (double)(games);
        }

        public int positionIndex() {
            return OclRestController.positionIndex(playerPosition);
        }

        @Override
        public int compareTo(PlayerPoints o) {
            return new CompareToBuilder().append(o.points, this.points).build();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof PlayerPoints && ((PlayerPoints) o).playerName.equals(playerName) && ((PlayerPoints) o).season.equals(season);
        }

        @Override
        public String toString() {
            if (TOP_20) {

                StringBuilder sb = new StringBuilder(String.format("%s, %s: %d", playerPosition, playerName.contains(":") ? playerName.split(":")[1] : playerName, points));
                if (null != lastPoints) {
                    writeLastPoints(sb);
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

        private void writeLastPoints(StringBuilder sb) {

            String average = String.format("%1$,.1f", (double)points / (double)games);

            sb.append(" (");
            if (lastPoints >= 0) {
                sb.append("+");
            }
            sb.append(lastPoints);
            sb.append(", ");
            sb.append(games);
            sb.append(" * ");
            sb.append(average);
            sb.append(")");
        }
    }

    private static int positionIndex(String playerPosition) {
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

    private static String position(int playerPosition) {
       switch (playerPosition) {
            case 1: return "QB";
            case 2: return "RB";
            case 3: return "WR";
            case 4: return "TE";
            case 5: return "D/ST";
            case 6: return "K";
            default: return null;
        }
    }

    private static String owner(int team, int season) {
        switch (team) {
            case 1:
                return "trav";
            case 2:
                return "nick";
            case 3:
                return "grum";
            case 4:
                return "justin";
            case 5:
                return season < 2016 ? "rux" : "beid";
            case 6:
                return "rich";
            case 7:
                return "greg";
            case 8:
                return "spoth";
            case 9:
                return season < 2016 ? "mbug" : "fussti";
            case 10:
                return "argo";
            case 11:
                return "bill";
            case 12:
                return season < 2015 ? "ruggs" : "dodge";
        }
        /*
            switch (teamNumber) {
        case 1:
            return "trav";
            break;
        case 2:
            return "nick";
            break;
        case 3:
            return "grum";
            break;
        case 4:
            return "justin";
            break;
        case 5:
            return season < 2016 ? "rux" : "beid";
            break;
        case 6:
            return "rich";
            break;
        case 7:
            return "greg";
            break;
        case 8:
            return "spoth";
            break;
        case 9:
            return season < 2016 ? "mbug" : "fussti";
            break;
        case 10:
            return "argo";
            break;
        case 11:
            return "bill";
            break;
        case 12:
            return season < 2015 ? "ruggs" : "dodge";
            break;
    }
         */
        return null;
    }

    private static Double percentage(Integer partial, Integer total) {
        return (double)partial / (double)total * (double)100;
    }

    private static Predicate<PlayerPoints> makeSeasonPredicate(final int positionIndex) {
        return playerPoints -> playerPoints.positionIndex() == positionIndex;
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
            @RequestParam(required = false) Boolean ties,
            @RequestParam(required = false) Boolean ruxbees,
            @RequestParam(required = false) Boolean bugtons,
            @RequestParam(required = false) Boolean sortByTotal
    ) {

        startSeason = startSeason == null ? 2006 : startSeason;
        startWeek = startWeek == null ? 1 : startWeek;
        endSeason = endSeason == null ? OclUtility.currentSeason : endSeason;
        endWeek = endWeek == null ? 16 : endWeek;
        teams = teams == null ? Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12) : teams;
        wins = wins == null ? true : wins;
        losses = losses == null ? true : losses;
        ties = ties == null ? true : ties;
        ruxbees = ruxbees == null ? false : ruxbees;
        bugtons = bugtons == null ? false : bugtons;
        sortByTotal = sortByTotal == null ? false : sortByTotal;

        Page<Game> games = gameRepository.findHighestScoringGames(
                startSeason, startWeek, endSeason, endWeek, teams, wins, losses, ties, ruxbees, bugtons, new PageRequest(0, 100, Sort.Direction.DESC, sortByTotal ? "totalPoints" : "points"));
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
