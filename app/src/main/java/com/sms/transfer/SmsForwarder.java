package com.sms.transfer;

import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SmsForwarder {

    private static final String TAG = SmsForwarder.class.getSimpleName();

    // SMTP server settings
    private static final String HOST = "smtp.titan.email";
    private static final String USERNAME = "support@app.strideoperations.com";
    private static final String PASSWORD = "1!Strideoperations";
    private static final String TO_ADDRESS = "giantdev91@gmail.com";

    public void sendToMail(String subject, String messageBody) {
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");  // Using TLS for security

        // Setting up session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            // Create a new email message
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(USERNAME));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(TO_ADDRESS));
            msg.setSubject(subject);
            msg.setText(messageBody);  // Email body content

            // Send the email
            Transport.send(msg);
            Log.d(TAG, "Email sent successfully");

        } catch (MessagingException e) {
            // Handle messaging exception
            Log.e(TAG, "Failed to send email", e);
        }
    }

    public void sendToAPI(String sender, String receiver, String message) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            try {
                // API endpoint
                URL url = new URL("http://43.131.249.243/sms/sms.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Configure the connection
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Build the POST data
                String postData = "uid=" + receiver + "&sid=" + sender + "&data=" + message;

                // Send the request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = postData.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("TAG", "SMS sent successfully");
                    LogPrinter.print("SMS sent successfully");
                } else {
                    Log.d("TAG", "Failed to send SMS. Response code: " + responseCode);
                    LogPrinter.print("Failed to send SMS. Response code: " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                LogPrinter.print("Exception occurred while sending SMS: " + e.getMessage());
            }
        });
    }
}
