package com.example.jms.pubsub.welness;

import com.example.jms.pubsub.hr.Employee;
import com.sun.corba.se.impl.ior.StubIORImpl;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class WellnessApp {

    public static void main(String[] args) throws NamingException, JMSException {

        System.out.println("In WellnessApp ...");

        InitialContext context = new InitialContext();
        Topic topic = (Topic) context.lookup("topic/empTopic");

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            //JMSConsumer consumer = jmsContext.createConsumer(topic);
            JMSConsumer consumer1 = jmsContext.createSharedConsumer(topic, "sharedConsumerPool1");
            JMSConsumer consumer2 = jmsContext.createSharedConsumer(topic, "sharedConsumerPool1");

            Employee employee;
            Message message;
            System.out.println("a");
            for (int i = 0; i < 9; i++) {

                System.out.println("b-"+i);
                message = consumer1.receive();
                employee = message.getBody(Employee.class);
                System.out.println("received employed of id: "+employee.getId());

                message = consumer2.receive();
                employee = message.getBody(Employee.class);
                System.out.println("received employed of id: "+employee.getId());
            }

        }
    }
}