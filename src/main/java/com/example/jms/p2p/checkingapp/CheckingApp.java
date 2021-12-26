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
        Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            Person person = new Person(123, "John", "Smith",
                    LocalDate.parse("2003-11-24"), "+01 7896787788", "pepe@gmail.com");

            JMSProducer producer = jmsContext.createProducer();
            ObjectMessage message = jmsContext.createObjectMessage();

            message.setJMSReplyTo(replyQueue);
            message.setObject(person);

            producer.send(requestQueue, message);
            System.out.printf("MessageId sent by producer: [%s]\n", message.getJMSMessageID());

            Map<String, ObjectMessage> messages = new HashMap<>();
            messages.put(message.getJMSMessageID(), message);

            MapMessage reply = (MapMessage) jmsContext.createConsumer(replyQueue).receive();

            System.out.printf("CorrelationId received in reply: [%s]\n", reply.getJMSCorrelationID());

            System.out.println("reply message: isReservationDone: "+ reply.getBoolean("isReservationDone"));


        }


    }


}
