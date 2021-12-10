package com.example.jms.basics;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

import javax.jms.*;
import javax.naming.*;

public class FirstQueue {

    public static void main(String[] args){

        /**
         * JMS 1.1 example
         */

        // access the jndi initial context
        InitialContext initialContext = null;
        Connection connection = null;

        try {

            // obtain a JNDI connection using a jndi.properties file
            // obtain a reference to the root of the JNDI tree of the naming server
            initialContext = new InitialContext();

            ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
            //System.out.println("Connection Factory class: "+connectionFactory.getClass());
            connection = connectionFactory.createConnection();


            ActiveMQJMSConnectionFactory x;

            Session session = connection.createSession();

            Queue queue = (Queue) initialContext.lookup("queue/myQueue");
            MessageProducer producer = session.createProducer(queue);

            TextMessage message = session.createTextMessage("I am the creator of my destiny");
            producer.send(message);
            System.out.println("Message sent: " + message);

            MessageConsumer consumer = session.createConsumer(queue);
            connection.start();

            // here we block. This is synchronous.
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
