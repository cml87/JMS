package com.example.jms.p2p.checkingapp;

import com.example.jms.p2p.hm.model.Patient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

            List<Person> personList = new ArrayList<>();
            for (int i = 0 ; i < 100; i++){
                personList.add(new Person(i, "John", "Smith",
                        LocalDate.parse("2003-11-24"), "+01 7896787788", "pepe@gmail.com"));
            }


            JMSProducer producer = jmsContext.createProducer();
            producer.setJMSReplyTo(replyQueue);

            ObjectMessage message = jmsContext.createObjectMessage();

            //message.setJMSReplyTo(replyQueue);
            for (int i = 0 ; i < 100; i++) {
                message.setObject(personList.get(i));
                producer.send(requestQueue, message);
                System.out.printf("MessageId sent by producer: [%s]. PersonId[%s]:\n", message.getJMSMessageID(),
                        ((Person)message.getObject()).getId());
            }

            System.out.println("\n\nWaiting 20 seconds for responses to arrive to replyQueue ...");
            Thread.sleep(20000);

            //Map<String, ObjectMessage> messages = new HashMap<>();
            //messages.put(message.getJMSMessageID(), message);

            JMSConsumer consumer = jmsContext.createConsumer(replyQueue);
            for (int i = 0 ; i < 100; i++) {
                MapMessage reply = (MapMessage) consumer.receive();
                System.out.printf("CorrelationId received in reply: [%s]\n", reply.getJMSCorrelationID());
            }

            //System.out.println("reply message: isReservationDone: "+ reply.getBoolean("isReservationDone"));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


}
