package FeedStudy.StudyFeed.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private static final String SENDER_EMAIL = "sangwha0@gmail.com";

    public MimeMessage createMail(String receiver, String authCode) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(SENDER_EMAIL);
        helper.setTo(receiver);
        helper.setSubject("회원 가입 인증 메일");

        String body = String.format(
                """
                        <h3> 회원 가입 인증 코드입니다.</h3>
                        <h1>%s</h1>
                        <p>코드를 입력해 인증을 완료하세요</p>
                """,
                authCode
        );
        helper.setText(body, true);
        return mimeMessage;
    }

    public void sendVerifyMail(String email, String authCode) {
        try {
            MimeMessage mail = createMail(email, authCode);
            mailSender.send(mail);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
