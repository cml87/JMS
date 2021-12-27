package com.example.jms.pubsub.security;

import com.example.jms.pubsub.hr.Employee;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SecurityApp {

    public static void main(String[] args) throws NamingException, JMSException, InterruptedException {

        System.out.println("In SecurityApp ...");

        InitialContext context = new InitialContext();
        Topic topic = (Topic) context.lookup("topic/empTopic");

        try(ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
            JMSContext jmsContext = cf.createContext()){

            // "register" a durable subscriber in the JMS context with id "SecurityApp"
            jmsContext.setClientID("SecurityApp");

            //JMSConsumer consumer = jmsContext.createConsumer(topic);
            // get the durable subscriber with id "SecurityApp"
            JMSConsumer consumer = jmsContext.createDurableConsumer(topic, "SecurityApp");

            // close the subscriber?
            consumer.close();

            // simulate that the app is down for a while
            Thread.sleep(10000);

            // open the subscriber
            consumer = jmsContext.createDurableConsumer(topic, "SecurityApp");

            Message message = consumer.receive();
            Employee employee = message.getBody(Employee.class);

            System.out.println(employee.getFirstName());

            consumer.close();
            jmsContext.unsubscribe("SecurityApp");

        }
    }
}