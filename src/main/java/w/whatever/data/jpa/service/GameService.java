package w.whatever.data.jpa.service;

import w.whatever.data.jpa.domain.Game;
import w.whatever.data.jpa.domain.GameStreak;

import java.util.List;

/**
 * Created by rich on 10/18/15.
 */
public interface GameService {

    List<GameStreak> findStreaks(List<Game> games, boolean includePlayoffs);
}
