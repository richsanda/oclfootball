package w.whatever.data.jpa.jobs.ocl.scraper;

import org.springframework.batch.item.ItemProcessor;
import w.whatever.data.jpa.jobs.ocl.scraper.data.OclScraperGame;

/**
 * Created by rich on 11/24/17.
 */
public class OclScraperProcessor implements ItemProcessor<OclScraperGame, OclScraperGame> {


    @Override
    public OclScraperGame process(OclScraperGame item) throws Exception {

        return item;
    }
}
