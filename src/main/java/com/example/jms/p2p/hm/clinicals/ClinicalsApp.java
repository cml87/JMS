package com.example.jms.p2p.hm.clinicals;

import com.example.jms.p2p.hm.model.Patient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ClinicalsApp {

    public static void main(String[] args) throws NamingException, JMSException {

        InitialContext initialContext = new InitialContext();
        Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
        Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            Patient patient = new Patient(123, "Bob");
            patient.setInsuranceProvider("Blue Cross Blue Shield");
            patient.setCopay(100d);
            patient.setAmountToBePayed(500d);

            JMSProducer producer = jmsContext.createProducer();
            ObjectMessage objectMessage = jmsContext.createObjectMessage();
            objectMessage.setObject(patient);

            for (int i = 1; i <= 10; i++)
                producer.send(requestQueue, patient);


//            JMSConsumer consumer = jmsContext.createConsumer(replyQueue);
//            MapMessage replyMessage = (MapMessage) consumer.receive(30000);
//
//            System.out.println("patient eligibility is: "+ replyMessage.getBoolean("eligible"));

        }
    }
}
