package es.nachobrito.sdd.example.adapter.out.messaging;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import es.nachobrito.sdd.example.adapter.messaging.RabbitMqConstants;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import jakarta.inject.Singleton;
import java.io.IOException;

/**
 * Configures the RabbitMQ exchange, queue, and bindings for order events.
 */
@Singleton
public class RabbitMqChannelInitializer extends ChannelInitializer {

  @Override
  public void initialize(Channel channel, String name) throws IOException {
    channel.exchangeDeclare(
        RabbitMqConstants.EXCHANGE_ORDERS,
        BuiltinExchangeType.TOPIC,
        true);
    channel.queueDeclare(
        RabbitMqConstants.QUEUE_NOTIFICATIONS,
        true,
        false,
        false,
        null);
    channel.queueBind(
        RabbitMqConstants.QUEUE_NOTIFICATIONS,
        RabbitMqConstants.EXCHANGE_ORDERS,
        RabbitMqConstants.BINDING_KEY_ORDER_ALL);
  }
}
