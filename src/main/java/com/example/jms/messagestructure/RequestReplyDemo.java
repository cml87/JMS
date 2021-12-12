package com.example.jms.messagestructure;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

/**
 *  JMS 2.0 example
 *  */
public class RequestReplyDemo {
   public static void main(String[] args) throws NamingException, JMSException {

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
           //producer.setJMSReplyTo(replyQueue);
           TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
           //TemporaryQueue replyQueue = jmsContext.createTemporaryQueue();

           message.setJMSReplyTo(replyQueue);
           producer.send(requestQueue, message);
           System.out.printf("Request message sent by producer: [%s] %s\n", message.getJMSMessageID(), message.getText());

           Map<String, TextMessage> requestMessages = new HashMap<>();
           requestMessages.put(message.getJMSMessageID(), message);

           /* consumer application*/
           JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
           TextMessage messageReceived = (TextMessage) consumer.receive();
           System.out.printf("Request message received by consumer: [%s] %s\n", messageReceived.getJMSMessageID(), messageReceived.getText());

           JMSProducer replyProducer = jmsContext.createProducer();
           TextMessage replyMessage = jmsContext.createTextMessage("You are awesome!!");
           replyMessage.setJMSCorrelationID(messageReceived.getJMSMessageID());
           replyProducer.send(messageReceived.getJMSReplyTo(), replyMessage);
           System.out.printf("Reply message sent by consumer: [correlation%s] %s\n", replyMessage.getJMSCorrelationID(), replyMessage.getText());

           /***************************/

           /* producer application */
           JMSConsumer replyConsumer = jmsContext.createConsumer(messageReceived.getJMSReplyTo());
           TextMessage replyReceived = (TextMessage) replyConsumer.receive();
           System.out.printf("Reply message received by producer: [correlation%s] %s\n", replyReceived.getJMSCorrelationID(), replyReceived.getText());

          // System.out.printf("The reply corresponds to message: %s", requestMessages.get(replyReceived.getJMSCorrelationID()).getText());

       }
   }
}