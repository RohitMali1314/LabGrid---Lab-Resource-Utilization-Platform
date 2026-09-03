package com.project.Lab.Resource.Utilization.Platform.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class TwilioSmsService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from-number:}")
    private String fromNumber;

    private boolean enabled = false;

    // =========================================================
    // INITIALIZE TWILIO
    // =========================================================

    @PostConstruct
    public void initialize() {

        if (accountSid == null || accountSid.isBlank()
                || authToken == null || authToken.isBlank()
                || fromNumber == null || fromNumber.isBlank()) {

            System.out.println(
                    "Twilio SMS is not configured. SMS notifications are disabled."
            );

            return;
        }

        try {

            Twilio.init(
                    accountSid,
                    authToken
            );

            enabled = true;

            System.out.println(
                    "Twilio SMS service initialized successfully."
            );

        } catch (Exception e) {

            enabled = false;

            System.err.println(
                    "Twilio initialization failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // SEND SMS
    // =========================================================

    public boolean sendSms(
            String recipientPhone,
            String messageBody
    ) {

        if (!enabled) {

            System.err.println(
                    "Twilio SMS skipped: Twilio is not configured."
            );

            return false;
        }

        if (recipientPhone == null ||
                recipientPhone.isBlank()) {

            System.err.println(
                    "Twilio SMS skipped: recipient phone number is empty."
            );

            return false;
        }

        if (messageBody == null ||
                messageBody.isBlank()) {

            System.err.println(
                    "Twilio SMS skipped: message is empty."
            );

            return false;
        }

        try {

            Message message = Message.creator(

                    new PhoneNumber(recipientPhone),

                    new PhoneNumber(fromNumber),

                    messageBody

            ).create();

            System.out.println(
                    "Twilio SMS sent successfully. SID: "
                            + message.getSid()
            );

            return true;

        } catch (ApiException e) {

            System.err.println(
                    "Twilio SMS failed: "
                            + e.getMessage()
            );

            return false;

        } catch (Exception e) {

            System.err.println(
                    "Unexpected Twilio SMS error: "
                            + e.getMessage()
            );

            return false;
        }
    }
}