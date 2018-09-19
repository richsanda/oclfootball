package w.whatever.data.jpa.jobs.ocl.scraper;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import w.whatever.data.jpa.jobs.ocl.scraper.data.OclScraperGame;
import w.whatever.data.jpa.util.OclUtility;

/**
 * Created by rich on 11/24/17.
 */
public class OclScraperReader implements ItemReader<OclScraperGame> {

    /*

    max_team_id_2005 = 10
    max_team_id = 12
    max_scoring_period_id = 17
    max_season_id = 2015

    allowedErrors = 5

     */

    private final static Integer minTeam = 1;
    private final static Integer maxTeam2005 = 10;
    private final static Integer maxTeam = 12;

    private final static Integer minScoringPeriod = OclUtility.currentScoringPeriod;
    private final static Integer maxScoringPeriod = OclUtility.currentScoringPeriod;

    private final static Integer minSeason = 2018; // 2006;
    private final static Integer maxSeason = 2018;

    private Integer team = minTeam;
    private Integer scoringPeriod = minScoringPeriod;
    private Integer season = minSeason;

    @Override
    public OclScraperGame read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (isDone()) return null;
        OclScraperGame result = new OclScraperGame(team, scoringPeriod, season);
        increment();
        return result;
    }

    private void increment() {

        if (isMaxSeason() && isMaxScoringPeriod() && isMaxTeam()) {
            clear();
        } else if (isMaxScoringPeriod() && isMaxTeam()) {
            team = minTeam;
            scoringPeriod = minScoringPeriod;
            season++;
        } else if (isMaxTeam()) {
            team = minTeam;
            scoringPeriod++;
        } else {
            team++;
        }
    }

    private boolean isMaxSeason() {
        return maxSeason.equals(season);
    }

    private boolean isMaxScoringPeriod() {
        return maxScoringPeriod.equals(scoringPeriod);
    }

    private boolean isMaxTeam() {
        return (season == 2005 && maxTeam2005.equals(team)) || maxTeam.equals(team);
    }

    private void clear() {
        team = null;
        scoringPeriod = null;
        season = null;
    }

    private boolean isDone() {
        return null == season;
    }
}
