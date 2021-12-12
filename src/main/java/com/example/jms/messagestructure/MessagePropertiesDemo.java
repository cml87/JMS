package com.example.jms.messagestructure;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *  JMS 2.0 example
 *  */
public class MessagePropertiesDemo {
   public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       Queue expiryQueue = (Queue) initialContext.lookup("queue/expiryQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           JMSProducer producer = jmsContext.createProducer();
           TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
           message.setBooleanProperty("loggedIn", true);
           message.setStringProperty("userToken", "abc123");

           producer.send(queue, message);

           // we'll get the message after three seconds
           TextMessage messageReceived = (TextMessage) jmsContext.createConsumer(queue).receive();
           System.out.println(messageReceived);
           System.out.println(messageReceived.getBooleanProperty("loggedIn"));
           System.out.println(messageReceived.getStringProperty("userToken"));

       }
   }
}