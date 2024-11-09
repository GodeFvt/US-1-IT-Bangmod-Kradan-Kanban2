package sit.us1.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sit.us1.backend.entities.taskboard.Collaboration;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${file.file-service-host-name}")
    private String hostName;
    @Value("${spring.mail.username}")
    private String mailSenderEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvitationEmail(String inviterName, String recipientEmail, String boardName, Collaboration.Access access, String boardId) throws MessagingException, IOException {
        String subject = inviterName + " has invited you to collaborate";
        String invitationLink = "https://" + hostName  + "/board/" + boardId + "/collab/invitations";

        // โหลดไฟล์ HTML template
        Path templatePath = Paths.get(new ClassPathResource("templates/invitation_email_template.html").getURI());
        String content = Files.readString(templatePath);

        // แทนที่ตัวแปรใน HTML template
        content = content.replace("{{inviterName}}", inviterName)
                .replace("{{boardName}}", boardName)
                .replace("{{access}}", access.toString())
                .replace("{{invitationLink}}", invitationLink);

        // สร้างและส่งอีเมล
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailSenderEmail, inviterName);
        helper.setReplyTo("noreply@" + hostName, "DO NOT REPLY");
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);


    }

}
