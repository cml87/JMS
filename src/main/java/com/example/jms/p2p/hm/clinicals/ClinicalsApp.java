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

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()){

            Patient patient = new Patient(123, "Bob");
            patient.setInsuranceProvider("Blue cross Blue Shield");
            patient.setCopay(30d);
            patient.setAmountToBePayed(500d);

            JMSProducer producer = jmsContext.createProducer();
            ObjectMessage objectMessage = jmsContext.createObjectMessage();
            objectMessage.setObject(patient);

            producer.send(requestQueue, patient);


        }


    }






}
