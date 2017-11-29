package w.whatever.data.jpa.jobs.ocl.scraper;

import org.springframework.batch.item.ItemWriter;
import w.whatever.data.jpa.jobs.ocl.scraper.data.OclScraperGame;

import java.util.List;

/**
 * Created by rich on 11/24/17.
 */
public class OclScraperWriter implements ItemWriter<OclScraperGame> {

    @Override
    public void write(List<? extends OclScraperGame> items) throws Exception {
         for (OclScraperGame game : items) {
             game.getGameFromEspn();
             game.writeHtmlToResource();
             game.writeXHTMLFromHTML();
             game.writeXMLFromXHTML();
         }
    }
}
