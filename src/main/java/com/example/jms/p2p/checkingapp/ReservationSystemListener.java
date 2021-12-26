package com.example.jms.p2p.checkingapp;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;

public class ReservationSystemListener implements MessageListener {


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
            mapMessage.setBoolean("isReservationDone", true);

            JMSProducer producer = jmsContext.createProducer();
            producer.send(replyQueue, mapMessage);

            System.out.println("listener method out");


        } catch (JMSException e) {
            e.printStackTrace();
        }


    }


}
