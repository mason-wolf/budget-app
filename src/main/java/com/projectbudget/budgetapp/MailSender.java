package com.projectbudget.budgetapp;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.projectbudget.budgetapp.dao.AccountJdbc;
import com.projectbudget.budgetapp.dao.UserJdbc;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;
public class MailSender {

	private static final SecureRandom secureRandom = new SecureRandom(); 
	private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); 

	public static String generateToken() {
	    byte[] randomBytes = new byte[24];
	    secureRandom.nextBytes(randomBytes);
	    return base64Encoder.encodeToString(randomBytes);
	}
	
	public void SendPasswordResetLink(String username)
	{
        final String email = "masonhwolf@gmail.com";
        final String password = "419nosam";

        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(username)
            );
            message.setSubject("Password Reset");
            String userToken = generateToken();
            UserJdbc.query.addUserToken(username, userToken);
            String passwordResetUrl = "192.168.1.9:5000/ResetPassword/" + userToken;
            message.setText("Click here to reset your passowrd: " + passwordResetUrl);

            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
	}
}
