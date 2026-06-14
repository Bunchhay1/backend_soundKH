package com.soundkh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Async
    public void sendAccessRequestStatus(String toEmail, String trackTitle, String status) {
        if (mailSender == null) return;
        var msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("SoundKH: Access Request " + status);
        msg.setText("Your access request for track \"" + trackTitle + "\" has been " + status.toLowerCase() + ".");
        mailSender.send(msg);
    }

    @Async
    public void sendNewTrackNotification(String toEmail, String channelName, String trackTitle) {
        if (mailSender == null) return;
        var msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("SoundKH: New track from " + channelName);
        msg.setText("\"" + trackTitle + "\" was just uploaded to " + channelName + ".");
        mailSender.send(msg);
    }
}
