package w.whatever.data.jpa.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;
import w.whatever.data.jpa.domain.Game;
import w.whatever.data.jpa.jobs.ocl.scraper.OclScraperProcessor;
import w.whatever.data.jpa.jobs.ocl.scraper.OclScraperReader;
import w.whatever.data.jpa.jobs.ocl.scraper.OclScraperWriter;
import w.whatever.data.jpa.jobs.ocl.scraper.data.OclScraperGame;
import w.whatever.data.jpa.service.data.GameRepository;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;

/**
 * Created by rich on 10/15/15.
 */
//@Configuration
@EnableBatchProcessing()
public class OclScraperBatchConfiguration extends DefaultBatchConfigurer {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Bean
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job oclScraperJob() throws Exception {
        return this.jobs.get("oclScraperJob").start(oclScraperStep()).build();
    }

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Bean
    protected Step oclScraperStep() throws Exception {
        return this.steps.get("oclScraperStep")
                .<OclScraperGame, OclScraperGame>chunk(10)
                .reader(oclScraperReader())
                .processor(oclScraperProcessor())
                .writer(oclScraperWriter())
                .build();
    }

    @Bean
    @StepScope
    protected ItemReader<OclScraperGame> oclScraperReader() throws IOException {
        OclScraperReader reader = new OclScraperReader();
        return reader;
    }

    @Bean
    @StepScope
    protected ItemProcessor<OclScraperGame, OclScraperGame> oclScraperProcessor() {
        OclScraperProcessor processor = new OclScraperProcessor();
        return processor;
    }

    @Bean
    protected Jaxb2Marshaller gameUnmarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Game.class);
        return marshaller;
    }

    @Bean
    protected ItemWriter<OclScraperGame> oclScraperWriter() {
        OclScraperWriter writer = new OclScraperWriter();
        // writer.setGameRepository(gameRepository);
        return writer;
    }
}
