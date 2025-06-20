package ua.shpp.feniuk.messaging;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shpp.feniuk.exception.MessageSendingException;
import ua.shpp.feniuk.util.FileWriterService;
import ua.shpp.feniuk.util.PropertiesLoader;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class MessageProcessingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessingService.class);
    private static final MessageGenerator MESSAGE_GENERATOR = new MessageGenerator();
    private static final int LOGGING_INTERVAL = 100000;

    public void processMessages(PropertiesLoader properties, int numberOfMessages, int stopTimeSeconds) {
        String brokerUrl = properties.getProperty("broker.url");
        String queueName = properties.getProperty("queue.name");
        String username = properties.getProperty("user.name");
        String password = properties.getProperty("user.password");
        int numberOfSenders = Integer.parseInt(properties.getProperty("number.of.senders"));
        int numberOfReceivers = Integer.parseInt(properties.getProperty("number.of.receivers"));

        LOGGER.info("NumberOfSenders = {}; NumberOfReceivers = {}; NumberOfMessages = {}",
                numberOfSenders, numberOfReceivers, numberOfMessages);

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        factory.setUserName(username);
        factory.setPassword(password);

        factory.setTrustedPackages(Arrays.asList("java.util", "java.lang", "ua.shpp.feniuk"));

        try (Connection connection = factory.createConnection()) {
            connection.start();

            try (ExecutorService senderService = Executors.newFixedThreadPool(numberOfSenders);
                 ExecutorService receiverService = Executors.newFixedThreadPool(numberOfReceivers)) {

                submitSenderTasks(senderService, connection, queueName, numberOfMessages, numberOfSenders);
                submitReceiverTasks(receiverService, connection, queueName, stopTimeSeconds, numberOfReceivers);

                senderService.shutdown();

                if (!senderService.awaitTermination(stopTimeSeconds, TimeUnit.SECONDS)) {
                    LOGGER.info("Reached stopTimeSeconds - {}, senders are still active," +
                            " initiating forced shutdown", stopTimeSeconds);
                    senderService.shutdownNow();
                }

                PoisonPillSender.sendPoisonPills(connection, queueName, numberOfReceivers);

                receiverService.shutdown();
                if (!receiverService.awaitTermination(stopTimeSeconds, TimeUnit.SECONDS)) {
                    LOGGER.warn("Receiver service did not terminate. Forcing shutdown");
                    receiverService.shutdownNow();
                }

                FileWriterService.closePrinters();
            }
        } catch (JMSException e) {
            LOGGER.error("JMS error during message processing: ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Message processing interrupted: ", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during message processing: ", e);
        }
    }

    private void submitSenderTasks(ExecutorService senderService, Connection connection, String queueName,
                                   int numberOfMessages, int numberOfSenders) {
        IntStream.range(0, numberOfSenders).forEach(i -> {
            final int senderId = i + 1;
            senderService.submit(() -> {
                try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                     MessageProducer producer = createProducer(session, queueName)) {
                    sendMessages(numberOfMessages / numberOfSenders, producer, session, senderId);
                } catch (Exception e) {
                    LOGGER.error("Error in sender thread {}: ", senderId, e);
                }
            });
        });
    }

    private void submitReceiverTasks(ExecutorService receiverService, Connection connection,
                                     String queueName, int stopTimeSeconds, int numberOfReceivers) {
        IntStream.range(0, numberOfReceivers).forEach(i -> {
            final int receiverId = i + 1;
            receiverService.submit(() -> {
                try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                    MessageConsumer consumer = createConsumer(session, queueName);
                    new MessageReceiver().receiveMessages(consumer, stopTimeSeconds, receiverId);
                } catch (Exception e) {
                    LOGGER.error("Error in receiver thread {}: ", receiverId, e);
                }
            });
        });
    }

    private MessageProducer createProducer(Session session, String queueName) throws JMSException {
        Destination destination = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return producer;
    }

    private MessageConsumer createConsumer(Session session, String queueName) throws JMSException {
        Destination destination = session.createQueue(queueName);
        return session.createConsumer(destination);
    }

    private void sendMessages(int numberOfMessages, MessageProducer producer, Session session, int senderId) {
        MessageSender sender = new MessageSender(producer, session);

        IntStream.range(0, numberOfMessages).forEach(i -> {
            try {
                sender.sendMessage(MESSAGE_GENERATOR.generateRawMessage());
                logMessageCount(senderId, i);
            } catch (JMSException e) {
                throw new MessageSendingException("Failed to send message #" + i + " from sender " + senderId, e);
            }
        });
    }

    private void logMessageCount(int senderId, int messageCount) {
        if (messageCount > 0 && messageCount % LOGGING_INTERVAL == 0) {
            LOGGER.info("Sender {} sent {} messages", senderId, messageCount);
        }
    }
}

