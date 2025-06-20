package ua.shpp.feniuk.messaging;

import jakarta.jms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class PoisonPillSenderTest {
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private Queue queue;
    private TextMessage poisonMessage;

    @BeforeEach
    void setUp() throws JMSException {
        connection = mock(Connection.class);
        session = mock(Session.class);
        producer = mock(MessageProducer.class);
        queue = mock(Queue.class);
        poisonMessage = mock(TextMessage.class);

        when(session.createQueue(anyString())).thenReturn(queue);
        when(session.createProducer(queue)).thenReturn(producer);
        when(session.createTextMessage("Poison Pill")).thenReturn(poisonMessage);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
    }

    @Test
    void testSendPoisonPills_sendsCorrectNumberOfMessages() throws JMSException {
        int numberOfReceivers = 3;
        PoisonPillSender.sendPoisonPills(connection, "testQueue", numberOfReceivers);

        verify(connection, times(numberOfReceivers)).createSession(false, Session.AUTO_ACKNOWLEDGE);
        verify(session, times(numberOfReceivers)).createProducer(queue);
        verify(session, times(numberOfReceivers)).createTextMessage("Poison Pill");
        verify(producer, times(numberOfReceivers)).send(poisonMessage);
    }

    @Test
    void testSendPoisonPills_handlesExceptionGracefully() throws JMSException {
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenThrow(new JMSException("Failed"));

        assertDoesNotThrow(() ->
                PoisonPillSender.sendPoisonPills(connection, "testQueue", 2)
        );
    }
}
