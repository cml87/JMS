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
             JMSContext jmsContext = cf.createContext()){

            JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
            consumer.setMessageListener(new EligibilityCheckListener());

            Thread.sleep(10000);
        }
        System.out.println("Listener app finished");
    }
}
