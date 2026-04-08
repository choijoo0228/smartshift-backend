package com.smartshift.smartshift_backend.service.impl;

import com.smartshift.smartshift_backend.entity.Shift;
import com.smartshift.smartshift_backend.service.NotificationService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final SesV2Client sesV2Client = SesV2Client.builder().region(Region.US_EAST_1).build();

    private static final String FROM_EMAIL = "verified-sender@example.com";

    @Override
    public void sendShiftPublishedEmail(Shift shift) {
        if (shift.getAssignedEmployee() == null || shift.getAssignedEmployee().getEmail() == null) {
            return;
        }

        String toEmail = shift.getAssignedEmployee().getEmail();
        String employeeName = shift.getAssignedEmployee().getFirstName() + " " + shift.getAssignedEmployee().getLastName();

        String subject = "Your shift has been published";
        String body = """
                Hello %s,

                Your shift has been published.

                Date: %s
                Start: %s
                End: %s
                Role: %s

                Regards,
                SmartShift
                """.formatted(
                employeeName,
                shift.getShiftDate(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getRoleRequired()
        );

        Destination destination = Destination.builder().toAddresses(toEmail).build();

        Content subjectContent = Content.builder().data(subject).build();

        Content textContent = Content.builder().data(body).build();

        Body emailBody = Body.builder().text(textContent).build();

        Message message = Message.builder().subject(subjectContent).body(emailBody).build();

        EmailContent emailContent = EmailContent.builder().simple(message).build();

        SendEmailRequest request = SendEmailRequest.builder().fromEmailAddress(FROM_EMAIL).destination(destination).content(emailContent).build();

        sesV2Client.sendEmail(request);
    }
}