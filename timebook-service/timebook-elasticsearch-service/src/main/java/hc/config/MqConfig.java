package hc.config;

import hc.common.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(MqConstants.TIMEBOOK_EXCHANGE,true,false);
    }
    @Bean
    public Queue insertQueue(){
        return new Queue(MqConstants.TIMEBOOK_INSERT_QUEUE,true);
    }
    @Bean
    public Queue updateQueue(){
        return new Queue(MqConstants.TIMEBOOK_UPDATE_QUEUE,true);
    }
    @Bean
    public Queue deleteQueue(){
        return new Queue(MqConstants.TIMEBOOK_DELETE_QUEUE,true);
    }

    @Bean
    public Binding insertQueueBinding(){
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(MqConstants.TIMEBOOK_INSERT_KEY);
    }
    @Bean
    public Binding updateQueueBinding(){
        return BindingBuilder.bind(updateQueue()).to(topicExchange()).with(MqConstants.TIMEBOOK_UPDATE_KEY);
    }
    @Bean
    public Binding deleteQueueBinding(){
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(MqConstants.TIMEBOOK_DELETE_KEY);
    }
}
