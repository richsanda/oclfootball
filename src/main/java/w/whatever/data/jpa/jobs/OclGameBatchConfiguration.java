package w.whatever.data.jpa.jobs;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;
import w.whatever.data.jpa.domain.Game;
import w.whatever.data.jpa.jobs.ocl.load.OclGameProcessor;
import w.whatever.data.jpa.jobs.ocl.load.OclLoadJobRunner;
import w.whatever.data.jpa.jobs.ocl.load.OclGameWriter;
import w.whatever.data.jpa.service.data.GameRepository;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;

/**
 * Created by rich on 10/15/15.
 */
@Configuration
@EnableBatchProcessing()
public class OclGameBatchConfiguration extends DefaultBatchConfigurer {

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
    public OclLoadJobRunner oclLoadJobRunner() throws Exception {
        OclLoadJobRunner runner = new OclLoadJobRunner();
        runner.setJob(oclLoadJob());
        runner.setJobLauncher(jobLauncher);
        return runner;
    }

    @Bean
    public Job oclLoadJob() throws Exception {
        return this.jobs.get("oclLoadJob").start(oclLoadStep()).build();
    }

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Bean
    protected Step oclLoadStep() throws Exception {
        return this.steps.get("oclLoadStep")
                .<Game, Game>chunk(10)
                .reader(oclReader())
                .processor(oclGameProcessor())
                .writer(oclGameWriter())
                .build();
    }

    @Bean
    @StepScope
    protected ItemReader<Game> oclReader() throws IOException {
        MultiResourceItemReader<Game> reader = new MultiResourceItemReader<Game>();
        //String path = "file:/Users/rich/Downloads/games/*.xml";
        String path = "games/*.xml";
        Resource[] resources = new PathMatchingResourcePatternResolver(resourceLoader).getResources(path);
        reader.setResources(resources);
        reader.setDelegate(oclGameReader());
        return reader;
    }

    @Bean
    @StepScope
    protected ResourceAwareItemReaderItemStream<Game> oclGameReader() {
        StaxEventItemReader<Game> reader = new StaxEventItemReader<Game>();
        reader.setFragmentRootElementName("game");
        //Resource resource = resourceLoader.getResource("file:/Users/rich/Downloads/games/2005.1.1.xml");
        //reader.setResource(resource);
        reader.setUnmarshaller(gameUnmarshaller());
        return reader;
    }

    @Bean
    @StepScope
    protected ItemProcessor<Game, Game> oclGameProcessor() {
        OclGameProcessor writer = new OclGameProcessor();
        return writer;
    }

    @Bean
    protected Jaxb2Marshaller gameUnmarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Game.class);
        return marshaller;
    }

    @Bean
    protected ItemWriter<Game> oclGameWriter() {
        OclGameWriter writer = new OclGameWriter();
        writer.setGameRepository(gameRepository);
        return writer;
    }
}
