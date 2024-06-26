package org.example;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

public class SendEmailWithAttachment {
    static final String FROM = "susheel@susheel.dev";
    static final String TO = "myself@susheel.dev";
    static final String CONFIGSET = "my-first-configuration-set";
    static final String SUBJECT = "Amazon SES test (AWS SDK for Java)";
    static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
            + "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
            + "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>"
            + "AWS SDK for Java</a>";
    static final String TEXTBODY = "This email was sent through Amazon SES "
            + "using the AWS SDK for Java.";

    // AWS Credentials (NOT RECOMMENDED for production use)
    static final String AWS_ACCESS_KEY_ID = "AKIAZI2LCS55TAXBZ4E7";
    static final String AWS_SECRET_ACCESS_KEY = "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";

    public static void main(String[] args) throws IOException {
        try {
            // Create a new SES client.
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withRegion(Regions.US_EAST_1)
                            .withCredentials(new AWSStaticCredentialsProvider(
                                    new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)))
                            .build();

            // Create a new email message.
            MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
            message.setFrom(new InternetAddress(FROM));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(TO));
            message.setSubject(SUBJECT);

            // Create a multipart/mixed parent container.
            MimeMultipart msg = new MimeMultipart("mixed");
            message.setContent(msg);

            // Create a multipart/alternative child container.
            MimeBodyPart wrap = new MimeBodyPart();
            msg.addBodyPart(wrap);

            MimeMultipart msgBody = new MimeMultipart("alternative");
            wrap.setContent(msgBody);

            // Define the text part.
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(TEXTBODY, "text/plain; charset=UTF-8");

            // Define the HTML part.
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(HTMLBODY, "text/html; charset=UTF-8");

            // Add the text and HTML parts to the child container.
            msgBody.addBodyPart(textPart);
            msgBody.addBodyPart(htmlPart);

            // Define the attachment
            MimeBodyPart att = new MimeBodyPart();
            DataSource fds = new FileDataSource(new File("/Users/susheels/Desktop/testing.png"));
            att.setDataHandler(new DataHandler(fds));
            att.setFileName(fds.getName());

            // Add the attachment to the message.
            msg.addBodyPart(att);

            // Send the email.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

            SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage)
                    .withConfigurationSetName(CONFIGSET);

            client.sendRawEmail(rawEmailRequest);
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent. Error message: "
                    + ex.getMessage());
        }
    }
}
