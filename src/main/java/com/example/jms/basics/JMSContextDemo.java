package com.example.jms.basics;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionForContext;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *  JMS 2.0 example
 *  */
public class JMSContextDemo {
   public static void main(String[] args) throws NamingException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       // JMSContext will have the ConnectionFactory and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           jmsContext.createProducer().send(queue, "Arise awake and stop not till the goal is reached");

           // receive directly the body of the message as a String
           // jmsContext.createConsumer(queue) ... this returns a JMSConsumer
           String messageReceived = jmsContext.createConsumer(queue).receiveBody(String.class);

           System.out.println("Message is: "+ messageReceived);

       }
   }
}