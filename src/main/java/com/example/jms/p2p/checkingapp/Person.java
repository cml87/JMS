package com.example.jms.p2p.checkingapp;

import java.io.Serializable;
import java.time.LocalDate;

public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String firstName;
    private String lastName;
    private LocalDate birthDay;
    String phone;
    String email;


    public Person() {
    }

    public Person(int id, String firstName, String lastName, LocalDate birthDay, String phone, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDay = birthDay;
        this.phone = phone;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(LocalDate birthDay) {
        this.birthDay = birthDay;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDay=" + birthDay +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
