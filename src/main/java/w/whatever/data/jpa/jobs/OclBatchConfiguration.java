package w.whatever.data.jpa.jobs;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
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
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import w.whatever.data.jpa.OclApplication;
import w.whatever.data.jpa.domain.Game;
import w.whatever.data.jpa.jobs.ocl.load.OclGameProcessor;
import w.whatever.data.jpa.jobs.ocl.load.OclLoadJobRunner;
import w.whatever.data.jpa.jobs.ocl.load.OclGameWriter;
import w.whatever.data.jpa.service.data.GameRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by rich on 10/15/15.
 */
@Configuration
@EnableBatchProcessing()
public class OclBatchConfiguration extends DefaultBatchConfigurer {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private GameRepository gameRepository;

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


    @Override
    public JobExplorer getJobExplorer() {
        return new JobExplorer() {

            @Override
            public List<JobInstance> getJobInstances(String jobName, int start, int count) {
                return null;
            }

            @Override
            public JobExecution getJobExecution(Long executionId) {
                return null;
            }

            @Override
            public StepExecution getStepExecution(Long jobExecutionId, Long stepExecutionId) {
                return null;
            }

            @Override
            public JobInstance getJobInstance(Long instanceId) {
                return null;
            }

            @Override
            public List<JobExecution> getJobExecutions(JobInstance jobInstance) {
                return null;
            }

            @Override
            public Set<JobExecution> findRunningJobExecutions(String jobName) {
                return null;
            }

            @Override
            public List<String> getJobNames() {
                return null;
            }

            @Override
            public List<JobInstance> findJobInstancesByJobName(String jobName, int start, int count) {
                return null;
            }

            @Override
            public int getJobInstanceCount(String jobName) throws NoSuchJobException {
                return 0;
            }
        };
    }
}
