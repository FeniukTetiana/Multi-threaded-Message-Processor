## Multi-threaded Message Processing Application
This project is a multi-threaded Java application designed to simulate sending, receiving, and processing a large number of messages through a message queuing system (Apache ActiveMQ). It encompasses message generation, validation, sending, receiving, and saving messages to separate CSV files for valid and invalid entries.

## Features
- Multi-threaded Architecture: Utilizes thread pools for parallel message sending and receiving, optimizing performance.

- Message Generation: Dynamically generates N POJO messages (where N is a command-line argument, N≥1,000,000) with random data, including valid and invalid EDDRs, using the Faker library.

- POJO Structure: {name: “text”, eddr: “EDDR”, count: xxx, created_at: date_time}

- Message Sending: Sends generated messages to a single ActiveMQ queue (<IN-queue>).

- Message Receiving and Processing: Reads messages from the <IN-queue> using multiple handlers and processes them through validators.

- Stop Generation by Time: Message generation can be stopped based on a time limit (specified in seconds in properties) using a "Poison Pill" mechanism to signal receiver termination.

- Data Validation: Messages are validated against specific rules: name length must be ≥7 characters and contain at least one 'a' or 'A'; count must be ≥10; eddr number must be valid.

- Data Persistence:

Valid messages are saved to one CSV file (valid_messages.csv) with two fields: name & count.

Invalid messages are saved to another CSV file (invalid_messages.csv) with additional errors field in JSON format ({"errors":[]}).

- Performance Measurement: Tracks total execution time and message processing speed (messages per second).

- Graceful Shutdown: Employs a "Poison Pill" mechanism to gracefully shut down receiver threads.

## Technologies Used
- Java 21
- Apache ActiveMQ Client: For message broker integration.
- SLF4J and Logback: For logging.
- Jackson (databind, JavaTimeModule): For serializing and deserializing PojoMessage objects to/from JSON.
- Guava: For the Stopwatch utility.
- Apache Commons CSV: For writing data to CSV files.
- Faker: For generating random message data.
- Hibernate Validator and Jakarta Validation API: For message validation using annotations.

## Setup and Installation
Step 1: Clone the repository
git clone <repository-url>

Step 2: Set up Apache ActiveMQ
This application requires a running instance of Apache ActiveMQ.

Download ActiveMQ from the official Apache ActiveMQ website.

Extract the downloaded archive.

Start the ActiveMQ broker:

```Linux/macOS:
cd apache-activemq-X.Y.Z/bin
./activemq start
```

```Windows:
cd apache-activemq-X.Y.Z/bin/win64
activemq.bat start
```

By default, ActiveMQ runs on tcp://localhost:61616. You can check the web console at http://localhost:8161/admin (default credentials: admin/admin).

Step 3: Configure application.properties
Create a file named src/main/resources/application.properties (if it doesn't exist) and configure it as follows:

```properties
broker.url=tcp://localhost:61616
queue.name=QUEUE
stop.time.seconds=5000
number.of.senders=2
number.of.receivers=2
user.name=${USER_NAME:admin}
user.password=${USER_PASSWORD:admin}
```
broker.url: The URL of your ActiveMQ broker.
queue.name: The name of the queue to be used for messages.
stop.time.seconds: The time in seconds after which receiver threads will stop waiting for messages if no "Poison Pill" is received.
number.of.senders: The number of threads that will send messages.
number.of.receivers: The number of threads that will receive messages.
user.name: Username for ActiveMQ connection. Uses admin as default if USER_NAME environment variable is not set.
user.password: Password for ActiveMQ connection. Uses admin as default if USER_PASSWORD environment variable is not set.

Step 4: Build the project
Use Maven to build the project:

```bash
mvn clean package
```

Usage
Run the application from the command line, providing the number of messages to process as a command-line argument:

java -jar target/your-project-name.jar <number_of_messages>

Example:

```bash
java -jar target/multi-threaded-message-processor-${project.version}.jar 1000000
```
This will start the application, which will generate and process 1,000,000 messages.
Upon execution, valid_messages.csv and invalid_messages.csv files will be created in the project's root directory, containing the respective messages.

## Project Structure
The project is structured into several classes, each with a specific responsibility, contributing to the overall message processing workflow:
- MainMultiThread.java: The main class and entry point of the application. It initializes the logging, validates command-line arguments (number of messages), loads application properties, and orchestrates the MessageProcessingService to start the message flow. It also calculates and logs the total execution time and processing speed.
- PropertiesLoader.java: Responsible for loading configuration properties from the application.properties file. It handles potential PropertiesLoadingException if the file is not found.
- ArgumentValidator.java: A utility class dedicated to validating the command-line arguments, ensuring the presence and correct format of the number of messages.
- MessageProcessingService.java: The core orchestration service. It sets up the ActiveMQ connection, creates thread pools for senders and receivers, submits the respective tasks, and manages their shutdown, including the "Poison Pill" mechanism.
- MessageGenerator.java: Responsible for creating PojoMessage instances with randomly generated data, including valid and invalid EDDRs, using the Faker library.
- MessageSender.java: Handles the actual sending of PojoMessage objects to the ActiveMQ queue. It serializes the PojoMessage to JSON before sending.
- MessageReceiver.java: Manages the reception of messages from the ActiveMQ queue. It deserializes messages, applies validation using MessageValidator, and directs messages to FileWriterService based on their validity. It also handles the "Poison Pill" signal for graceful shutdown.
- PojoMessage.java: The data transfer object (DTO) representing the structure of a message. It includes fields for name, eddr, count, createdAt, and an errors field for validation messages. It also contains Javax Validation annotations.
- MessageValidator.java: Utilizes the Hibernate Validator framework to validate PojoMessage instances against defined constraints (e.g., name length, count value, EDDR format). It populates the errors field of invalid messages.
- EDDRValidator.java: A dedicated utility for validating and generating EDDR (Enhanced Document Digest Record) strings. It implements the logic for EDDR format and checksum validation.
- EDDRComponents.java: A simple helper class used internally by EDDRValidator to structure components during EDDR generation.
- EDDRConstraintValidator.java & @ValidEDDR: These classes work together to provide a custom Javax Validation annotation for the eddr field within PojoMessage, allowing for seamless integration of EDDRValidator into the overall validation process.
- FileWriterService.java: Manages writing the processed messages to two separate CSV files: valid_messages.csv for valid messages and invalid_messages.csv for messages that failed validation, including their error details. It ensures proper closing and flushing of the CSV printers.
- PoisonPillSender.java: A utility class responsible for sending "Poison Pill" messages to the message queue, signaling receiver threads to initiate a graceful shutdown.
- MessageSendingException.java & PropertiesLoadingException.java: Custom exception classes to provide more specific error handling for message sending failures and properties loading issues, respectively.
src/main/resources/:
- application.properties: Configuration file.
- logback.xml: Logback logging configuration.

## License
This project is intended for demonstration purposes in my portfolio only. All code is protected by copyright.

You are free to view and clone the repository to review the code. However, you are not permitted to modify, distribute, or use this code or any part of it in any other project (commercial or non-commercial) without my explicit written consent.