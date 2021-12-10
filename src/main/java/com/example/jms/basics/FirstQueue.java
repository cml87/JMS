package com.example.jms.basics;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory;

import javax.jms.*;
import javax.naming.*;

 /**
  *  JMS 1.1 example. All the boilerplate code is removed with JMS 2.0
  *  */
public class FirstQueue {
    public static void main(String[] args){

        InitialContext initialContext = null;
        Connection connection = null;
        try {

            // obtain a reference to the root of the JNDI tree of the naming server
            // of the JMS server
            initialContext = new InitialContext();

            ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            Session session = connection.createSession();

            Queue queue = (Queue) initialContext.lookup("queue/myQueue");
            MessageProducer producer = session.createProducer(queue);

            TextMessage message = session.createTextMessage("I am the creator of my destiny");
            producer.send(message);

            System.out.println("Message sent: " + message);

            /** Now we'll consume the messages  */

            MessageConsumer consumer = session.createConsumer(queue);
            connection.start(); // start the flow of messages in the queue to the consumers.
                                //Tell the JMS provider we are ready to consume the messages

            // here we block. This is synchronous.
            // throw exception if message is not received after 5 seconds
            TextMessage messageReceived = (TextMessage) consumer.receive(5000);

            System.out.println("Message received: " + messageReceived.getText());

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e){
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