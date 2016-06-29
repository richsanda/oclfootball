package w.whatever.data.jpa.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * Created by rich on 10/15/15.
 */
@Entity
public class TeamWeek {

    @Id
   	@GeneratedValue
   	private Long id;

    private String header;
    private int points = 0;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PlayerWeek> playerWeeks;


    @XmlElement(name = "header")
    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @XmlElementWrapper(name = "players")
    @XmlElement(name = "player")
    @JsonProperty(value = "player")
    public List<PlayerWeek> getPlayerWeeks() {
        return playerWeeks;
    }

    public void setPlayerWeeks(List<PlayerWeek> playerWeeks) {
        this.playerWeeks = playerWeeks;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void init() {
        for (PlayerWeek playerWeek : playerWeeks) {
            points += playerWeek.getPoints();
            playerWeek.init();
        }
    }
}
