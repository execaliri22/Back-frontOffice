package com.example.front_office.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Service
public class EmailService {
    public void enviarConfirmacion(Integer pedidoId) { }
    @Autowired
    private JavaMailSender mailSender;
    public void enviarVerificacion(String to, String nombre, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String link = "http://localhost:4200/verify?token=" + token; // Link a tu Frontend

            String html = "<h1>Hola " + nombre + "</h1>"
                    + "<p>Gracias por registrarte. Haz clic abajo para activar tu cuenta:</p>"
                    + "<a href='" + link + "'>VERIFICAR MI CUENTA</a>";

            helper.setTo(to);
            helper.setSubject("Verifica tu cuenta en FEEL");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }
}