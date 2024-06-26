package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SendEmailRest {

    private static final String AWS_ACCESS_KEY = "AKIAZI2LCS55TAXBZ4E7";
    private static final String AWS_SECRET_KEY = "W+HS72aWszJI9fDgVJbHKtloXHXm6++xTShPOrfV";
    private static final String REGION = "us-east-1";  // Change to your region
    private static final String SERVICE = "ses";
    private static final String ENDPOINT = "https://email." + REGION + ".amazonaws.com/v2/email/outbound-emails";

    public static void main(String[] args) throws Exception {
        String from = "susheel@susheel.dev";
        String to = "myself@susheel.dev";
        String subject = "Test Email from java code";
        String body = "This is a test email sent from Java.";
        sendEmail(from, to, subject, body);
    }

    private static void sendEmail(String from, String to, String subject, String body) throws Exception {
        String dateTime = getCurrentTimeInGMT();
        String date = dateTime.substring(0, 8);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject emailData = getEmailData();
        String payload = gson.toJson(emailData);
        String canonicalUri = "/v2/email/outbound-emails";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json\nhost:email." + REGION + ".amazonaws.com\nx-amz-date:" + dateTime + "\n";
        String signedHeaders = "content-type;host;x-amz-date";
        String payloadHash = hash(payload);

        String canonicalRequest = "POST\n" +
                canonicalUri + "\n" +
                canonicalQueryString + "\n" +
                canonicalHeaders + "\n" +
                signedHeaders + "\n" +
                payloadHash;

        String algorithm = "AWS4-HMAC-SHA256";
        String credentialScope = date + "/" + REGION + "/" + SERVICE + "/aws4_request";
        String stringToSign = algorithm + "\n" +
                dateTime + "\n" +
                credentialScope + "\n" +
                hash(canonicalRequest);

        byte[] signingKey = getSignatureKey(AWS_SECRET_KEY, date, REGION, SERVICE);
        String signature = toHex(hmacSHA256(signingKey, stringToSign));

        String authorizationHeader = algorithm + " " +
                "Credential=" + AWS_ACCESS_KEY + "/" + credentialScope + ", " +
                "SignedHeaders=" + signedHeaders + ", " +
                "Signature=" + signature;

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("X-Amz-Date", dateTime)
                    .header("Authorization", authorizationHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        System.out.println("Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
    }

    private static JsonObject getEmailData() {
        JsonObject emailData = new JsonObject();
        emailData.addProperty("ConfigurationSetName", "my-first-configuration-set");
        JsonObject content = new JsonObject();
        JsonObject simple = new JsonObject();
        JsonObject subjectObj = new JsonObject();
        subjectObj.addProperty("Charset", "UTF-8");
        subjectObj.addProperty("Data", "Test Email");

        JsonObject bodyObj = new JsonObject();
        JsonObject text = new JsonObject();
        text.addProperty("Charset", "UTF-8");
        text.addProperty("Data", "This is a test email");

        bodyObj.add("Text", text);
        simple.add("Subject", subjectObj);
        simple.add("Body", bodyObj);
        content.add("Simple", simple);
        emailData.add("Content", content);

        // FromEmailAddress
        emailData.addProperty("FromEmailAddress", "susheel@susheel.dev");

        // Destination
        JsonObject destination = new JsonObject();
        JsonArray toAddresses = new JsonArray();
        toAddresses.add("myself@susheel.dev");
        destination.add("ToAddresses", toAddresses);
        emailData.add("Destination", destination);
        return emailData;
    }

    private static String getCurrentTimeInGMT() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date());
    }

    private static String hash(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return toHex(hash);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static byte[] hmacSHA256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] getSignatureKey(String key, String date, String region, String service) throws Exception {
        byte[] kDate = hmacSHA256(("AWS4" + key).getBytes(StandardCharsets.UTF_8), date);
        byte[] kRegion = hmacSHA256(kDate, region);
        byte[] kService = hmacSHA256(kRegion, service);
        return hmacSHA256(kService, "aws4_request");
    }
}
