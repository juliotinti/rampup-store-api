package com.julio.rampUp.sendEmail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailHandler {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String password) {
        SimpleMailMessage simpleMail = new SimpleMailMessage();
        simpleMail.setTo(to);
        simpleMail.setSubject("Store account created");
        simpleMail.setText("Thanks for creating a new account in our site! We are happy"
                + " to have you with us!! Please, contact us if you are having any trouble. " + "\n Your password is: "
                + password);
        javaMailSender.send(simpleMail);
    }

}
