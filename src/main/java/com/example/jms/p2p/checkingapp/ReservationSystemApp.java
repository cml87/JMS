package com.example.jms.p2p.checkingapp;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/*
* I will start three of these consumers, all listening to the same queue, requestQueue, and replying also to the same queue, replyQueue.
* I will start three of them by running in three separate shells the program:
* java -cp  /home/camilo/my_java_projects/JMS/target/jmsfundamentals-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.example.jms.p2p.checkingapp.ReservationSystemApp
*
* There will be only one instance of the producer application adding messages to the requestQueue:
* java -cp  /home/camilo/my_java_projects/JMS/target/jmsfundamentals-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.example.jms.p2p.checkingapp.CheckingApp
*
* After I start the three consumers, or listeners, I start the producer application. It will quickly add 100 messages to the requestQueue, all of which will
* be consumed with load balance by the three consumers.
*
* */

public class ReservationSystemApp {

    public static void main (String[] args) throws NamingException, InterruptedException {

        System.out.println("Listener application started ...");

        InitialContext initialContext = new InitialContext();
        Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

        //ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

        ReservationSystemListener reservationSystemListener = applicationContext.getBean("reservationSystemListener",
                                                            ReservationSystemListener.class);

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()){

            JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
            consumer.setMessageListener(reservationSystemListener);

            Thread.sleep(15000);

        }

        System.out.println("Listener application ended");
    }



}
