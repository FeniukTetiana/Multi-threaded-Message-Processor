package ua.shpp.feniuk.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.jms.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shpp.feniuk.pojo.PojoMessage;

public class MessageSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);
    private final ObjectMapper objectMapper;
    private final MessageProducer producer;
    private final Session session;

    public MessageSender(MessageProducer producer, Session session) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.producer = producer;
        this.session = session;
    }

    public void sendMessage(PojoMessage pojoMessage) throws JMSException {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(pojoMessage);
        } catch (Exception e) {
            LOGGER.error("Error serializing message: {}", pojoMessage, e);
            return;
        }

        TextMessage message = session.createTextMessage(jsonMessage);
        this.producer.send(message);
    }
}



