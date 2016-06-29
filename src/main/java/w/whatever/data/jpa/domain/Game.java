package w.whatever.data.jpa.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by rich on 10/15/15.
 */
@XmlRootElement(name = "game")
@Entity
public class Game {

    @Id
   	@GeneratedValue
   	private Long id;

    private String file;
    @Column(nullable = false)
    private int season;
    @Column(nullable = false)
    private int scoringPeriod;
    @Column(nullable = false)
    private int teamNumber;
    private int points;
    private int opponentPoints;
    private boolean win;
    private boolean loss;
    private boolean tie;
    private boolean initialized = false;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TeamWeek> teamWeeks;

    @XmlElement(name = "team")
    @JsonIgnore
    public List<TeamWeek> getTeamWeeks() {
        return teamWeeks;
    }

    public void setTeamWeeks(List<TeamWeek> teamWeeks) {
        this.teamWeeks = teamWeeks;
    }

    @XmlAttribute(name = "file")
    @JsonIgnore
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public int getScoringPeriod() {
        return scoringPeriod;
    }

    public void setScoringPeriod(int scoringPeriod) {
        this.scoringPeriod = scoringPeriod;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void init() {

        try {

            String[] fileSplit = file.split("/");
            String fileName = fileSplit[fileSplit.length - 1];
            String[] fileNameSplit = fileName.split("\\.");

            season = Integer.parseInt(fileNameSplit[0]);
            scoringPeriod = Integer.parseInt(fileNameSplit[1]);
            teamNumber = Integer.parseInt(fileNameSplit[2]);

            initialized = true;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            // pass
        }


        for (TeamWeek teamWeek : teamWeeks) {
            teamWeek.init();
        }

        points = teamWeeks.get(0).getPoints();
        opponentPoints = teamWeeks.get(opponentTeamWeekIndex()).getPoints();
        win = points > opponentPoints;
        tie = points == opponentPoints;
        loss = points < opponentPoints;
    }

    @Override
    public String toString() {
        return "" + season + "; " + scoringPeriod + "; " + teamNumber;
    }

    public boolean isWin() {
        return win;
    }

    public boolean isTie() {
        return tie;
    }

    public boolean isLoss() {
        return loss;
    }

    @JsonIgnore
    public boolean isWinOrTie() {
        return isWin() || isTie();
    }

    @JsonIgnore
    public boolean isLossOrTie() {
        return isLoss() || isTie();
    }

    public boolean isRegularSeasonGame() {
        return scoringPeriod <= 14;
    }

    public boolean isPlayoffGame() {
        return !isRegularSeasonGame();
    }

    @JsonIgnore
    public boolean isLastGameOfSeason(boolean includePlayoffs) {
        if (includePlayoffs && scoringPeriod == 16) {
            return true;
        } else if (scoringPeriod == 14) {
            return true;
        }
        return false;
    }

    @JsonIgnore
    public boolean isFirstGameOfSeason() {
        return scoringPeriod == 1;
    }

    @JsonProperty(value = "team")
    public TeamWeek getTeamWeek() {
        return teamWeeks.get(0);
    }

    @JsonProperty(value = "opponent")
    public TeamWeek opponentTeamWeek() {
        return teamWeeks.get(opponentTeamWeekIndex());
    }

    // is this game right after other ?
    public boolean isNextTo(Game other, boolean includePlayoffs) {
        if (null == other) return false;
        if (teamNumber != other.getTeamNumber()) return false; // throw exception instead ?
        if (isFirstGameOfSeason()) {
            return season - 1 == other.getSeason() && other.isLastGameOfSeason(includePlayoffs);
        } else if (scoringPeriod - 1 == other.getScoringPeriod()) {
            return true;
        }
        return false;
    }

    public boolean isPreviousTo(Game other, boolean includePlayoffs) {
        return other.isNextTo(this, includePlayoffs);
    }

    // in 2015, bench "teams" began recording, so from then on, get team week at index 2 instead of 1
    private int opponentTeamWeekIndex() {
        return season >= 2015 ? 2 : 1;
    }
}
