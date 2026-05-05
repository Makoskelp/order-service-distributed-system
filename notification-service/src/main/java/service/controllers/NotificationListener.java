package service.controllers;

import jakarta.jms.*;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import service.core.Notification;

public class NotificationListener {

    public static void main(String[] args) {
        String momServerUrl;

        if (args.length == 0) {
            // Broker URL: protocol, hostname and port
            momServerUrl = "tcp://localhost:61616";
        } else {
            momServerUrl = "tcp://%s:61616".formatted(args[0]);
        }

        try {
            // ConnectionFactory creates a classless connection object;
            // ActiveMQConnectionFactory then creates a specific object; turns on listening
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(momServerUrl);
            connectionFactory.setDeserializationWhiteList("service.core,java.lang");

            // Login details
            String brokerUser = "artemis";
            String brokerPassword = "artemis";

            // Establish connection to broker; ActiveMQ Artemis
            Connection connection = connectionFactory.createConnection(brokerUser, brokerPassword);

            // Name the connection; unique
            String connectionId = "notificationListener";
            connection.setClientID(connectionId);

            // Establish a new session (from the connection)
            // b: is session transacted? Meaning collation/grouping of send/acknowledge ops into a unit - transaction;
            // requires explicit commit(all succeed)/rollback(all fail)
            // if false; not transacted (each message send or receive operation will happen independently)
            // if true; transacted
            // Ack mode: for messages received by the session; whose responsibility is it to ack the message (message.ack())
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE); // client here is afqservice

            // Create Queues for connecting with ActiveMQ Artemis broker
            Queue queue = session.createQueue("ORDERS"); // 1-to-1 comms

            // consumer/producer is dependent on the role of the client
            MessageConsumer consumer = session.createConsumer(queue); // consumer subscribes to topic to receive

            // Turn connection ON
            connection.start();
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        // Receive message and parse it
                        // ObjectMessage is specifically designed to carry Java objects that implement the java.io.Serializable interface.
                        // Other JMS message objs types include TextMessage, MapMessage, BytesMessage etc
                        Notification notification = (Notification) ((ObjectMessage) message).getObject();

                        System.out.println("Notification: User " + notification.getUserId() + " your order has been placed!");
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
