package com.example.jms.p2p.hm.eligibilitycheck;

import com.example.jms.p2p.hm.eligibilitycheck.listeners.EligibilityCheckListener;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class EligibilityCheckerApp {

    public static void main(String[] args) throws NamingException, JMSException, InterruptedException {

        System.out.println("Listener app started ...");

        InitialContext initialContext = new InitialContext();
        Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

//            JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
//            consumer.setMessageListener(new EligibilityCheckListener());

            // we set two consumers attached to the same queue
            JMSConsumer consumer1 = jmsContext.createConsumer(requestQueue);
            JMSConsumer consumer2 = jmsContext.createConsumer(requestQueue);

            // We alternate the two consumers in consuming the messages in the same queue
            for (int i = 1; i <= 10; i+=2) {
                System.out.println("Consumer1: " + consumer1.receive());
                System.out.println("Consumer2: " + consumer2.receive());
            }

            //        Thread.sleep(10000);
        }
        System.out.println("Listener app finished");
    }
}
