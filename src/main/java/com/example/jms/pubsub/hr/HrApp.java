package com.example.jms.pubsub.hr;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class HrApp {

    public static void main(String[] args) throws NamingException {

        InitialContext context = new InitialContext();
        Topic topic = (Topic) context.lookup("topic/empTopic");

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()){

            Employee employee = new Employee(1,"Paul", "White","pepe@gmail.com","developer","0122234344");

            JMSProducer producer = jmsContext.createProducer();
            // we send the message directly
            producer.send(topic, employee);



        }

    }

}
