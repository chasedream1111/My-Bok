package com.gsy.bok.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置。
 * <p>
 * 放在 bok-user 而不是 bok-common：只有消息服务用 MQ，
 * 放公共模块会让网关也被迫带上 amqp（进而触发 Rabbit 健康检查、MQ 连接等无关初始化）。
 */
@Slf4j
@Configuration
public class RabbitmqConfiguration {

    /**
     * 自定义 RabbitTemplate：统一挂上 confirm / return 回调，业务侧只发消息不用再管。
     * <p>
     * 注意：@Bean 方法不能返回 null，否则会注册成 NullBean，
     * 导致 RabbitHealthContributor 报 "'beans' must not be empty"、整个应用启动失败。
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        // 消息没有路由到任何队列时退回给生产者（配合 spring.rabbitmq.template.mandatory=true）
        template.setMandatory(true);
        // broker 确认：ack=false 说明 broker 没收到消息
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息发送失败, correlationData={}, cause={}", correlationData, cause);
                // TODO重发消息
                template.convertAndSend("","");
            }
        });
        // 退回：消息到了 exchange，但没有队列接收
        template.setReturnsCallback(returned -> log.error(
                "消息被退回: exchange={}, routingKey={}, replyCode={}, replyText={}",
                returned.getExchange(), returned.getRoutingKey(),
                returned.getReplyCode(), returned.getReplyText()));
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
