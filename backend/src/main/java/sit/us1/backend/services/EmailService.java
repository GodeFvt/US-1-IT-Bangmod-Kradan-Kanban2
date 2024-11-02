package sit.us1.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sit.us1.backend.entities.taskboard.Collaboration;


import java.io.UnsupportedEncodingException;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${jwt.issuer}")
    private String hostName;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvitationEmail(String inviterName, String recipientEmail, String boardName, Collaboration.Access accessRole, String boardId) throws MessagingException, UnsupportedEncodingException {
        String subject = inviterName + " ได้เชิญแล้ว";
        String invitationLink = hostName +"/board/" + boardId + "/collab/invitations";
        String content = String.format(
                "%s ได้เชิญคุณเข้าร่วมทำงานบนบอร์ด %s โดยให้สิทธิ์การเข้าถึงระดับ %s<br>" +
                        "คุณสามารถเข้าร่วมได้ผ่านลิงก์นี้: <a href=\"%s\">เข้าร่วมบอร์ด</a>",
                inviterName, boardName, accessRole, invitationLink);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("noreply@intproj23.sit.kmutt.ac.th", inviterName);
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
}
