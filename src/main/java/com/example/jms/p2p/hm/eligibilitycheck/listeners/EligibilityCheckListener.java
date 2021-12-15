package com.example.jms.p2p.hm.eligibilitycheck.listeners;

import com.example.jms.p2p.hm.model.Patient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class EligibilityCheckListener implements MessageListener {

    @Override
    public void onMessage(Message message) {

        ObjectMessage objectMessage = (ObjectMessage) message;

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            InitialContext initialContext = new InitialContext();
            Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

            // We'll reply to this queue
            MapMessage replyMessage = jmsContext.createMapMessage();

            // Business logic. Processing of the incoming message
            Patient patient = (Patient) objectMessage.getObject();
            System.out.println("received Patient: " + patient.toString());
            String insuranceProvider = patient.getInsuranceProvider();
            if (insuranceProvider.equals("Blue Cross Blue Shield") || insuranceProvider.equals("United Health")){
                System.out.println("Patient copay is: "+ patient.getCopay());
                System.out.println("Patient amount to be paid: "+ patient.getAmountToBePayed());
                if (patient.getCopay()<40 && patient.getAmountToBePayed()<1000){
                    System.out.println("a");
                    replyMessage.setBoolean("eligible", true);
                } else {
                    replyMessage.setBoolean("eligible", false);
                }
            } else {
                replyMessage.setBoolean("eligible", false);
            }

            // Replay creation
            JMSProducer producer = jmsContext.createProducer();
            producer.send(replyQueue, replyMessage);

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }
}
