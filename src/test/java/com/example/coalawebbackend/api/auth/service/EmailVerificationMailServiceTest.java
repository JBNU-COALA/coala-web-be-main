package com.example.coalawebbackend.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailVerificationMailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailVerificationMailService emailVerificationMailService;

    @BeforeEach
    void setUp() {
        emailVerificationMailService = new EmailVerificationMailService(mailSender);
        ReflectionTestUtils.setField(emailVerificationMailService, "from", "noreply@coala.test");
    }

    @Test
    void sendVerificationCodeBuildsMultipartMessageForHtmlBody() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailVerificationMailService.sendVerificationCode("user@example.com", "홍길동", "123456");

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        sentMessage.saveChanges();

        assertThat(sentMessage.getSubject()).isEqualTo("[코알라] 이메일 인증번호");
        assertThat(((InternetAddress) sentMessage.getFrom()[0]).getAddress()).isEqualTo("noreply@coala.test");
        assertThat(((InternetAddress) sentMessage.getAllRecipients()[0]).getAddress()).isEqualTo("user@example.com");
        assertThat(sentMessage.getContent()).isInstanceOf(Multipart.class);
    }
}
