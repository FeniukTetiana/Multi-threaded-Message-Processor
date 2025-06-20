package ua.shpp.feniuk.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.shpp.feniuk.pojo.PojoMessage;
import ua.shpp.feniuk.util.FileWriterService;
import ua.shpp.feniuk.validation.MessageValidator;

import static org.mockito.Mockito.*;

class MessageReceiverTest {
    private FileWriterService fileWriterService;
    private MessageValidator validator;
    private MessageReceiver receiver;
    private MessageConsumer consumer;

    @BeforeEach
    void setup() {
        fileWriterService = mock(FileWriterService.class);
        validator = mock(MessageValidator.class);
        receiver = new MessageReceiver(fileWriterService, validator);
        consumer = mock(MessageConsumer.class);
    }

    @Test
    void testReceiveMessages_processValidAndInvalidMessages() throws Exception {
        PojoMessage validMessage = new PojoMessage();
        validMessage.setName("valid");
        PojoMessage invalidMessage = new PojoMessage();
        invalidMessage.setName("invalid");

        String validJson = new ObjectMapper().writeValueAsString(validMessage);
        String invalidJson = new ObjectMapper().writeValueAsString(invalidMessage);

        TextMessage validTextMessage = mock(TextMessage.class);
        when(validTextMessage.getText()).thenReturn(validJson);

        TextMessage invalidTextMessage = mock(TextMessage.class);
        when(invalidTextMessage.getText()).thenReturn(invalidJson);

        // Return true only for validMessage, false for invalidMessage
        when(validator.isNoConfirmedErrors(any())).thenAnswer(invocation -> {
            PojoMessage arg = invocation.getArgument(0);
            return "valid".equals(arg.getName());
        });

        when(consumer.receive(anyLong()))
                .thenReturn(validTextMessage)
                .thenReturn(invalidTextMessage)
                .thenReturn(null)
                .thenReturn(createPoisonPillMessage());

        receiver.receiveMessages(consumer, 5, 1);

        verify(fileWriterService).saveValidMessageToFile(argThat(msg -> "valid".equals(msg.getName())));
        verify(fileWriterService).saveInvalidMessageToFile(argThat(msg -> "invalid".equals(msg.getName())));
        verify(consumer).close();
    }


    @Test
    void testReceiveMessages_poisonPillStopsReceiving() throws Exception {
        TextMessage poisonPillMessage = createPoisonPillMessage();

        when(consumer.receive(anyLong()))
                .thenReturn(poisonPillMessage);

        receiver.receiveMessages(consumer, 5, 1);

        verify(consumer).close();
    }

    @Test
    void testReceiveMessages_handlesJmsException() throws Exception {
        when(consumer.receive(anyLong())).thenThrow(new JMSException("Test JMS Exception"));

        receiver.receiveMessages(consumer, 2, 1);

        verify(consumer).close();
    }

    private TextMessage createPoisonPillMessage() throws JMSException {
        TextMessage poisonPill = mock(TextMessage.class);
        when(poisonPill.getText()).thenReturn("Poison Pill");
        return poisonPill;
    }
}
