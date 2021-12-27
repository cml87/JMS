package com.example.jms.pubsub.welness;

import com.example.jms.pubsub.hr.Employee;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class WellnessApp {

    public static void main(String[] args) throws NamingException, JMSException {

        System.out.println("In WellnessApp ...");

        InitialContext context = new InitialContext();
        Topic topic = (Topic) context.lookup("topic/empTopic");

        try(ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
            JMSContext jmsContext = cf.createContext()){

            JMSConsumer consumer = jmsContext.createConsumer(topic);
            Message message = consumer.receive();
            Employee employee = message.getBody(Employee.class);

            System.out.println(employee.getFirstName());

        }
    }
}