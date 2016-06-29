package w.whatever.data.jpa.domain;

import com.google.common.collect.Lists;
import w.whatever.data.jpa.domain.Game;

import java.util.List;

/**
 * Created by rich on 10/17/15.
 */
public class GameStreak {

    private List<Game> games = Lists.newArrayList();

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    public void addGame(Game game) {
        games.add(game);
    }
}
