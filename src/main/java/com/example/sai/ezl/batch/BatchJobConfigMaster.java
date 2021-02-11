package com.example.sai.ezl.batch;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.ChunkMessageChannelItemWriter;
import org.springframework.batch.integration.chunk.RemoteChunkHandlerFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;

@Configuration
@Profile("master")
public class BatchJobConfigMaster {


    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private final JobRepository jobRepository;

    private final DataSource dataSource;

    public BatchJobConfigMaster(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, JobRepository jobRepository, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobRepository = jobRepository;
        this.dataSource = dataSource;
    }

    public TaskExecutor ezlMessageTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(3);
        executor.setThreadNamePrefix("ezl_message_task_executor");
        executor.initialize();
        return executor;
    }

    @Bean
    public ExecutorChannel ezlRequests() {
        return new ExecutorChannel(ezlMessageTaskExecutor());
    }

    @Bean
    public IntegrationFlow ezlRequestFlow(RabbitTemplate rabbitTemplate) {
        return IntegrationFlows.from("ezlRequests")
                .handle(Amqp.outboundAdapter(rabbitTemplate)
                        .routingKey("ezlRequests"))
                .get();
    }

    @Bean
    public QueueChannel ezlReplies() {
        return new QueueChannel();
    }

    @Bean
    public IntegrationFlow ezlReplyFlow(ConnectionFactory rabbitConnectionFactory) {
        return IntegrationFlows
                .from(Amqp.inboundAdapter(rabbitConnectionFactory, "ezlReplies"))
                .channel(ezlReplies())
                .get();
    }

    @Bean
    public MessagingTemplate ezlMessagingTemplate() {
        final MessagingTemplate template = new MessagingTemplate();
        template.setDefaultChannel(ezlRequests());
        template.setReceiveTimeout(100_000_000L);
        return template;
    }

    @Bean
    public ChunkMessageChannelItemWriter<Zip> itemWriter() {
        final ChunkMessageChannelItemWriter<Zip> chunkMessageChannelItemWriter = new ChunkMessageChannelItemWriter<>();
        chunkMessageChannelItemWriter.setMessagingOperations(ezlMessagingTemplate());
        chunkMessageChannelItemWriter.setReplyChannel(ezlReplies());
        chunkMessageChannelItemWriter.setThrottleLimit(Integer.MAX_VALUE);
        chunkMessageChannelItemWriter.setMaxWaitTimeouts(6 * 3600);
        return chunkMessageChannelItemWriter;
    }

    @Bean
    public RemoteChunkHandlerFactoryBean<Zip> ezlChunkHandler(TaskletStep ezlProcessClaimsStep) {
        final RemoteChunkHandlerFactoryBean<Zip> remoteChunkHandlerFactoryBean = new RemoteChunkHandlerFactoryBean<>();
        remoteChunkHandlerFactoryBean.setChunkWriter(itemWriter());
        remoteChunkHandlerFactoryBean.setStep(ezlProcessClaimsStep);
        return remoteChunkHandlerFactoryBean;
    }

    @Bean
    public ThreadPoolTaskExecutor ezlTaskExecutor() {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(1);
        taskExecutor.setMaxPoolSize(1);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }


//////////////----------------------------------------------------//////////////////////////////

    @Bean(name = "ezlBatchJob")
    public Job remoteChunkingJob(TaskletStep deleteEzlClaims) {
        return this.jobBuilderFactory.get("extract zip load")
                .incrementer(new RunIdIncrementer())
                .start(deleteEzlClaims)// previous data in the tmp tables
//                .next(removeGdxFiles) //removes all files in path
//                .next(ezlProcessClaimsStep)
//                .next(aggregateEzlClaims) // aggregate all csv files  into one
//                .next(loadGdxClaims) //db2load
                .build();
    }

    @Bean(name = "ezlJobLauncher")
    public JobLauncher simpleJobLauncher() throws Exception {
        final SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(ezlTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public StepExecutionListener ezlProcessorStepListener() {
        return new StepExecutionListener() {

            @Override
            public void beforeStep(StepExecution stepExecution) {
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                stepExecution.getJobExecution().getExecutionContext().putInt("READ_COUNT", stepExecution.getReadCount());
                return ExitStatus.COMPLETED;
            }
        };
    }


    @Bean
    public TaskletStep ezlProcessClaimsStep(ItemReader<Zip> claimReader, ItemProcessor<Zip, Zip> processor) {
        return this.stepBuilderFactory.get("ezlProcessClaimsStep")
                .listener(ezlProcessorStepListener())
                .<Zip, Zip>chunk(100)
                .reader(claimReader)
                .processor(processor)
                .writer(itemWriter())
                .throttleLimit(Integer.MAX_VALUE)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Zip, Zip> itemProcessor(@Value("#{jobParameters[JOB_NAME]}") String jobName) {
        return item -> new Zip();
    }

    @Bean(destroyMethod = "")
    @StepScope
    public JdbcCursorItemReader<Zip> ezlClaimReader(@Value("#{jobParameters[JOB_NAME]}") String jobName,
                                                            @Value("#{jobParameters[START_DATE]}") String startDate,
                                                            @Value("#{jobParameters[END_DATE]}") String endDate) {
        final JdbcCursorItemReader<Zip> itemReader = new JdbcCursorItemReader<>();
        String query = "";
        itemReader.setSql(query);
        itemReader.setDataSource(dataSource);
        return itemReader;
    }
}