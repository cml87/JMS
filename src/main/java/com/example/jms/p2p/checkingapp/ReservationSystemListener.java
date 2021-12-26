package com.example.jms.p2p.checkingapp;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.jms.*;
import java.time.LocalDate;
import java.time.Period;

public class ReservationSystemListener implements MessageListener {

    private static int minimumAgeYears;

    public static void setMinimumAgeYears(int minimumAgeYears) {
        ReservationSystemListener.minimumAgeYears = minimumAgeYears;
    }

    @Override
    public void onMessage(Message message) {

        System.out.println("listener method in ...");

        ObjectMessage objectMessage = (ObjectMessage)message;

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()){

            Queue replyQueue = (Queue) objectMessage.getJMSReplyTo();

            MapMessage mapMessage = jmsContext.createMapMessage();
            mapMessage.setJMSCorrelationID(objectMessage.getJMSMessageID());

            // request processing
            Person person = (Person) objectMessage.getObject();
            System.out.println("received person's birthday: "+ person.getBirthDay().toString());
            LocalDate now = LocalDate.now();
            System.out.println("Today is: "+ now.toString());
            int personAge = Period.between(person.getBirthDay(), now).getYears();
            System.out.println("Person's age is: "+ personAge);
            if (personAge>= minimumAgeYears){
                System.out.println("Person's age is above the minimum "+minimumAgeYears);
                mapMessage.setBoolean("isReservationDone", true);
            } else {
                System.out.println("Person's age is below the minimum "+minimumAgeYears);
                mapMessage.setBoolean("isReservationDone", false);
            }

            JMSProducer producer = jmsContext.createProducer();
            producer.send(replyQueue, mapMessage);

            System.out.println("listener method out");


        } catch (JMSException e) {
            e.printStackTrace();
        }


    }


}
