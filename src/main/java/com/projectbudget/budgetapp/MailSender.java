package com.projectbudget.budgetapp;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.projectbudget.budgetapp.dao.AccountJdbc;
import com.projectbudget.budgetapp.dao.UserJdbc;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;
public class MailSender {

	private static final SecureRandom secureRandom = new SecureRandom(); 
	private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); 
	static final String FROM = "support@pennybudget.com";
	static final String FROMNAME = "Penny Budget";
	static final String SMTP_USERNAME = "";
	static final String SMTP_PASSWORD = "";
	static final String HOST = "";
	static final int PORT = 587;   
    static final String SUBJECT = "Password Reset";
    
	public static String generateToken() {
	    byte[] randomBytes = new byte[24];
	    secureRandom.nextBytes(randomBytes);
	    return base64Encoder.encodeToString(randomBytes);
	}

	public void SendPasswordResetLink(String username) throws Exception
	{
		
        // Create a Properties object to contain connection configuration information.
    	Properties props = System.getProperties();
    	props.put("mail.transport.protocol", "smtp");
    	props.put("mail.smtp.port", PORT); 
    	props.put("mail.smtp.starttls.enable", "true");
    	props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties. 
    	Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information. 
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM,FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(username));
        msg.setSubject(SUBJECT);
        
        String userToken = generateToken();
        UserJdbc.query.addUserToken(username, userToken);
        String passwordResetUrl = "http://www.pennybudget.com/ResetPassword/" + userToken;
        
        msg.setContent("Click here to reset your password: " + passwordResetUrl,"text/html");
        

        // Create a transport.
        Transport transport = session.getTransport();
                    
        // Send the message.
        try
        {
            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
        	
            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
        }
        catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        }
        finally
        {
            transport.close();
        }
	}
	
	public void SendContactForm(String email, String contactForm) throws Exception
	{
		
        // Create a Properties object to contain connection configuration information.
    	Properties props = System.getProperties();
    	props.put("mail.transport.protocol", "smtp");
    	props.put("mail.smtp.port", PORT); 
    	props.put("mail.smtp.starttls.enable", "true");
    	props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties. 
    	Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information. 
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, email));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress("masonhwolf@gmail.com"));
        msg.setSubject("User Message");
        
        msg.setContent(contactForm, "text/html");
        

        // Create a transport.
        Transport transport = session.getTransport();
                    
        // Send the message.
        try
        {
            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
        	
            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
        }
        catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        }
        finally
        {
            transport.close();
        }
	}
}
