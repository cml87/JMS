package com.example.jms.messagestructure;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *  JMS 2.0 example
 *  */
public class MessagePriority {
   public static void main(String[] args) throws NamingException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       // JMSContext will have the ConnectionFactory and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

            String[] messages = {"Message One", "Message Two", "Message Three"};

           JMSProducer producer = jmsContext.createProducer();

           producer.setPriority(3);
           producer.send(queue, messages[0]);

           producer.setPriority(1);
           producer.send(queue, messages[1]);

           producer.setPriority(9);
           producer.send(queue, messages[2]);

           JMSConsumer consumer = jmsContext.createConsumer(queue);

           for (int i = 0; i < 3; i++ ){
               Message receivedMessage = consumer.receive();
               int messagePriority = receivedMessage.getJMSPriority();
               String messageBody = receivedMessage.getBody(String.class);

               System.out.printf("Message priority: %d, Message body: %s\n", messagePriority, messageBody);
               //System.out.println(consumer.receiveBody(String.class));
           }
       } catch (JMSException e) {
           e.printStackTrace();
       }
   }
}