package ua.shpp.feniuk.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.jms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ua.shpp.feniuk.pojo.PojoMessage;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class MessageSenderTest {
    private MessageSender sender;
    private MessageProducer producer;
    private Session session;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        producer = mock(MessageProducer.class);
        session = mock(Session.class);
        sender = new MessageSender(producer, session);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSendMessage_Success() throws Exception {
        PojoMessage pojoMessage = new PojoMessage("Test", "19901125-12540", 123, LocalDateTime.now());
        String jsonMessage = objectMapper.writeValueAsString(pojoMessage);

        TextMessage mockTextMessage = mock(TextMessage.class);
        when(session.createTextMessage(jsonMessage)).thenReturn(mockTextMessage);

        sender.sendMessage(pojoMessage);

        verify(session).createTextMessage(jsonMessage);
        verify(producer).send(mockTextMessage);
    }
    @Test
    void testSendMessage_success() throws Exception {
        PojoMessage pojoMessage = new PojoMessage();
        pojoMessage.setName("test");

        TextMessage textMessage = mock(TextMessage.class);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);

        sender.sendMessage(pojoMessage);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(session).createTextMessage(captor.capture());
        verify(producer).send(textMessage);

        String jsonSent = captor.getValue();
        assertTrue(jsonSent.contains("\"name\":\"test\""),
                "The message must contain a name field with the value test");
    }

    @Test
    void testSendMessage_serializationError_logsAndDoesNotSend() throws JMSException {
        PojoMessage pojoMessage = new PojoMessage();

        ObjectMapper mockMapper = mock(ObjectMapper.class);
        try {
            when(mockMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization failed"));
        } catch (Exception e) {
            fail("Unexpected Exception");
        }

        try {
            java.lang.reflect.Field mapperField = MessageSender.class.getDeclaredField("objectMapper");
            mapperField.setAccessible(true);
            mapperField.set(sender, mockMapper);
        } catch (Exception e) {
            fail("Failed to replace objectMapper via reflection");
        }

        sender.sendMessage(pojoMessage);

        verify(session, never()).createTextMessage(anyString());
        verify(producer, never()).send(any());
    }
}