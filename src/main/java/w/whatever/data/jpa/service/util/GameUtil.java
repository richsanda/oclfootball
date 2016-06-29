package w.whatever.data.jpa.service.util;

import org.apache.commons.lang3.builder.CompareToBuilder;
import w.whatever.data.jpa.domain.Game;

import java.util.Comparator;

/**
 * Created by rich on 10/17/15.
 */
public class GameUtil {

    public static final Comparator<Game> GAME_COMPARATOR_BY_POINTS = new Comparator<Game>() {
        @Override
        public int compare(Game game1, Game game2) {
            return new CompareToBuilder()
                    .append(game1.getPoints(), game2.getPoints())
                    .toComparison();
        }
    };

    public static final Comparator<Game> GAME_COMPARATOR_BY_WEEK = new Comparator<Game>() {
        @Override
        public int compare(Game game1, Game game2) {
            return new CompareToBuilder()
                    .append(game1.getSeason(), game2.getSeason())
                    .append(game1.getScoringPeriod(), game2.getScoringPeriod())
                    .toComparison();
        }
    };
}
