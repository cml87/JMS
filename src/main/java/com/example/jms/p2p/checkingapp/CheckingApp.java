package com.example.jms.p2p.checkingapp;

import com.example.jms.p2p.hm.model.Patient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CheckingApp {
//    public Person(int id, String firstName, String lastName, LocalDate birthDay, String phone, String email) {

    public static void main(String[] args) throws NamingException, JMSException {

        InitialContext initialContext = new InitialContext();
        Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

        // the reply queue will be a temporary queue!

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            Queue replyQueue = jmsContext.createTemporaryQueue();

            Person person = new Person(123, "John", "Smith", LocalDate.parse("1990-01-23"), "+01 7896787788", "pepe@gmail.com");

            ObjectMessage message = jmsContext.createObjectMessage();
            message.setObject(person);
            message.setJMSReplyTo(replyQueue);

            Map<String, ObjectMessage> messages = new HashMap<>();
            messages.put(message.getJMSMessageID(), message);

            JMSProducer producer = jmsContext.createProducer();
            System.out.println("message is: "+ message.toString());
            System.out.println("message id is: "+message.getJMSMessageID());
            producer.send(requestQueue, message);


//            JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
//            ObjectMessage message1 = (ObjectMessage) consumer.receive();
//            Person person1 = (Person) message1.getObject();
//
//            MapMessage response = jmsContext.createMapMessage();
//            response.setJMSCorrelationID(message1.getJMSMessageID());
//            response.setBoolean("isReservationDone", true);
//            jmsContext.createProducer().send(message1.getJMSReplyTo(), response);
//
//
//            System.out.println("received person is: "+ person1.toString());







        }




    }







}
