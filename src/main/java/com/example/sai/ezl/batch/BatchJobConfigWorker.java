package com.example.sai.ezl.batch;


import com.example.sai.ezl.component.EzlChunkProcessor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.integration.chunk.ChunkProcessorChunkHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@Profile("worker")
@Configuration
public class BatchJobConfigWorker extends DefaultBatchConfigurer {

    private final EzlChunkProcessor ezlChunkProcessor;

    public BatchJobConfigWorker(EzlChunkProcessor ezlChunkProcessor) {
        this.ezlChunkProcessor = ezlChunkProcessor;
    }

    @Bean
    public DirectChannel ezlRequests() {
        return new DirectChannel();
    }

    @Bean
    public TopicExchange ezlExchange() {
        return new TopicExchange("ezl-remote-chunking-exchange");
    }

    @Bean
    Binding repliesBinding(TopicExchange ezlExchange, Queue ezlRepliesQueue) {
        return BindingBuilder.bind(ezlRepliesQueue).to(ezlExchange).with("ezlReplies");
    }

    @Bean
    Binding requestBinding(TopicExchange ezlExchange, Queue ezlRequestQueue) {
        return BindingBuilder.bind(ezlRequestQueue).to(ezlExchange).with("ezlRequests");
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory rabbitConnectionFactory) {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(rabbitConnectionFactory);
        container.setConcurrentConsumers(1);
        container.setPrefetchCount(1);
        container.setIdleEventInterval(1000 * 60 * 10);
        container.setListenerId("ezlRequestQueue");
        container.setQueueNames("ezlRequests");
        return container;
    }

    @Bean
    public IntegrationFlow messagesIn(SimpleMessageListenerContainer container) {
        return IntegrationFlows
                .from(Amqp.inboundAdapter(container))
                .channel(ezlRequests())
                .get();
    }

    @Bean
    public IntegrationFlow outgoingReplies(RabbitTemplate rabbitTemplate) {
        return IntegrationFlows.from("ezlReplies")
                .handle(Amqp.outboundAdapter(rabbitTemplate)
                        .routingKey("ezlReplies"))
                .get();
    }

    @Bean
    @ServiceActivator(inputChannel = "ezlRequests" , outputChannel = "ezlReplies", sendTimeout = "100000000")
    public ChunkProcessorChunkHandler<Zip> chunkProcessorChunkHandler() {
        final ChunkProcessorChunkHandler<Zip> chunkProcessorChunkHandler = new ChunkProcessorChunkHandler<>();
        chunkProcessorChunkHandler.setChunkProcessor(ezlChunkProcessor);
        return chunkProcessorChunkHandler;
    }
}
