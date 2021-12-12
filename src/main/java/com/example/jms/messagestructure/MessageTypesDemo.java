package com.example.jms.messagestructure;

import com.sun.beans.editors.ByteEditor;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *  JMS 2.0 example
 *  */
public class MessageTypesDemo {
   public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           JMSProducer producer = jmsContext.createProducer();
           BytesMessage bytesMessage = jmsContext.createBytesMessage();
           bytesMessage.writeUTF("John"); // first payload ?
           bytesMessage.writeLong(123l);  // second payload ?
           producer.send(queue, bytesMessage);

           BytesMessage messageReceived = (BytesMessage) jmsContext.createConsumer(queue).receive();
           System.out.println(messageReceived.readUTF());
           System.out.println(messageReceived.readLong());
       }
   }
}