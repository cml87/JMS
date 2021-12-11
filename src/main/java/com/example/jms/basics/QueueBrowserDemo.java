package com.example.jms.basics;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Enumeration;

/**
 * JMS 1.1 example. All the boilerplate code is removed with JMS 2.0
 */
public class QueueBrowserDemo {
    public static void main(String[] args) {

        InitialContext initialContext = null;
        Connection connection = null;
        try {

            // obtain a reference to the root of the JNDI tree of the naming server
            // of the JMS server
            initialContext = new InitialContext();

            Queue queue = (Queue) initialContext.lookup("queue/myQueue");
            ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

            connection = connectionFactory.createConnection();
            Session session = connection.createSession();

            MessageProducer producer = session.createProducer(queue);
            MessageConsumer consumer = session.createConsumer(queue);

            TextMessage message1 = session.createTextMessage("Message 1");
            TextMessage message2 = session.createTextMessage("Message 2");

            producer.send(message1);
            producer.send(message2);

            QueueBrowser browser = session.createBrowser(queue);
            Enumeration messageEnum = browser.getEnumeration();

            // show the messages currently in the queue
            System.out.println("Messages in the queue are:");
            while (messageEnum.hasMoreElements()) {
                TextMessage message = (TextMessage) messageEnum.nextElement();
                System.out.println("Browsing: "+ message.getText());
            }


            /** Now we'll consume the messages  */

            // start the flow of messages in the queue towards the consumers.
            // tell the JMS provider we are ready to consume the messages
            connection.start();

            // here we block. This is synchronous.
            // throw exception if message is not received after 5 seconds
            System.out.println("Consuming messages in the queue: ");
            TextMessage messageReceived = (TextMessage) consumer.receive(5000);
            System.out.println("Message received: " + messageReceived.getText());

            messageReceived = (TextMessage) consumer.receive(5000);
            System.out.println("Message received: " + messageReceived.getText());

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            if (initialContext != null) {
                try {
                    initialContext.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}