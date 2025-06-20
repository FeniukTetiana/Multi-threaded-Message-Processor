package ua.shpp.feniuk.messaging;

import jakarta.jms.Connection;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

public class PoisonPillSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoisonPillSender.class);
    private static final String POISON_PILL = "Poison Pill";

    private PoisonPillSender() {}

    public static void sendPoisonPills(Connection connection, String queueName, int numberOfReceivers) {
        IntStream.range(0, numberOfReceivers).forEach(i -> {
            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                MessageProducer producer = session.createProducer(session.createQueue(queueName));
                producer.send(session.createTextMessage(POISON_PILL));
            } catch (Exception e) {
                LOGGER.error("Error sending poison pill: ", e);
            }
        });
    }
}
