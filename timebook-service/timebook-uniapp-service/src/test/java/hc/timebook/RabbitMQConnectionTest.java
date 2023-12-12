package hc.timebook;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConnectionTest {
    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.56.160");
        factory.setPort(5672);
        factory.setUsername("rabbitMq");
        factory.setPassword("rabbitMq");

        try (Connection connection = factory.newConnection()) {
            System.out.println("Connected to RabbitMQ successfully!");
            // 这里可以进行进一步的操作，如发送和接收消息
        } catch (Exception e) {
            System.out.println("Failed to connect to RabbitMQ: " + e.getMessage());
        }
    }
}
