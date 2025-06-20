package ua.shpp.feniuk.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.jms.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shpp.feniuk.pojo.PojoMessage;
import ua.shpp.feniuk.util.FileWriterService;
import ua.shpp.feniuk.validation.MessageValidator;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiver.class);
    private static final String POISON_PILL = "Poison Pill";
    public static final int LOGGING_INTERVAL = 100000;

    private final ObjectMapper objectMapper;
    private final FileWriterService fileWriterService;
    private final MessageValidator validator;

    public MessageReceiver() {
        this(new FileWriterService(), new MessageValidator());
    }

    public MessageReceiver(FileWriterService fileWriterService, MessageValidator validator) {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.fileWriterService = fileWriterService;
        this.validator = validator;
    }

    public void receiveMessages(MessageConsumer consumer, int stopTimeSeconds, int receiverId) {
        Instant startTime = Instant.now();
        List<PojoMessage> messageBatch = new ArrayList<>();

        try {
            while (true) {
                if (hasTimedOut(startTime, stopTimeSeconds)) {
                    break;
                }

                Message message = consumer.receive(TimeUnit.SECONDS.toMillis(1));

                if (processMessage(message, messageBatch, receiverId)) {
                    break;
                }

                if (shouldProcessBatch(message, messageBatch)) {
                    processBatch(messageBatch, receiverId);
                    messageBatch.clear();
                }
            }

            processBatchIfNotEmpty(messageBatch, receiverId);
        } catch (JMSException e) {
            LOGGER.error("Receiver {}: Error while receiving messages: ", receiverId, e);
        } catch (Exception e) {
            LOGGER.error("Receiver {}: Unexpected error occurred: ", receiverId, e);
        } finally {
            closeConsumer(consumer, receiverId);
        }
    }

    private boolean hasTimedOut(Instant startTime, int stopTimeSeconds) {
        return Duration.between(startTime, Instant.now()).getSeconds() >= stopTimeSeconds;
    }

    private boolean processMessage(Message message, List<PojoMessage> messageBatch, int receiverId) {
        try {
            if (message instanceof TextMessage textMessage) {
                String text = textMessage.getText();
                if (POISON_PILL.equals(text)) {
                    LOGGER.info("Receiver {}: Received poison pill. Processing complete", receiverId);
                    return true;
                }
                parseAndAddMessage(text, messageBatch, receiverId);
            } else if (message != null) {
                LOGGER.warn("Receiver {}: Received unknown message type: {}", receiverId, message);
            }
        } catch (JMSException e) {
            LOGGER.error("Receiver {}: Error reading message: ", receiverId, e);
        }
        return false;
    }

    private void parseAndAddMessage(String text, List<PojoMessage> messageBatch, int receiverId) {
        try {
            PojoMessage pojoMessage = objectMapper.readValue(text, PojoMessage.class);
            messageBatch.add(pojoMessage);
            logMessageCount(messageBatch.size(), receiverId);
        } catch (JsonProcessingException e) {
            LOGGER.error("Receiver {}: Error parsing message: {}", receiverId, text, e);
        }
    }

    private void logMessageCount(int receivedMessageCount, int receiverId) {
        if (receivedMessageCount % LOGGING_INTERVAL == 0) {
            LOGGER.info("Receiver {}: Processed {} messages.", receiverId, receivedMessageCount);
        }
    }

    private boolean shouldProcessBatch(Message message, List<PojoMessage> messageBatch) {
        return !messageBatch.isEmpty() && (message == null || messageBatch.size() >= 100);
    }

    private void processBatchIfNotEmpty(List<PojoMessage> messageBatch, int receiverId) {
        if (!messageBatch.isEmpty()) {
            processBatch(messageBatch, receiverId);
        }
    }

    private void processBatch(List<PojoMessage> messageBatch, int receiverId) {
        for (PojoMessage pojoMessage : messageBatch) {
            try {
                if (validator.isNoConfirmedErrors(pojoMessage)) {
                    fileWriterService.saveValidMessageToFile(pojoMessage);
                } else {
                    fileWriterService.saveInvalidMessageToFile(pojoMessage);
                }
            } catch (IOException e) {
                LOGGER.error("Receiver {}: Error saving message to file: {}", receiverId, pojoMessage, e);
            }
        }
    }

    private void closeConsumer(MessageConsumer consumer, int receiverId) {
        try {
            LOGGER.info("Receiver {}: Closing resources...", receiverId);
            if (consumer != null) {
                consumer.close();
            }
        } catch (JMSException e) {
            LOGGER.error("Receiver {}: Error while closing consumer: ", receiverId, e);
        }
    }
}



