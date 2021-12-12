package com.example.jms.messagestructure;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.soap.Text;

import static java.lang.Thread.*;

/**
 *  JMS 2.0 example
 *  */
public class MessageExpirationDemo {
   public static void main(String[] args) throws NamingException, InterruptedException {

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
           producer.setTimeToLive(2000);
           TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
           producer.send(queue, message);

           // we wait for three seconds, so we make the message expire
           Thread.sleep(3000);

           // wait for only one second, after which, if there is no message we'll get null back
           TextMessage messageReceived = (TextMessage) jmsContext.createConsumer(queue).receive(1000);
           System.out.println(messageReceived);

           System.out.println("Printing messages in the ExpiryQueue:");
           System.out.println(jmsContext.createConsumer(expiryQueue).receiveBody(String.class));
       }
   }
}