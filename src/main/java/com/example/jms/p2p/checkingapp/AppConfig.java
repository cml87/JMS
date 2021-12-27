package com.example.jms.p2p.checkingapp;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean(name = "reservationSystemListener")
    public ReservationSystemListener getReservationSystemListener(){
        ReservationSystemListener reservationSystemListener = new ReservationSystemListener();
        reservationSystemListener.setMinimumAgeYears(17);
        return reservationSystemListener;
    }

}
