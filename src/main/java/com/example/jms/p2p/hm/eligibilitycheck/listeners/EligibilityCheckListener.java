package com.example.jms.p2p.hm.eligibilitycheck.listeners;

import com.example.jms.p2p.hm.model.Patient;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class EligibilityCheckListener implements MessageListener {


    @Override
    public void onMessage(Message message) {

        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Patient patient = (Patient) objectMessage.getObject();
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

}
