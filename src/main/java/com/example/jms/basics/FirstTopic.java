package com.example.jms.basics;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class FirstTopic {

    public static void main (String[] args) throws NamingException, JMSException {

        InitialContext initialContext = initialContext = new InitialContext();
        Topic topic = (Topic) initialContext.lookup("topic/myTopic");
        ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession();

        MessageProducer producer = session.createProducer(topic);

        // This is how we subscribe a consumer to the topic
        // Consumers will only receive messages sent to the topic after they subscribed
        MessageConsumer consumer1 = session.createConsumer(topic);
        MessageConsumer consumer2 = session.createConsumer(topic);

        TextMessage message = session.createTextMessage("All the power is within me. I can do anything and everything.");
        producer.send(message);

        // tell the JMS provider that the consumer are ready to receive messages.
        connection.start();

        TextMessage message1 = (TextMessage) consumer1.receive();
        System.out.println("Consumer 1 message received: "+ message1.getText());

        TextMessage message2 = (TextMessage) consumer2.receive();
        System.out.println("Consumer 2 message received: "+ message2.getText());

        connection.close();
        initialContext.close();

    }
}
