package com.example.coalawebbackend.api.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationMailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendVerificationCode(String to, String name, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("[코알라] 이메일 인증번호");
            helper.setText(buildPlainText(name, code), buildHtml(name, code));
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to create email verification message", e);
        }
    }

    private String buildPlainText(String name, String code) {
        String displayName = name == null || name.isBlank() ? "코알라 회원" : name;
        return """
                안녕하세요, %s님.

                코알라 회원가입 이메일 인증번호입니다.

                인증번호: %s

                이 번호는 10분 동안 사용할 수 있습니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(displayName, code);
    }

    private String buildHtml(String name, String code) {
        String displayName = name == null || name.isBlank() ? "코알라 회원" : name;
        return """
                <!doctype html>
                <html lang="ko">
                <body style="margin:0;padding:0;background:#f4f7f5;font-family:Arial,'Apple SD Gothic Neo','Malgun Gothic',sans-serif;color:#172d24;">
                  <div style="max-width:560px;margin:0 auto;padding:32px 18px;">
                    <div style="background:#ffffff;border:1px solid #d7e3dd;border-radius:16px;overflow:hidden;">
                      <div style="background:#123f31;padding:28px 30px;color:#ffffff;">
                        <p style="margin:0 0 8px;font-size:13px;font-weight:700;letter-spacing:.08em;">COALA</p>
                        <h1 style="margin:0;font-size:26px;line-height:1.35;">이메일 인증</h1>
                      </div>
                      <div style="padding:30px;">
                        <p style="margin:0 0 18px;font-size:16px;line-height:1.7;">안녕하세요, <strong>%s</strong>님.<br/>아래 인증번호를 코알라 회원가입 화면에 입력해주세요.</p>
                        <div style="margin:22px 0;padding:22px;border-radius:14px;background:#eef6f2;text-align:center;">
                          <span style="display:block;color:#61756b;font-size:12px;font-weight:700;margin-bottom:8px;">인증번호</span>
                          <strong style="font-size:34px;letter-spacing:.18em;color:#123f31;">%s</strong>
                        </div>
                        <p style="margin:0;color:#60756b;font-size:14px;line-height:1.7;">인증번호는 10분 동안 사용할 수 있습니다. 본인이 요청하지 않았다면 이 메일을 무시해주세요.</p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(displayName, code);
    }
}
