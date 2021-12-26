package com.example.jms.p2p.checkingapp;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ReservationSystemApp {

    public static void main (String[] args) throws NamingException, InterruptedException {

        System.out.println("Listener application started ...");

        InitialContext initialContext = new InitialContext();
        Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        ReservationSystemListener reservationSystemListener = applicationContext.getBean("reservationSystemListener",
                                                            ReservationSystemListener.class);

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()){

            JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
            consumer.setMessageListener(reservationSystemListener);

            Thread.sleep(7000);

        }

        System.out.println("Listener application ended");
    }



}
