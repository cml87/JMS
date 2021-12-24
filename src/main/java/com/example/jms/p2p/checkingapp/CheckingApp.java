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


        // the reply queue will be a temporary queue!

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            Person person = new Person(123, "John", "Smith", LocalDate.parse("1990-01-23"), "+01 7896787788", "pepe@gmail.com");
        //    Queue replyQueue = jmsContext.createTemporaryQueue();


            JMSProducer producer = jmsContext.createProducer();
            //TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
            ObjectMessage message = jmsContext.createObjectMessage();
            message.setJMSReplyTo(replyQueue);

            message.setObject(person);


            message.setJMSReplyTo(replyQueue);
            producer.send(requestQueue, message);
            System.out.printf("MessageId sent by producer: [%s] \n", message.getJMSMessageID());

            Map<String, ObjectMessage> messages = new HashMap<>();
            messages.put(message.getJMSMessageID(), message);


            JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
            ObjectMessage message1 = (ObjectMessage) consumer.receive();
            System.out.println("id of message received: "+ message1.getJMSMessageID());


            Person person1 = (Person) message1.getObject();
            System.out.println("Person received: "+ person1.toString());


            MapMessage response = jmsContext.createMapMessage();
            response.setJMSCorrelationID(message1.getJMSMessageID());
            response.setBoolean("isReservationDone", true);

            jmsContext.createProducer().send(message1.getJMSReplyTo(), response);

            MapMessage reply = (MapMessage) jmsContext.createConsumer(replyQueue).receive();

            System.out.println("correlation id in reply: "+ reply.getJMSCorrelationID());
            System.out.println("reply message: isReservationDone:"+ reply.getBoolean("isReservationDone"));


        }


    }


}
