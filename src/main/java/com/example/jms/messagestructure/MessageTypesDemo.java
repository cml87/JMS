package com.example.jms.messagestructure;

import com.sun.beans.editors.ByteEditor;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;

/**
 *  JMS 2.0 example
 *  */
public class MessageTypesDemo {
   public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

       System.out.println("-1");

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");
       System.out.println("0");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           System.out.println("dfd");

           JMSProducer producer = jmsContext.createProducer();

           System.out.println("a");

           BytesMessage bytesMessage = jmsContext.createBytesMessage();
           bytesMessage.writeUTF("John"); // first payload ?
           bytesMessage.writeLong(123l);  // second payload ?
           //producer.send(queue, bytesMessage);
           System.out.println("b");

          // BytesMessage messageReceived = (BytesMessage) jmsContext.createConsumer(queue).receive();
          // System.out.println(messageReceived.readUTF());
          // System.out.println(messageReceived.readLong());

           StreamMessage streamMessage = jmsContext.createStreamMessage();
           streamMessage.writeBoolean(true); // first payload ?
           streamMessage.writeFloat(2.5f);  // second payload ?
//           producer.send(queue, streamMessage);
//
//           StreamMessage messageReceived = (StreamMessage) jmsContext.createConsumer(queue).receive();
//           System.out.println(messageReceived.readBoolean());
//           System.out.println(messageReceived.readFloat());
           System.out.println("c");


           MapMessage mapMessage = jmsContext.createMapMessage();
           mapMessage.setBoolean("isCreditAvailable", true);

           producer.send(queue,mapMessage);
           MapMessage messageReceived = (MapMessage) jmsContext.createConsumer(queue).receive();
           System.out.println(messageReceived.getBoolean("isCreditAvailable"));

       }
   }
}