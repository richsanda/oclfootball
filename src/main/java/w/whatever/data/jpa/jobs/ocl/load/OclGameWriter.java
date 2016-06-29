package w.whatever.data.jpa.jobs.ocl.load;

import org.springframework.batch.item.ItemWriter;
import w.whatever.data.jpa.domain.Game;
import w.whatever.data.jpa.service.data.GameRepository;

import java.util.List;

/**
 * Created by rich on 10/15/15.
 */
public class OclGameWriter implements ItemWriter<Game> {

    private GameRepository gameRepository;

    public void setGameRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public void write(List<? extends Game> games) throws Exception {
        for (Game game : games) {
            System.out.println(game.toString());
            gameRepository.save(game);
        }
    }
}
