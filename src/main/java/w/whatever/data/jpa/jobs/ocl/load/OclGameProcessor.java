package w.whatever.data.jpa.jobs.ocl.load;

import org.springframework.batch.item.ItemProcessor;
import w.whatever.data.jpa.domain.Game;

/**
 * Created by rich on 10/15/15.
 */
public class OclGameProcessor implements ItemProcessor<Game, Game> {

    @Override
    public Game process(Game game) throws Exception {

        game.init();

        return game;
    }
}
