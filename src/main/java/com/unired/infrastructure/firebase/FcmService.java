package com.unired.infrastructure.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.unired.util.constants.SecurityConstants;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {

    public void enviarNotificacion(String fcmToken, String title, String body, Map<String, String> data)
            throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data == null ? Collections.emptyMap() : data)
                .build();

        FirebaseMessaging.getInstance().send(message);
        log.info("FCM single notification sent. correlationId={}", MDC.get(SecurityConstants.CORRELATION_ID_KEY));
    }

    public void enviarMasivo(List<String> tokens, String title, String body) throws FirebaseMessagingException {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        int batchSize = 500;
        for (int i = 0; i < tokens.size(); i += batchSize) {
            int end = Math.min(i + batchSize, tokens.size());
            List<String> chunk = tokens.subList(i, end);

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(chunk)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .build();

            FirebaseMessaging.getInstance().sendEachForMulticast(message);
        }

        log.info(
                "FCM bulk notification sent to {} recipients. correlationId={}",
                tokens.size(),
                MDC.get(SecurityConstants.CORRELATION_ID_KEY)
        );
    }
}
