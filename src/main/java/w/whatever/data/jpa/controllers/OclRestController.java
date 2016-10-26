package w.whatever.data.jpa.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import w.whatever.data.jpa.domain.Game;
import w.whatever.data.jpa.jobs.ocl.load.OclLoadJobRunner;
import w.whatever.data.jpa.service.GameService;
import w.whatever.data.jpa.service.data.CityRepository;
import w.whatever.data.jpa.domain.City;
import w.whatever.data.jpa.service.data.GameRepository;

import java.util.Iterator;
import java.util.List;

/**
 * Created by rich on 10/10/15.
 */
@RestController
public class OclRestController {

    @Autowired
    CityRepository cityRepository;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    GameService gameService;

    @Autowired
    OclLoadJobRunner oclLoadJobRunner;

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
        endSeason = endSeason == null ? 2015 : endSeason;
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
