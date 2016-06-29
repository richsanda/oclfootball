package w.whatever.data.jpa.service.impl;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import w.whatever.data.jpa.domain.Game;
import w.whatever.data.jpa.domain.GameStreak;
import w.whatever.data.jpa.service.GameService;
import w.whatever.data.jpa.service.util.GameUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rich on 10/17/15.
 */
@Component
public class GameServiceImpl implements GameService {

    public List<GameStreak> findStreaks(List<Game> games, boolean includePlayoffs) {

        if (null == games || games.isEmpty()) return null;

        List<GameStreak> streaks = Lists.newArrayList();

        List<Game> sortedGames = Lists.newArrayList(games);
        Collections.sort(sortedGames, GameUtil.GAME_COMPARATOR_BY_WEEK);
        Iterator<Game> gameIterator = sortedGames.iterator();

        Game lastGame = null;
        GameStreak streak = null;
        while (gameIterator.hasNext()) {

            Game game = gameIterator.next();

            if (!includePlayoffs && game.isPlayoffGame()) continue;

            if (null == streak) {
                streak = new GameStreak();
            } else if (!game.isNextTo(lastGame, includePlayoffs)) {
                streaks.add(streak);
                streak = new GameStreak();
            }

            streak.addGame(game);
            lastGame = game;
        }

        return streaks;
    }
}
