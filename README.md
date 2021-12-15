# Java Messaging Service, JMS Fundamentals
### by Bharath Thippireddy, <span style="color:green">Udemy</span>

The courser will make use of APIs JMS 1.x and JMS 2.x. For the JMS provider we'll use **Apache ActiveMQ Artemis**.
We will deploy a Java EE application in Jboss.


Outline:
1. What is messaging and why to use them.
2. What is JMS
3. Types of messages: Point-to-point (P2P) and Publisher-Subscriber (PUB-SUB)
4. Anatomy of a message: Headers, Properties and Payload
5. Load balancing in P2P.
6. Durable and Sharable Subscriptions in PUB-SUB
7. Filtering messages
8. Message guarantee: Acknowledgment and Transaction

The course is divided into 14 sections. I will organize my notes per section.

## 2. Messaging Basics
Messaging is the process of exchanging business data or information across applications, or across components within the same application. The components of a messaging system are:
1. The message: contains the business data as well as networking or routing headers.
2. Sender (an application)
3. Receiver (an application)
4. The Messaging Server, or MOM (Message Oriented Middleware): takes the incoming message and ensure it is delivered to the proper receiver.

The Messaging Server provides useful services such as fault tolerance, load balancing, scalability, transaction management among others. Example of MOMs (JMS providers) are:
- Apache ActiveMQ
- SonicMQ
- IBM WebsphereMQ
- TibcoMQ
- RabbitMQ

  ![image info](./pictures/messaging_system.png)
  
## Messaging advantages
A messaging server decouples the sender and receiver applications allowing for heterogeneous integration. Each application can be a service or a micro-service, developed in different programming languages and running on completely different environments. Moreover, they can be replaced at any time, as they will all fallow the same (abstract) contract set by the MOM. This **increase the flexibility** of our application, also good for microservices.

![image info](./pictures/heterogeneous_integration.png)

Before messaging came in, applications communication was made through a database or making remote procedural calls. This introduced tight coupling among applications ?. Messaging brought in the desired **loose coupling**, making applications need to know nothing about each other. All the request and response process is now mediated through the MOM. 

Compared to web services (HTTP request/response), MOMs are more reliable as request and response messages are persisted, so there are much fewer chances they are lost.

Messaging also **reduce system bottlenecks** and **increase scalability**. If a queue only has one receiver application and there are much more messages in the queue than the app can process, we can introduce more instances of the same consumer application set to listen the same queue. In other words, we can spin off more instances of the consumer application as the load increase, and they will work asynchronously !

![image info](./pictures/system_bottleneck.png)


## What is JMS
JMS is a specification for messaging services in Java applications. It is maintained by Oracle. All Messaging Servers, irrespective of the vendor, must implement it. Developers use the APIs JMS provides. The current version of JMS is 2.0, and it is a big improvement over the earlier version 1.2.

**JMS is for messaging what JDBC is for databases**.

In this course we will use Apache ActiveMQ Artemis as JMS provider. It is a JMS client. Once installed we need to create a broker ? and run it. We'll need to create the <u>administered objects</u>:
- ConnectionFactory
- Queue (for P2P messaging)
- Topic (for PUB-SUB messaging)

The application will access the administered objects through **JNDI** (Java Naming and Directory Interface), from both the producer and the consumer side.

![image info](./pictures/jndi.png)

The JMS provider will give us durability, scalability, acknowledgment, transaction management, clustering and more.

## The two messaging models
JMS supports two types of messaging models: Point-to-Point and Publish/Subscriber.

### Point-to-Point
The Point-to-Point (P2P) messaging model allows sending and receiving messages both synchronously and asynchronously, through channels called **queues**. The JMS provider allow creating queues. There will be a Producer, or Sender, application adding messages to the queue. And there will be a Receiver, or Consumer, application taking the messages from the queue.

In Point-to-Point messaging the message that is put into the queue is consumed by only one application and then removed from the queue. The JMS provider will ensure this.
 
P2P messaging supports **asynchronous fire and forget**, which means that the producer application will send the message to the JMS provider and will forget it. The consumer application will then consume and process it however it wants. However, it also supports **synchronous request/replay messaging**. In this case, after the producer application sends a message to the queue, the consumer application receives it, process it, and sends a message back to the producer app. through a different queue. The producer will read this message as a response.

![image info](./pictures/point_to_point2.png)

### Publish/Subscribe
In the Publish/Subscribe (PUB-SUB) messaging model the messages are published to a virtual channel called **topic**. We will have only one producer, but many consumers called "Subscribers". The same message will be received my multiple subscribers (applications). 

In the PUB-SUB messaging model messages are automatically broadcasted to the consumers, without them having to request or pull the topic. In other words, it is a push model. So, after the producer sends the message to the topic, the JMS provider will ensure the message is sent to all the subscribers subscribed to that topic.

![image info](./pictures/publish_subscribe.png)


## Apache ActiveMQ installation
I installed Apache ActiveMQ 2.19 by downloading it from https://activemq.apache.org/components/artemis/download/ and unzipping it in /opt.

Once "installed" I need to go to `/opt/apache-artemis-2.19.0/bin/` and run 
```text
$ ./bin/artemis create brokers/mybroker
```
to _create_ a **JMS broker**, or server. In this case the name I chose for the broker is "mybroker" . I decided to create my brokers inside the directory `/opt/apache-artemis-2.19.0/brokers/`, but they can be created anywhere. When creating the broker, user and password properties will be asked to be set, as well as whether we want to allow anonymous access to the broker.

Now go to the `bin` directory inside the created broker directory and run
```text
$ artemis run
```
sudo privileges may be needed depending on where we created the server. This command will create a set of predefined queues and topics on the fly. Startup logs will be printed out with all the  useful information about the started services, similar to when we start an application server such as Wildfly.

The file `mybroker/etc/broker.xml` will be a configuration file with lots of configurations, including queues and topics. We can edit this file directly, or the jndi.properties file of our project, to create queues. It seems that if we ask for a queue that doesn't extis, Artemis can create it for us in the fly.

## Components of the JMS 1.x API
The 7 important components (classes) of the JMS 1.x API are:
1. Connection Factory
2. Destination: a queue in case of P2P messaging, or a topic, in case of PUB-SUB messaging.
3. Connection
4. Session
5. Message
6. Message Producer
7. Message Consumer
 
The **ConnectionFactory** and the **Destination** are provided by the JMS provider, which will create and put them in the JNDI registry from where we can retrieve them. From the ConnectionFactory we get a Connection. From the Connection we then get a Session. 

A Session is a unit of work. We can create any number of session using a single connection to the JMS provider (server?). From the Session we can create a Message and a MessageProducer to send the message. In the consumer part of the application we'll also use a Session to create a MessageConsumer to consume the message. We will have queue producers/consumers and topic producer/consumer.

So we have JNDI tree -> ConnectionFactory -> Session -> Message -> MessageProducer or Consumer.

## Project setup
A pom file for our messaging example project can be:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>jmsfundamentals</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>JMS Fundamentals</name>
    <description>Demo project for JMS</description>

    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.2.0.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>javax.annotation</groupId>
            <artifactId>javax.annotation-api</artifactId>
            <version>1.3.2</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.apache.activemq/artemis-jms-client-all -->
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>artemis-jms-client-all</artifactId>
            <version>2.6.4</version>
        </dependency>

    </dependencies>
</project>
```

Q: What's the difference between a JMS client and a JMS provider?

The javax and Spring dependencies are not strictly needed, but I include them because I want to use Spring and annotations configuration. ActiveMQ will read a properties file `jndi.properties` in the resources' directory (in the class path). In this file we will specify the `InitialContext` class, as well as some other properties that will be used to look up for resources in the JNDI tree of the JMS server.

## Sending and receiving messages from a Queue

Our main class can be: 
```java

/**
 *  JMS 1.1 example. All the boilerplate code is removed with JMS 2.0
 *  */
public class FirstQueue {
  public static void main(String[] args){

    InitialContext initialContext = null;
    Connection connection = null;
    try {

      // obtain a reference to the root of the JNDI tree of the naming server
      // of the JMS server
      initialContext = new InitialContext();

      // get the resources in the JNDI tree we need
      Queue queue = (Queue) initialContext.lookup("queue/myQueue");
      ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

      connection = connectionFactory.createConnection();
      Session session = connection.createSession();

      MessageProducer producer = session.createProducer(queue);
      MessageConsumer consumer = session.createConsumer(queue);

      TextMessage message = session.createTextMessage("I am the creator of my destiny");
      producer.send(message);

      System.out.println("Message sent: " + message.getText());

      /** Now we'll consume the messages  */

      // start the flow of messages in the queue towards the consumers.
      // tell the JMS provider we are ready to consume the messages
      connection.start();

      // here we block. This is synchronous.
      // throw exception if message is not received after 5 seconds
      TextMessage messageReceived = (TextMessage) consumer.receive(5000);

      System.out.println("Message received: " + messageReceived.getText());

    } catch (NamingException e) {
      e.printStackTrace();
    } catch (JMSException e){
      e.printStackTrace();
    } finally {
      if (initialContext != null) {
        try {
          initialContext.close();
        } catch (NamingException e) {
          e.printStackTrace();
        }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (JMSException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
```
The `jndi.propeties` file used is:
```text
# initial context class
java.naming.factory.initial=org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory

# a ConnectionFactory resource
# default location where the JNDI server run??
connectionFactory.ConnectionFactory=tcp://localhost:61616

# a queue resource
queue.queue/myQueue=myQueue
```
When we create an instance of InitialContext with
```java
initialContext = new InitialContext();
```
it will automatically use the information defined in the application.properties file. I think we will get the `InitialContext` from the factory class we have specified in property `java.naming.factory.initial`. And this factory class is specific to the JMS vendor we are using.

In the queue name specification, the first "queue." indicates it is a queue type of administered object. There is no queue named "myQueue". It will be created dynamically at run time. However, I don't understand whether the JNDI name will be "queue/myQueue" (left member) or "myQueue" (right member). In the code we use "queue/myQueue" as argument to `lookup()` anyway, buh.

These are properties that the ArtemisMQ JMS broker host needs to setup a JNDI tree. I'm not sure whether these properties will just be that, properties to be loaded by our application, or will also create bindings and resources in the JMS server. But I think it defines bindings with the names and types we specify. For example, the line:
```text
connectionFactory.ConnectionFactory=tcp://localhost:61616
```
defines a resource of type "connectionFactory" with name "ConnectionFactory" and with value "tcp:://localhost:61616".

**---->** For the case of the queue, the teacher said it is created by the JMS provider the first time we `lookup()` for it in our code. May be the Connection Factory is also created the first time we lookup for it, buh.

Each call to `consumer.receive()` will consume a message from the queue and will delete it. messages are consumed in order, in a first-in first-out fashion. We can call several times to `.receive()` to consume each message in the queue in order. 

Notice that a call to `receive()` is blocking. In other words, the program will stop until this method gets a message from the queue. If there are no messages in the queue, it will wait for one forever, I think, if we don't specify a timeout. If after the timeout no message has been retrieved from the queue (no messages have arrived while we were waiting), the method will return with `null`.  

## Sending and receiving messages from a Topic
Sending and receiving messages form a Topic follows the same pattern as for a Queue:
1. Obtain the reference to the `InitialContext`.
2. Retrieve the Topic or Queue resource, as well as the ConnectionFactory resource, invoking with `lookup()` from the initial context and the resource names.
3. Create a Session from the Connection
4. Create MessageProducer and MessageConsumer from the Session and passing the destination queue or topic
5. Create a TextMessage from the Session.
6. Send the TextMessage with the producer
7. start the connection, so the consumers can receive messages from the queues
8. receive the TextMessage with the consumers invoking `.receive()`

Notice that consumers will only receive messages sent to the topic after they have subscribed.
Here is the example code
```java
public class FirstTopic {

    public static void main (String[] args) throws NamingException, JMSException {

        InitialContext initialContext = initialContext = new InitialContext();
        Topic topic = (Topic) initialContext.lookup("topic/myTopic");
        ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession();

        MessageProducer producer = session.createProducer(topic);

        // This is how we subscribe a consumer to the topic
        // Consumers will only receive messages sent to the topic after they subscribed
        MessageConsumer consumer1 = session.createConsumer(topic);
        MessageConsumer consumer2 = session.createConsumer(topic);

        TextMessage message = session.createTextMessage("All the power is within me. I can do anything and everything.");
        producer.send(message);

        // tell the JMS provider that the consumer are ready to receive messages.
        connection.start();

        TextMessage message1 = (TextMessage) consumer1.receive();
        System.out.println("Consumer 1 message received: "+ message1.getText());

        TextMessage message2 = (TextMessage) consumer2.receive();
        System.out.println("Consumer 2 message received: "+ message2.getText());

        connection.close();
        initialContext.close();

    }
}
```
Question: When all consumers subscribed to the queue have received a message, is that message removed from the queue?

## Looping through the messages in Queue
We can loop through the messages in a queue <u>without consuming them</u> (without removing them from the queue). For this, we use another object that can be obtained from the `Session` and the destination, like when we obtain a `MessageProducer` or a `MessageConsumer`. It is called `QueueBrowser`:

```java

/**
 * JMS 1.1 example. All the boilerplate code is removed with JMS 2.0
 */
public class QueueBrowserDemo {
    public static void main(String[] args) {

        InitialContext initialContext = null;
        Connection connection = null;
        try {

            // obtain a reference to the root of the JNDI tree of the naming server
            // of the JMS server
            initialContext = new InitialContext();

            Queue queue = (Queue) initialContext.lookup("queue/myQueue");
            ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

            connection = connectionFactory.createConnection();
            Session session = connection.createSession();

            MessageProducer producer = session.createProducer(queue);
            MessageConsumer consumer = session.createConsumer(queue);

            TextMessage message1 = session.createTextMessage("Message 1");
            TextMessage message2 = session.createTextMessage("Message 2");

            producer.send(message1);
            producer.send(message2);

            QueueBrowser browser = session.createBrowser(queue);
            Enumeration messageEnum = browser.getEnumeration();

            // show the messages currently in the queue
            System.out.println("Messages in the queue are:");
            while (messageEnum.hasMoreElements()) {
                TextMessage message = (TextMessage) messageEnum.nextElement();
                System.out.println("Browsing: "+ message.getText());
            }


            /** Now we'll consume the messages  */

            // start the flow of messages in the queue towards the consumers.
            // tell the JMS provider we are ready to consume the messages
            connection.start();

            // here we block. This is synchronous.
            // throw exception if message is not received after 5 seconds
            System.out.println("Consuming messages in the queue: ");
            TextMessage messageReceived = (TextMessage) consumer.receive(5000);
            System.out.println("Message received: " + messageReceived.getText());

            messageReceived = (TextMessage) consumer.receive(5000);
            System.out.println("Message received: " + messageReceived.getText());

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            if (initialContext != null) {
                try {
                    initialContext.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```
This will print:
```text
Messages in the queue are:
Browsing: Message 1
Browsing: Message 2
Consuming messages in the queue: 
Message received: Message 1
Message received: Message 2
```

## JMS 2.0 introduced in Java EE 7
JMS 2.0 makes it much easier to send and receive messages. It shortens the steps we saw before. JMS 2.0 provides a new class that is the combination of a Connection and a Session, `JMSContext`, from which  create producer and consumer objects `JMSProducer` and a `JMSConsumer`. All `JMSContext`, `JMSProducer` and `JMSConsumer` implement `java.lang.AutoClosable`, so we will not need to close them explicitly, provided we use them inside a try/catch block. Moreover,`JMSProducer` and `JMSConsumer` give us easy access to a message's Headers, Properties and Body. 

JMS 2.0 in Java EE 7 compatible application servers allows injecting the Connection Factory and destination resources easily into our code as:
```java
@Inject
@JMSConnectionFactory("jms/connectionFactory") private JMSContext context;

@Resource(lookup = "jms/dataQueue")
private Queue dataQueue
```

With JMS 2.0 we'll create a connection factory object, `ActiveMQConnectionFactory`, and from it we'll instantiate directly a context object, instead of a session, `JMSContext`. The way we'll use this context object to send and receive messages to queues will be similar to what we saw before with the session object.

I think the line 
```java
ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
```
will use default info, or info defined in the `jndi.properties` file.

With JMS 2.0 it is also possible to make our own customized connection factory. So far we have been using the default connection factory provided by the JMS vendor. This is done through the annotations `@JMSConnectionFactoryDefinitions` and `JMSConnectioFactoryDefinition`, or through xml configuration `<jms-connection-factory>`.

This is how we send and receive a message from a queue with JMS 2.0:
```java
/**
 *  JMS 2.0 example
 *  */
public class JMSContextDemo {
   public static void main(String[] args) throws NamingException {

       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

        // JMSContext will have the Connection and the Session
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           jmsContext.createProducer().send(queue, "Arise awake and stop not till the goal is reached");
           String messageReceived = jmsContext.createConsumer(queue).receiveBody(String.class);

           System.out.println("Message is: "+ messageReceived);

       }
   }
}
```
Notice that a producer `JMSProducer` is created from the context without specifying the destination to which we want to send messages later with it. The destination is specified when we can `send()` on the producer:
```java
JMSProducer producer = jmsContext.createProducer();
producer.send(queue, "Arise awake and stop not till the goal is reached");
```
On the other hand, a consumer `JMSConsumer` do need the destination from which we'll consume messages with it after:
```java
JMSConsumer consumer = jmsContext.createConsumer(queue);
String messageReceived = consumer.receiveBody(String.class);
```

## JMS message

Messages communicate all the data an event possible in the JMS specification. A message is divided into three parts: _Header_, _Properties_ and _Body_ (or Payload).
The Body can be of different types: byte message, text message, object message etc.

The message headers are metadata. There are provider-set headers and developer-set headers. 

**Provider-set headers** are automatically assigned by the provider to a message when it is sent. These may include:
- JMSDestination: queue or topic to which the message should be delivered.
- JMSDeliveryMode: eg. persistence or non- persistence message etc.
- JMSMessageId: message unique id assigned by the provider, so the consumer can identify a particular message.
- JMSTimeStamp: timestamp at which the message was received by the JMS provider.
- JMSExpiration: time at which, if reached, the message will be expired?.
- JMSRedelivered: set by the provider when it re-delivers the message to a particular consumer, because that message was not delivered in its prior try ?
- JMSPriority: an integer ranged 0-9 meaning priority of the message. 0-4 is called 'normal priority', 5-9 is called high priority. 

**Developer-set headers** include:
- JMSReplayTo: The producer application will set this header so that the consumer application know which destination it should replay back on, in a request/replay scenario.
- JMSCorrelationID: also used in a request/replay scenario. The consumer application will set it with the JMSMessageId of the request message for which it is sending the response back. This way the producer application can relate the incoming response with the particular request it previously sent. So it is to "correlate" request with its response.
- JMSType: set by the producer application. Used to convey what type of message is being sent

Any Developer-set header can be set in the message itself, before sending it. It can also be set in the producer, for all messages sent with it. For example:
```java
JMSProducer producer = jmsContext.createProducer();
//producer.setJMSReplyTo(replyQueue);
TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
message.setJMSReplyTo(replyQueue);
producer.send(requestQueue, message);
```

Properties are also divided into provider-set properties and developer-set properties. At the producer side, **developer-set** (or **application specific**) properties can be added to the message as key-value pair, with method `setXXXProperty`. XXX stands for a particular type, such as integer, boolean, string etc (primitives ?). At the consumer end we can then retrieve any property with `getXXXPropety`. 

**Provider-set** properties include JMSXUserID, JMSXAppID, JMSXProducerTXID, JMSXConsumerTXID, JMSXRcvTimeStamp, JMSDeliveryCount, JMSXState, JMSXGroupID and JMSXGroupSeq. It is not mandatory for the provider to support all of them. We don't usually touch the provider-set properties.

The properties JMSXGroupID and JMSXGroupSeq are used when we work with groups of messages, ie. instead of sending one message we can send a group of messages and process them.

Messages can be filtered by both its headers and its properties.

## Message priority
The priority of the messages present in a queue affect the order in which they are received when we call `receive()` in a consumer. Messages with higher priority will be received first. Instead of setting the priority to the message itself, we set the priority to the producer with which we'll send it. Once a producer has been set with a priority, all messages sent with it will have that priority in the queue. Here is an example:
```java
public class MessagePriority {
   public static void main(String[] args) throws NamingException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       // JMSContext will have the ConnectionFactory and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

            String[] messages = {"Message One", "Message Two", "Message Three"};

           JMSProducer producer = jmsContext.createProducer();

           producer.setPriority(3);
           producer.send(queue, messages[0]);

           producer.setPriority(1);
           producer.send(queue, messages[1]);

           producer.setPriority(9);
           producer.send(queue, messages[2]);

           JMSConsumer consumer = jmsContext.createConsumer(queue);

           for (int i = 0; i < 3; i++ ){
               Message receivedMessage = consumer.receive();
               int messagePriority = receivedMessage.getJMSPriority();
               String messageBody = receivedMessage.getBody(String.class);

               System.out.printf("Message priority: %d, Message body: %s\n", messagePriority, messageBody);
               //System.out.println(consumer.receiveBody(String.class));
           }
       } catch (JMSException e) {
           e.printStackTrace();
       }
   }
}
```
Notice how we retrieve the priority of the message with `getJMSPriority()` which return the value in this header.

If we don't set a priority for the messages in the producer, a default priority value will be set for them, say 4. In this case the messages will be retrieved by a consumer in the order they were received by the JMS provider.

## Request-replay scenario
A consumer application can send a message to queue, a _request_, from which the message is consumed after by a consumer application. The consumer application can then consume and process this message, sending another one to another queue, which can be consumed by the producer, as a _replay_. Here is an example:
```java
public class RequestReplyDemo {
   public static void main(String[] args) throws NamingException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
       Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           /* producer application */
           JMSProducer producer = jmsContext.createProducer();
           producer.send(requestQueue, "Arise awake and stop not till the goal is reached");


           /* consumer application*/
           JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
           String messageReceived = consumer.receiveBody(String.class);
           System.out.println(messageReceived);

           JMSProducer replyProducer = jmsContext.createProducer();
           replyProducer.send(replyQueue, "You are awesome!!");
           /***************************/


           /* producer application */
           JMSConsumer replyConsumer = jmsContext.createConsumer(replyQueue);
           System.out.println(replyConsumer.receiveBody(String.class));

       }
   }
}
```
When a producer sends a messages, it can specify in which queue it is expecting the reply to that message. However, the convention is to specify this information in through a header of the message being sent, `JMSReplayTo`. So the message itself can convey where we should send the reply to it. Here is how we do it:
```java
public class RequestReplyDemo {
   public static void main(String[] args) throws NamingException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
       Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           /* producer application */
           JMSProducer producer = jmsContext.createProducer();
           //producer.setJMSReplyTo(replyQueue);
           TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
           
           // set the JMSReplayTo header
           message.setJMSReplyTo(replyQueue);
           producer.send(requestQueue, message);

           /* consumer application*/
           JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
           // consume the whole message so we can get also its metadata
           TextMessage messageReceived = (TextMessage) consumer.receive();
           System.out.println(messageReceived.getText());

           JMSProducer replyProducer = jmsContext.createProducer();
           replyProducer.send(messageReceived.getJMSReplyTo(), "You are awesome!!");

           /***************************/

           /* producer application */
           JMSConsumer replyConsumer = jmsContext.createConsumer(messageReceived.getJMSReplyTo());
           System.out.println(replyConsumer.receiveBody(String.class));

       } catch (JMSException e) {
           e.printStackTrace();
       }
   }
}
```
The queue where a producer wants to receive a message can be a `TemporaryQueue`, which is a queue directly created from a context instead of being retrieved from the JNDI tree. "A `TemporaryQueue` object is a unique Queue object created for the duration of a Connection. It is a system-defined queue that can be consumed only by the Connection that created it." ??

We create a temporary queue as:
```java
TemporaryQueue replyQueue = jmsContext.createTemporaryQueue();
```
When there are multiple application sending requests and replies, it may be useful to associate a particular request to a particular replay. This is where headers `messageId` and `correlationID` become useful. Header `JMSmessageID` is set automatically by the JMS provider when we send a message. When we want to make clear to which request message a given reply message corresponds to, we set the `JMSCorrelationID` of the replay message equal to the `JMSMessageID` of the request message. Here is how we do it:
```java
public class RequestReplyDemo {
   public static void main(String[] args) throws NamingException, JMSException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
       //Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
            JMSContext jmsContext = cf.createContext()) {

           /* producer application */
           JMSProducer producer = jmsContext.createProducer();
           //producer.setJMSReplyTo(replyQueue);
           TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
           TemporaryQueue replyQueue = jmsContext.createTemporaryQueue();

           message.setJMSReplyTo(replyQueue);
           producer.send(requestQueue, message);
           System.out.printf("Message sent by producer: [%s] %s\n", message.getJMSMessageID(), message.getText());

           Map<String, TextMessage> requestMessages = new HashMap<>();
           requestMessages.put(message.getJMSMessageID(), message);

           /* consumer application*/
           JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
           TextMessage messageReceived = (TextMessage) consumer.receive();
           System.out.printf("Message received by consumer: [%s] %s\n", messageReceived.getJMSMessageID(), messageReceived.getText());

           JMSProducer replyProducer = jmsContext.createProducer();
           TextMessage replyMessage = jmsContext.createTextMessage("You are awesome!!");
           replyMessage.setJMSCorrelationID(messageReceived.getJMSMessageID());
           replyProducer.send(messageReceived.getJMSReplyTo(), replyMessage);
           System.out.printf("Reply message sent by producer: [correlation%s] %s\n", replyMessage.getJMSCorrelationID(), replyMessage.getText());

           /***************************/

           /* producer application */
           JMSConsumer replyConsumer = jmsContext.createConsumer(messageReceived.getJMSReplyTo());
           TextMessage replyReceived = (TextMessage) replyConsumer.receive();
           System.out.printf("Reply message received by producer: [correlation%s] %s\n", replyReceived.getJMSCorrelationID(), replyReceived.getText());

           System.out.printf("The reply corresponds to message: %s", requestMessages.get(replyReceived.getJMSCorrelationID()).getText());

       }
   }
}
```
Notice that the correlationId of the consumed reply, in the producer side, will result equal to the correlationId we set in the reply message, in the consumer side, only if the replay queue is a `TemporaryQueue`, I don't know why ??? 

Notice also how we can add sent messages in the producer side to a Map, using as key the messageID, and then retrieve them using as key the correlationId of the reply message. This is how we set the request-reply linkage.

## Message expiration
We can set an expiry time, or time-to-live, to a message with `setTimeToLive`.  If a message is not consumed before it is expired, it will be moved to an "expiry queue" automatically, disappearing from the queue we send it to. We set the timeToLive in the producer with which we'll send the message, not on the message itself.

The expiry queue will be set automatically by the JMS provider. In the configuration file `broker.xml` it appears configured with name `ExpiryQueue`. From our code we can get the JNDI reference to it if we include in the `jndi.propeties`:
```text
queue.queue/expiryQueue=ExpiryQueue
```
Here is the example:
```java
public class MessageExpirationDemo {
  public static void main(String[] args) throws NamingException, InterruptedException {

    // get the reference to the root context of the JNDI tree
    // This will read the properties file
    InitialContext initialContext = new InitialContext();
    Queue queue = (Queue) initialContext.lookup("queue/myQueue");

    Queue expiryQueue = (Queue) initialContext.lookup("queue/expiryQueue");

    // JMSContext will have the Connection and the Session
    // I think this is either using defaults or properties from jndi.properties file
    try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
         JMSContext jmsContext = cf.createContext()) {

      JMSProducer producer = jmsContext.createProducer();
      // the message will expire after 2 seconds
      producer.setTimeToLive(2000);
      TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
      producer.send(queue, message);

      // we wait for three seconds, so we make the message expire
      Thread.sleep(3000);

      // wait for only one second, after which, if there is no message we'll get null back
      TextMessage messageReceived = (TextMessage) jmsContext.createConsumer(queue).receive(1000);
      System.out.println(messageReceived); // null

      System.out.println("Printing messages in the ExpiryQueue:");
      System.out.println(jmsContext.createConsumer(expiryQueue).receiveBody(String.class));
      // Arise awake and stop not till the goal is reached
    }
  }
}
```

## Message delivery delay
Other than the time-to-live, in the producer we can set a delivery delay period of time, so that messages sent with it will actually be sent after such time, when we call `send()`. Here is the example:
```java
public class MessageDelayDemo {
   public static void main(String[] args) throws NamingException, InterruptedException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       Queue expiryQueue = (Queue) initialContext.lookup("queue/expiryQueue");


       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           JMSProducer producer = jmsContext.createProducer();
           producer.setDeliveryDelay(3000);
           TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
           producer.send(queue, message);

           // we'll get the message after three seconds, cuz that's the 
           // delay in the producer
           TextMessage messageReceived = (TextMessage) jmsContext.createConsumer(queue).receive();
           System.out.println(messageReceived);

       }
   }
}
```

## Custom message properties
Custom message properties can be set in the message or in the producer. Properties can then be retrieved in the consumer side, from the message. We can set a property of any primitive type, or even of Object type, which determines which setter and getter method we use for the property. When we set a property, we must specify a name for it, other than a (proper type) value. For example:
```java
public class MessagePropertiesDemo {
   public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       Queue expiryQueue = (Queue) initialContext.lookup("queue/expiryQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           JMSProducer producer = jmsContext.createProducer();
           TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
           message.setBooleanProperty("loggedIn", true);
           message.setStringProperty("userToken", "abc123");
           producer.send(queue, message);

           // we'll get the message after three seconds
           TextMessage messageReceived = (TextMessage) jmsContext.createConsumer(queue).receive();
           System.out.println(messageReceived);
           System.out.println(messageReceived.getBooleanProperty("loggedIn"));
           System.out.println(messageReceived.getStringProperty("userToken"));

       }
   }
}
```

## Types of messages
We can use 5 different types of messages when working with JMS. They all implement the <u>`Message` interface</u>:
1. TextMessage: Used to send text data as Strings
2. ByteMessage: Bytes, binary level 
3. ObjectMessage: send an object that can be (de)serialized.
4. StreamMessage: Stream Java objects and wrap primitive types? The consumer must read the stream in the same order it was written by the producer ?
5. MapMessage: A set of key-value pairs

### Byte message
This is how we can send and receive a bytes message:
```java
public class MessageTypesDemo {
   public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           JMSProducer producer = jmsContext.createProducer();
           BytesMessage bytesMessage = jmsContext.createBytesMessage();
           bytesMessage.writeUTF("John"); // first payload ?
           bytesMessage.writeLong(123l);  // second payload ?
           producer.send(queue, bytesMessage);

           BytesMessage messageReceived = (BytesMessage) jmsContext.createConsumer(queue).receive();
           System.out.println(messageReceived.readUTF());
           System.out.println(messageReceived.readLong());
       }
   }
}
```
### Stream message
A stream message works exactly in the same way ???:
```java
public class MessageTypesDemo {
   public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           JMSProducer producer = jmsContext.createProducer();
           StreamMessage streamMessage = jmsContext.createStreamMessage();
           streamMessage.writeBoolean(true); // first payload ?
           streamMessage.writeFloat(2.5f);  // second payload ?
           producer.send(queue, streamMessage);

           StreamMessage messageReceived = (StreamMessage) jmsContext.createConsumer(queue).receive();
           System.out.println(messageReceived.readBoolean());
           System.out.println(messageReceived.readFloat());
       }
   }
}
```
### Map message
Map messages carry keys and values. To set the payload, or write values to a map message, we use setter methods passing name and value pairs. For example:
```java
public class MessageTypesDemo {
   public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

       // get the reference to the root context of the JNDI tree
       // This will read the properties file
       InitialContext initialContext = new InitialContext();
       Queue queue = (Queue) initialContext.lookup("queue/myQueue");

       // JMSContext will have the Connection and the Session
       // I think this is either using defaults or properties from jndi.properties file
       try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
                JMSContext jmsContext = cf.createContext()) {

           JMSProducer producer = jmsContext.createProducer();

           MapMessage mapMessage = jmsContext.createMapMessage();
           mapMessage.setBoolean("isCreditAvailable", true);
           producer.send(queue,mapMessage);

           MapMessage messageReceived = (MapMessage) jmsContext.createConsumer(queue).receive();
           System.out.println(messageReceived.getBoolean("isCreditAvailable"));

       }
   }
}
```
### Object message
When and object implements interface `Serializable`, we can send it through a `ObjectMessage`. We will create an `ObjectMessage` and `set` and `get` the object it carries when sending and receiving the message, respectively. For example:
```java

public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private String name;

    // getters and setters
    
}
```
```java
public class MessageTypesDemo {
    public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

        // get the reference to the root context of the JNDI tree
        // This will read the properties file
        InitialContext initialContext = new InitialContext();
        Queue queue = (Queue) initialContext.lookup("queue/myQueue");

        // JMSContext will have the Connection and the Session
        // I think this is either using defaults or properties from jndi.properties file
        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            JMSProducer producer = jmsContext.createProducer();

            Patient patient = new Patient(123, "John");
            ObjectMessage objectMessage = jmsContext.createObjectMessage();

            objectMessage.setObject(patient);
            producer.send(queue, objectMessage);

            ObjectMessage messageReceived = (ObjectMessage) jmsContext.createConsumer(queue).receive();
            Patient patientReceived = (Patient) messageReceived.getObject();
            System.out.println(patientReceived.toString());

        }
    }
}
```

### Sending Java types and objects directly
Until now, we have been creating different types of messages to be sent with `send()`: `TextMessage`, `ByteMessage`, `StreamMessage`, `MapMessage` and `ObjectMessage`. All these are actually types that implement the interface `javax.jms.Message`. This is just one of the several types we can pass to `send()`, after the queue specification. Method `send()` has overloaded versions accepting types `String`, `byte[]`, `Map<>` and `Serializable` (an interface), other than `Message`. When we send one of these other types, the type will be set in the body of the message, so we'll be able to retrieve it with `receiveBody()`, invoking from a consumer and specifying the body type. For example, let's send and receive the type `Patient` just seen, which implements `Serializable`:
```java
public class MessageTypesDemo {
    public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

        // get the reference to the root context of the JNDI tree
        // This will read the properties file
        InitialContext initialContext = new InitialContext();
        Queue queue = (Queue) initialContext.lookup("queue/myQueue");

        // JMSContext will have the Connection and the Session
        // I think this is either using defaults or properties from jndi.properties file
        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            JMSProducer producer = jmsContext.createProducer();

            Patient patient = new Patient(123, "John");
            producer.send(queue, patient);

            Patient patientReceived = jmsContext.createConsumer(queue).receiveBody(Patient.class);
            System.out.println(patientReceived.toString());

        }
    }
}
```
However, if we want to set headers or properties, we do need to create and send a type implementing `Message`, because we cannot directly set these in the object we are sending. In this case would use the setters methods such as `setText()`, for a `TextMessage`, or `setObject()`, for an `ObjectMessage`, to set the payload of the message before sending it.

## Point-to-Point messages (P2P)
P2P communication is used in the fallowing cases:
1. One to one communication with a request/response scenario. The request will be sent by the asker, through a queue, to the responder. The responder will respond through another queue, from which the response will read by the asker. Once read, the response will be deleted. The key is that the <u>response is needed only once</u>, so once it is read by the asker, no other application can read it.
2. Interoperability among application built and/or running in different platforms. Decoupling.
3. Throughout/Performance. An application can receive its inputs, or requests through a queue. When the load increases, we can either increase the number of threads, or instances, of the application listening the same queue, or we can increase the number of queues, each listened by a new copy of the application. 
4. Possibility of browsing, or looping through, the messages in the queue without consuming (deleting) them, as this cannot be done with PUB/SUB messaging. Class `QueueBrowser`.

## Asynchronous processing of messages. Listener and `onMessage()`
The operation of reading a message with a consumer using method `receive()` is a blocking operation. If there are no messages in the queue the consumer will simply block waiting for one, without doing any other job. This is **synchronous processing**.

The JMS api allows for **asynchronous processing** though, through interface `MessageListener`. This interface has a method `onMessage(Message m)`. A "consumer", or "listener", class implementing this interface, will override this method specifying the action to be taken when a message arrive to the _listened_ queue. This class needs to be registered with the JMS provider in the consumer side code, passing it to a consumer. For example, if `EligibilityCheckListener` is the consumer class we do this with:
```java
consumer.setMessageListener(new EligibilityCheckListener());
```

In this way, the JMS provider will invoke its method `onMessages()` whenever a message arrive to the listened queue, and will pass it that message as argument. 


















_________
# JNDI
### "Java Programming 24-hour Trainer", Yakov Fain
Application Servers can host a set of ready-to-use Java objects that enterprise applications can request and use. These objects will be pre-created and published in the server under some name, so the application can look up them, without needing to create them over and over again. It is like a registry of objects.

**Java Naming and Directory Interface (JNDI)** is about registering and finding objects in distributed applications (enterprise applications deployed in one or many application servers, or standalone applications communicating with application servers or JNDI servers of some application). It is an API (a standard) that can be used for binding and accessing objects located in any Java EE or specialized _naming server_ that implement this standard API. Various software vendors (eg. JMS vendors) offer specialized "directory assistance software" that implement the JNDI API.

Every Java EE application server comes with an administration console that allow you to manage (create?) objects in a _JNDI tree_. In the JNDI tree we publish and look up _administered objects_, which are objects configured by the server administrator. Examples of administered objects are database connection pool (Data Source) as well as connection factories and destinations (queues and topics) in JMS servers.

Notice that instead of objects (or resources), we can also publish in the JNDI tree <u>references</u> to them, or any information that allows retrieving them somehow.

## Naming and Directory service
A **_naming service_** enables you to add, change or delete names of objects that exist in some _naming hierarchy_, so other Java classes can look them up to find their location. For example, the directory of books in a library has the names of the physical locations of the books in the shelves, where we can go and get the books.

A naming service provides a unique name for each entry that is registered, or _bound to_, this service. Every naming service has one or more _contexts_. A context is like a point in a directory tree in which we have other child directories. There will be one, and only one, root context, or node, called _initial context_, like the root directory in a disk. 

All objects in the JNDI tree will have a name by which we look them up. We can therefore call the tree a _naming tree_ as well. A **_directory service_**, on the other hand, enables us to search the naming tree by object attributes, rather than by object name. For example, imagine an object that represents a computer connected to a network. The object may have the attributes domain name, IP address and listening port. If this object is registered in a directory service, we may look it up by its domain name (eg. amazon.com) and then obtain it's IP and port. DNS servers do exactly this.

Naming and directory services are said to be provided by naming and directory servers, respectively.

To allow client code to do look-ups in a JNDI, or naming, tree, there has to be a process that initially binds the objects to the naming tree. This can be handled via a server administration console, or from client code that binds names to a names, or directory, server, of some software that has one. 

Java EE servers bind such objects as EJB, Servlets, JMS Connection Factories, and JDBC database connection pools to their naming servers during startup. They may have some of these bindings already predefined.

All classes and interfaces that support JNDI are located in the package `javax.naming`.

## The InitialContext class
<u>The class `InitialContext` represents the root of a JNDI tree in a naming server</u>. Once a particular resource has been bound to this tree, there are two ways of getting a reference to it:

- If a program is deployed in a Java EE server, we can inject the JNDI resource into it by using the `@Resource` annotation. It is also possible to make a `lookup()` on the `InitialContext` object.
- If an external Java program needs a JNDI resource of an application server, it has to get a reference to an `InitialContext`  of that application server, and then invoke the method `lookup()`, passing as argument the name of the desired resource. This may be the case of a standalone messaging program that needs to get a reference to the messaging queues bound to the JNDI tree of an application server, or the JNDI tree of a JMS server.

Explicit instantiation of the `InitialContext` is needed only if we are planning to use `lookup()` as opposed to resource injection.

When a Java program runs inside an application server, instantiating and getting a reference to the initial context is simply done by the line:
```java
Context initialContext = new InitialContext();
```
If on the other hand our program is outside the application server and we want to **get access** to the JNDI tree of the later through its InitialContext class, we need to pass some `Properties` to the InitialContext constructor. The specific properties vary from vendor to vendor of naming or directory service. For example, for the naming service of a Wildfly application server we may need to specify the location of the server, the <u>names of the vendor-specific classes implementing `InitialContext`</u>, and the access credentials:
```java
final Properties env = new Propeties();

env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");

env.put(Context, PROVIDER_URL, "http-remoting://127.0.0.1:8080");

env.put(Context.SECURITY_PRINCIPAL, "Alex123");
env.put(Context.SECURITY_CREDENTIALS, "MySecretPwd");

Context initialContext = new InitialContext(env);
```
Similarly, if an external program needs to access the InitialContext object in a GlassFish server the code may look like this:
```java
final Properties env = new Properties();
env.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
env.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
env.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
env.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
env.setProperty("org.omg.CORBA.ORBInitialPort", "8080");
InitialContext initContext = new InitialContext(env);
```
We need to read the documentation that comes with our application server to get the proper code for accessing JNDI from an external program.

Notice that in some applications that need access to the JNDI tree of some vendor, it may be enough to define a properties file in place of the `env` variable shown above, such that we do not need to pass any argument to the constructor of `InitialContext`. In this file we must specify the vendor-specific Initial Context class. We may specify other resources as well. (**Am I doing bindings to the JNDI tree or just specifying names for the resources I want to look up later (right members)?**). For example, access to the JNDI tree of an ActiveMQ JMS server can be configured through the `jndi.properties` file:
```text
# initial context class
java.naming.factory.initial=org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory

# a ConnectionFactory resource
connectionFactory.ConnectionFactory=tcp://localhost:61616

# a queue resource
queue.queue/myQueue=myQueue
```
After receiving a reference to the InitialContext, we can invoke a lookup() method specifying the name of the required resource. Here is an example of
getting a reference to a message queue named test:
```java
Destination destination = (Destination) initContext.lookup("jms/queue/test");
```
And here is an example of getting a reference to an EJB and a default JDBC data source:
```java
MySessionBean msb = (MySessionBean) initContext.lookup("java:comp/env/ejb/mySessionBean");
DataSource ds = (DataSource) initContext.lookup("java:comp/env/jdbc/DefaultDataSource");
```

JNDI resources can be obtained by injection as well with `@Resource`.

As mentioned before, JNDI resources can also be references to other resources we create in the Java EE server. One example is a connections pool resource (type `javax.sql.DataSource`) in GlassFish. Here we first create the mentioned resource as a "JDBC Connection Pool" from the console, setting the necessary attributes Username, Password, ServerName, Port and DatabaseName. After, we create a JNDI resource, specifically a "JDBC Resource", with a given "JNDI name" and associate it with the just created pool of connections. This JNDI resource will be a reference to that pool of connections (object of type `DataSource`). From our code we can then access this resource which points to a pool of connections as:
```java
import javax.annotation.Resource;
import javax.sql.DataSource;

public class InfoResource {

  @Resource(lookup = "jdbc/utenti") // this is the JNDI name "associated" with, or 
                                // referencing a, pool of connections in the GlassFish console
  DataSource dataSource;

  // ...
  
}
```
See Xonya https://www.youtube.com/watch?v=NlifzWtN2cA min 7.
