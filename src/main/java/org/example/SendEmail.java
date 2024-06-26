package org.example;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import java.io.IOException;

public class SendEmail {
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
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withRegion(Regions.US_EAST_1)
                            .withCredentials(new AWSStaticCredentialsProvider(
                                    new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)))
                            .build();

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(HTMLBODY))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM)
                    .withConfigurationSetName(CONFIGSET);

            client.sendEmail(request);
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent. Error message: "
                    + ex.getMessage());
        }
    }
}
