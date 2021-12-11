package com.example.jms.messagestructure;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *  JMS 2.0 example
 *  */
public class RequestReplyDemo {
   public static void main(String[] args) throws NamingException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
       Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           /* producer application */
           JMSProducer producer = jmsContext.createProducer();
           producer.send(requestQueue, "Arise awake and stop not till the goal is reached");


           /* consumer application*/
           JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
           String messageReceived = consumer.receiveBody(String.class);
           System.out.println(messageReceived);

           JMSProducer replyProducer = jmsContext.createProducer();
           replyProducer.send(replyQueue, "You are awesome!!");
           /***************************/


           /* producer application */
           JMSConsumer replyConsumer = jmsContext.createConsumer(replyQueue);
           System.out.println(replyConsumer.receiveBody(String.class));

       }
   }
}