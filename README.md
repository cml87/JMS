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

The course is divided into 14 sections.

## Messaging Basics
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
A messaging server decouples the sender and receiver applications allowing for heterogeneous integration. Each application can be a service or a micro-service, developed in different programming languages and running on completely different environments. Moreover, they can be replaced at any time, as they will all fallow the same (abstract) contract set by the MOM. This **increases the flexibility** of our application, also good for microservices.

![image info](./pictures/heterogeneous_integration.png)

Before messaging came in, applications communication was made through a database or making remote procedural calls. This introduced tight coupling among applications ?. Messaging brought in the desired **loose coupling**, making applications not to need know anything about each other. All the request and response process is now mediated through the MOM. 

Compared to web services (HTTP request/response), MOMs are more reliable as request and response messages are persisted (in the queue), so there are much fewer chances they are lost.

Messaging also **reduce system bottlenecks** and **increase scalability**. If a queue only has one receiver application and there are much more messages in the queue than the app can process, we can introduce more instances of the same consumer application set to listen the same queue. In other words, we can spin off more instances of the consumer application as the load increases, and they will work asynchronously !

![image info](./pictures/system_bottleneck.png)


## What is JMS
JMS is a specification for messaging services in Java applications. It is maintained by Oracle. All Messaging Servers, irrespective of the vendor, must implement it. Developers use the APIs JMS provides. The current version of JMS is 2.0, and it is a big improvement over the earlier version 1.2.

**JMS is for messaging what JDBC is for databases**.

In this course we will use Apache ActiveMQ Artemis as JMS provider. It is a JMS client. Once installed we need to create a **broker ?** and run it. We'll need to create the <u>administered objects</u>:
- ConnectionFactory
- Queue (for P2P messaging)
- Topic (for PUB-SUB messaging)

The application will access the administered objects through **JNDI** (Java Naming and Directory Interface), from both the producer and the consumer side.

![image info](./pictures/jndi.png)

The JMS provider will give us durability, scalability, acknowledgment, transaction management, clustering and more.

## The two messaging models
JMS supports two types of messaging models: Point-to-Point and Publish/Subscriber.

### Point-to-Point
The Point-to-Point (P2P) messaging model allows sending and receiving messages both synchronously and asynchronously, through channels called **queues**. The JMS provider allows creating queues. There will be a Producer, or Sender, application adding messages to the queue. And there will be a Receiver, or Consumer, application taking the messages from the queue.

In Point-to-Point messaging the message that is put into the queue is consumed by only one application and then removed from the queue. The JMS provider will ensure this.
 
P2P messaging supports **asynchronous fire and forget**, which means that the producer application will send the message to the JMS provider and will forget it. The consumer application will then consume and process it however it wants. However, it also supports **synchronous request/replay messaging**. In this case, after the producer application sends a message to the queue, the consumer application receives it, process it, and sends a message back to the producer app. through a different queue. The producer will read this message as a response. We'll see this case later.

![image info](./pictures/point_to_point2.png)

### Publish/Subscribe
In the Publish/Subscribe (PUB-SUB) messaging model the messages are published to a virtual channel called **topic**. We will have only one producer, but many consumers called "Subscribers". The same message will be received my multiple subscribers (applications). 

In the PUB-SUB messaging model messages are automatically broadcasted to the consumers, without them having to request or pull the topic. In other words, it is a push model. So, after the producer sends the message to the topic, the JMS provider will ensure the message is sent to all the subscribers subscribed to that topic.

![image info](./pictures/publish_subscribe.png)


## Apache ActiveMQ installation
I installed Apache ActiveMQ 2.19 by downloading it from https://activemq.apache.org/components/artemis/download/ and unzipping it in /opt.

When working wiht JMS providers, I think a running JMS server is called a **broker**. Brokers are created in some directory. I decided to create mine `/opt/apache-artemis-2.19.0/brokers/`, which I also crated after install. The command to create a broker is `artemis create <broker_dir_ffn>`, for example:
```text
/opt/apache-artemis-2.19.0/bin/$ artemis create ../brokers/mybroker
```
In this case the name I chose for the broker is "mybroker" . I decided to create my brokers inside the directory `/opt/apache-artemis-2.19.0/brokers/`, but they can be created anywhere. When creating the broker, user and password properties will be asked to be set, as well as whether we want to allow anonymous access to the broker.

To start the broker, or JMS server, we go to the `bin` directory inside the created broker directory and run
```text
$ artemis run
```
sudo privileges may be needed depending on where we created the server. This command will create a set of predefined queues and topics on the fly. Startup logs will be printed out with all the  useful information about the started services, similar to when we start an application server such as Wildfly.

The file `mybroker/etc/broker.xml` will be a configuration file with lots of configurations, including queues and topics. We can edit this file directly, or the **jndi.properties** file of our project, to create queues. As we'll see later, it seems that if we ask for a queue in our code defined in the jndi.properties file but that doesn't exist in the server, Artemis can create it for us on the fly.

## Components of the JMS 1.x API
The 7 important components (classes) of the JMS 1.x API are:
1. Connection Factory
2. Destination: a queue in case of P2P messaging, or a topic, in case of PUB-SUB messaging.
3. Connection
4. Session
5. Message
6. Message Producer
7. Message Consumer
 
The **ConnectionFactory** and the **Destination** are provided by the JMS provider, which will create and put them in the JNDI registry. Once in the JNDI registry these resources can be retrieved from our code. From the ConnectionFactory we get a Connection. From the Connection we then get a Session. 

A Session is a unit of work. We can create any number of sessions using a single connection to the JMS provider (server?). From the Session we can create a Message and a MessageProducer to send the message. In the consumer part of the application we'll also use a Session to create a MessageConsumer to consume messages. We will have queue producers/consumers and topic producer/consumer.

So we have JNDI tree -> ConnectionFactory -> Session -> Message -> MessageProducer or Consumer.<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Session -> MessageProducer or Consumer.


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

The javax and Spring dependencies are not strictly needed, but I include them because I want to use Spring and annotations configuration.

ActiveMQ will read a properties file `jndi.properties` in the resources' directory (in the class path). In this file we will specify the `InitialContext` class, as well as some other properties that will be used to look up for resources in the JNDI tree of the JMS server.

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
it will automatically uses the information defined in the application.properties file. The object returned by `new InitialContext()` represents the root of the JNDI tree of the naming server the JMS server has. From there we can start to look up resources. I think we will get the `InitialContext` object from the factory class we have specified in property `java.naming.factory.initial`, ie. `org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory`. This factory class is specific to the JMS vendor we are using; it will be in the jar file `artemis-jms-client-all-2.6.4.jar` we included in the class path of the project with the maven dependency:
```xml
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>artemis-jms-client-all</artifactId>
            <version>2.6.4</version>
        </dependency>
```
In the queue name specification, the first "queue." indicates it is a queue type of administered object. There is no queue named "myQueue". It will be created dynamically at run time. However, I don't understand whether the JNDI name will be "queue/myQueue" (left member) or "myQueue" (right member). In the code we use "queue/myQueue" as argument to `lookup()` anyway, buh. I think that `queue/myQueue` will be a JNDI name, a reference to a JNDI resource. The resource will be then a queue named `myQueue`.

These are properties that the ArtemisMQ JMS broker host needs to setup a JNDI tree. I'm not sure whether these properties will just be that, properties to be loaded by our application, or will also create bindings and resources in the JMS server. But I think it defines bindings with the names and types we specify. For example, the line:
```text
connectionFactory.ConnectionFactory=tcp://localhost:61616
```
defines a resource of type "connectionFactory" with name "ConnectionFactory" and with value "tcp:://localhost:61616".

**---->** For the case of the queue, the teacher said it is created by the JMS provider the first time we `lookup()` for it in our code. May be the Connection Factory is also created the first time we lookup for it, buh.

Each call to `consumer.receive()` will consume a message from the queue and will delete it. Messages are consumed in order, in a first-in first-out fashion. We can call several times to `.receive()` to consume each message in the queue in order.

Notice that a call to `receive(<timeout_milis>)` is <u>blocking</u>. In other words, the program will stop until this method gets a message from the queue. If there are no messages in the queue, it will wait for one forever, I think, if we don't specify a timeout. In case no message has been retrieved after the timout from the queue (no messages have arrived while we were waiting), the method will return with `null`.  

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

Notice that consumers will only receive messages sent to the topic <u>after</u> they have subscribed to it.
Here is the example code
```java
public class FirstTopic {

    public static void main (String[] args) throws NamingException, JMSException {

        InitialContext initialContext = new InitialContext();
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
**Question**: When all consumers subscribed to the queue have received a message, is that message removed from the queue?

## Looping through the messages in Queue
We can loop through the messages in a queue <u>without consuming them</u> (without removing them from the queue). For this, we use another object that can be obtained from the `Session` and the destination, like when we obtain a `MessageProducer` or a `MessageConsumer`. It is called `QueueBrowser`. Method `getEnumeration()` invocated in a `QueueBrowser` object returns an object implementing the legacy interface `Enumeration`, which is similar to the modern interface `Iterator`:
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
JMS 2.0 makes it much easier to send and receive messages. It shortens the steps we saw before. JMS 2.0 provides a new class that is the combination of a Connection and a Session, `JMSContext`, from which we create producer and consumer objects `JMSProducer` and a `JMSConsumer`. All `JMSContext`, `JMSProducer` and `JMSConsumer` implement `java.lang.AutoClosable`, so we will not need to close them explicitly, provided we use them inside a try/catch block. Moreover,`JMSProducer` and `JMSConsumer` give us easy access to a message's Headers, Properties and Body. 

JMS 2.0 is the JMS version implemented in in Java EE 7 and 8. With this version, compatible application servers allow injecting the Connection Factory and destination resources easily into our code as:
```java
@Inject
@JMSConnectionFactory("jms/connectionFactory") private JMSContext context;

@Resource(lookup = "jms/dataQueue")
private Queue dataQueue
```

With JMS 2.0 we'll create a connection factory object, `ActiveMQConnectionFactory`, and from it we'll instantiate directly a context object `JMSContext`, instead of a session. The way we'll use this context object to send and receive messages to queues will be similar to what we saw before with the session object. One difference is that now only the consumer will be instantiated specifying also the queue to be read. The producer will not need the target queue at initialization time, it will be specified later when we call `send(<queue_name>)` with it.

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
Notice that different to JMS 1.1, producer `JMSProducer` is created from the context without specifying the destination to which we want to send messages later with it. The destination is specified when we call `send()` on the producer:
```java
JMSProducer producer = jmsContext.createProducer();
producer.send(queue, "Arise awake and stop not till the goal is reached");
```
On the other hand, a consumer `JMSConsumer` does need the destination from which we'll consume messages with it after:
```java
JMSConsumer consumer = jmsContext.createConsumer(queue);
String messageReceived = consumer.receiveBody(String.class);
```

## JMS message

Messages communicate all the data and events possible in the JMS specification. A message is divided into three parts: **_Header_**, **_Properties_** and **_Body_** (or Payload).
The Body can be of different types: byte message, text message, object message etc.

### Message headers

The message headers are metadata. There are provider-set headers and developer-set headers. 

**Provider-set headers** are automatically assigned by the provider to a message when it is sent. These may include:
- JMSDestination: queue or topic to which the message should be delivered.
- JMSDeliveryMode: eg. persistence or non- persistence message etc.
- JMSMessageId: message unique id assigned by the provider, so the consumer can identify a particular message.
- JMSTimeStamp: timestamp at which the message was received by the JMS provider.
- JMSExpiration: time at which, if reached, the message will be expired?.
- JMSRedelivered: set by the provider when it re-delivers the message to a particular consumer, because that message was not delivered in its prior try ?
- JMSPriority: an integer ranged 0-9 meaning message priority. 0-4 is "normal priority", 5-9 is "high priority". 

_Can I_ send a message invoking `send()` with no destination argument, if the messaga already has the destination queue specified in header `JMSDestination`?

**Developer-set headers** include:
- JMSReplayTo: The producer application will set this header so that the consumer application know which destination it should replay back on, in a request/replay scenario.
- JMSCorrelationID: also used in a request/replay scenario. The consumer application will set it with the JMSMessageId of the request message for which it is sending the response back. This way the producer application can relate the incoming response with the particular request it previously sent. So it is to _correlate_ a request with its response.
- JMSType: set by the producer application. Used to convey what type of message is being sent

Any Developer-set header can be set in the message itself, before sending it. It can also be set in the producer, for all messages sent with it. For example:
```java
JMSProducer producer = jmsContext.createProducer();
//producer.setJMSReplyTo(replyQueue);
TextMessage message = jmsContext.createTextMessage("Arise awake and stop not till the goal is reached");
message.setJMSReplyTo(replyQueue);
producer.send(requestQueue, message);
```

_Can I_ invent new headers?

### Message properties
Properties are also divided into provider-set properties and developer-set properties. At the producer side, **developer-set** (or **application specific**) properties can be added to the message as key-value pair, with method `setXXXProperty`. XXX stands for a particular type, such as integer, boolean, string etc (primitives ?). At the consumer end we can then retrieve any property with `getXXXPropety`. 

**Provider-set** properties include JMSXUserID, JMSXAppID, JMSXProducerTXID, JMSXConsumerTXID, JMSXRcvTimeStamp, JMSDeliveryCount, JMSXState, JMSXGroupID and JMSXGroupSeq. It is not mandatory for the provider to support all of them. We don't usually touch the provider-set properties.

The properties JMSXGroupID and JMSXGroupSeq are used when we work with groups of messages, ie. instead of sending one message we can send a group of messages and process them.

Messages can be filtered by both its headers and its properties.

## Message priority
The priority of the messages (provider-set header) present in a queue affect the order in which they are received when we call `receive()` in a consumer. Messages with higher priority will be received first. Instead of setting the priority to the message itself, we set the priority to the producer with which we'll send it. Once a producer has been set with a priority, all messages sent with it will have that priority in the queue. Here is an example:
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
A producer application can send a message to queue, a _request_, from which the message is consumed after by a consumer application. The consumer application can then consume and process this message, sending another one to another queue, which can be consumed by the producer, as a _replay_. Here is an example:
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

### Temporary queue
The queue where a producer wants to receive a message back as reply, can be a `TemporaryQueue`. This is a queue directly created from a context, instead of being retrieved from the JNDI tree. "A `TemporaryQueue` object is a unique Queue object created for the duration of a Connection. It is a system-defined queue that can be consumed only by the Connection that created it." ??

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
In my experiments the correlationId of the consumed reply, in the producer side, will result equal to the correlationId we set in the reply message, in the consumer side, <u>only if</u> the replay queue is a `TemporaryQueue`, I don't know why ---> False. It was due to the fact that in the reply queue gotten from the JNDI tree, there were messages already, before I was adding my new reply to the queue. On the other hand, the temporary queue was always empty.

Notice also how we can add sent messages in the producer side to a Map, using as key the messageID, and then retrieve them using as key the correlationId of the reply message. This is how we set the request-reply linkage.

## Message expiration
We can set an expiry time, or time-to-live, to a message with `setTimeToLive`. It is a provider-set header, like the priority.  If a message is not consumed before it is expired, it will be <u>moved to an "expiry queue" automatically</u>, disappearing from the queue we send it to. We set the timeToLive in the producer with which we'll send the message, not on the message itself.

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
Other than headers time-to-live and priority, in the producer we can set a delivery delay period of time header, so that messages sent with it will actually be sent after such time, when we call `send()`. Here is the example:
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
Custom message properties can be set in the message or in the producer. Properties can then be retrieved in the consumer side, from the message. We can set a property of any primitive type, or even of Object type, which determines which setter and getter method we use for the property. When we set a property, we must specify a name for it, other than a value (proper type). For example:
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

           TextMessage messageReceived = (TextMessage) jmsContext.createConsumer(queue).receive();
           System.out.println(messageReceived);
           System.out.println(messageReceived.getBooleanProperty("loggedIn"));
           System.out.println(messageReceived.getStringProperty("userToken"));

       }
   }
}
```

## Types of messages
Among the types of messages we can send with JMS, there are 5 implementing the <u>interface</u> `javax.jms.Message`. <u>These are also interfaces</u> belonging to package `javax.jms`:
1. TextMessage: Used to send text data as Strings
2. ByteMessage: Bytes, binary level 
3. ObjectMessage: send an object that can be (de)serialized.
4. StreamMessage: Stream Java objects and wrap primitive types? The consumer must read the stream in the same order it was written by the producer ?
5. MapMessage: A set of key-value pairs

JMS providers, such as Apache ActiveMQ, implement these interfaces in concrete classes. For example, interface `ObjectMessage` is implemented by `ActiveMQObjectMessage`.

It's not clear to me when to use which one. The way in which we set and fetch the payload for each of these message is different (read and write methods to be used).

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
When and object implements interface `Serializable`, we can send it through an `ObjectMessage`. We will create an `ObjectMessage` and `setObject()` and `getObject()` the object it carries when sending and receiving the message, respectively. For example:
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
However, <u>if we want to set headers or properties, we do need to create and send a type implementing `Message`</u>, because we cannot directly set these in the object we are sending. In this case would use the setters methods such as `setText()`, for a `TextMessage`, or `setObject()`, for an `ObjectMessage`, to set the payload of the message before sending it.

## Point-to-Point messages (P2P)
P2P communication is used in the fallowing cases:
1. One to one communication with a request/response scenario. The request will be sent by the asker, through a queue, to the responder. The responder will respond through another queue, from which the response will be read by the asker. Once read, the response will be deleted. The key is that the <u>response is needed only once</u>, so once it is read by the asker, no other application can read it.
2. Interoperability among application built and/or running in different platforms. Decoupling.
3. Throughout/Performance. An application can receive its inputs, or requests through a queue. When the load increases, we can either increase the number of threads, or instances, of the application listening the same queue, or we can increase the number of queues, each listened by a new copy of the application. 
4. Possibility of browsing, or looping through, the messages in the queue without consuming (deleting) them, as this cannot be done with PUB/SUB messaging. Class `QueueBrowser`.

## Asynchronous processing of messages. Listener and `onMessage()`
The operation of reading a message with a consumer using method `receive()` is a blocking operation. If there are no messages in the queue, the consumer will simply block waiting for one, without doing any other job. This is **synchronous processing**.

The JMS api allows for **asynchronous processing** though, through interface `MessageListener`. This interface has a method `onMessage(Message m)`. A "consumer", or "listener", class implementing this interface, will override this method specifying the action to be taken when a message arrive to the _listened_ queue. This class needs to be "registered" with the JMS provider in the consumer side code, passing it to a consumer. For example, if `EligibilityCheckListener` is the consumer class, we register it with consumer `consumer` with:
```java
consumer.setMessageListener(new EligibilityCheckListener());
```
In this way, the JMS provider will invoke the listener method `EligibilityChechkListener.onMessages()` whenever a message arrive to the queue to which consumer `consumer` is attached. This method will receive in its parameter the message arrived to the queue. Remember, every consumer must specify a queue to which attach when it is declared. 

In the example below the producer application is `ClinicalsApp`, and the consumer application is `EligibilityCheckerApp`. Both these classes have a `main()` method, that's why they are separate "applications". The producer application will send a request (a message) through a `requestQueue`, from where the consumer application will read it, by means of a listener class `EligibilityCheckListener`. After processing the request in method `EligibilityCheckListener.onMessage()`, the consumer application will generate a response (another message) and will send it to a `replyQueue`. The later will then be read by the consumer application to fetch the response, making the application end. The first part of the communication (producer -> consumer) is asynchronous, since the producer application will send the request to a queue that is "listened by" the consumer application. The second part of the communication (consumer -> producer) is synchronous though, since the producer will read the response message from the `replyQueue` with method `receive()`.

```java
// producer application
public class ClinicalsApp {

    public static void main(String[] args) throws NamingException, JMSException {

        InitialContext initialContext = new InitialContext();
        Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
        Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()){

            Patient patient = new Patient(123, "Bob");
            patient.setInsuranceProvider("Blue Cross Blue Shield");
            patient.setCopay(100d);
            patient.setAmountToBePayed(500d);

            JMSProducer producer = jmsContext.createProducer();
            ObjectMessage objectMessage = jmsContext.createObjectMessage();
            objectMessage.setObject(patient);

            producer.send(requestQueue, patient);

            JMSConsumer consumer = jmsContext.createConsumer(replyQueue);
            // here we block
            MapMessage replyMessage = (MapMessage) consumer.receive(30000);

            System.out.println("patient eligibility is: "+ replyMessage.getBoolean("eligible"));

        }
    }
}
```
```java
// consumer application. It will simply set a listener on a queue
public class EligibilityCheckerApp {

  public static void main(String[] args) throws NamingException, JMSException, InterruptedException {

    System.out.println("Listener app started ...");

    InitialContext initialContext = new InitialContext();
    Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

    try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
         JMSContext jmsContext = cf.createContext()){

      JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
      consumer.setMessageListener(new EligibilityCheckListener());

      // This is the time the consumer will be up
      // If the application is deployed in an application server, this command is not needed  
      Thread.sleep(10000);
    
    }
    System.out.println("Listener app finished");
  }
}
```
```java
// listener class of the consumer application
public class EligibilityCheckListener implements MessageListener {

    @Override
    public void onMessage(Message message) {

        // we know that the listened queue will have an ObjectMessage, so we can apply this casting here
        ObjectMessage objectMessage = (ObjectMessage) message;

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            InitialContext initialContext = new InitialContext();
            Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

            // We'll reply to this queue
            MapMessage replyMessage = jmsContext.createMapMessage();

            // Business logic. Processing of the incoming message
            Patient patient = (Patient) objectMessage.getObject();
            System.out.println("received Patient: " + patient.toString());
            String insuranceProvider = patient.getInsuranceProvider();
            if (insuranceProvider.equals("Blue Cross Blue Shield") || insuranceProvider.equals("United Health")){
                System.out.println("Patient copay is: "+ patient.getCopay());
                System.out.println("Patient amount to be paid: "+ patient.getAmountToBePayed());
                if (patient.getCopay()<40 && patient.getAmountToBePayed()<1000){
                    System.out.println("a");
                    replyMessage.setBoolean("eligible", true);
                } else {
                    replyMessage.setBoolean("eligible", false);
                }
            } else {
                replyMessage.setBoolean("eligible", false);
            }

            // Replay creation
            JMSProducer producer = jmsContext.createProducer();
            producer.send(replyQueue, replyMessage);

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }
}
```
Notice that we need to keep the consumer application up for enough time for the producer application to produce and send a message to it. That's why the consumer applications has the command `Thread.sleep(10000);`. To test this example we first need to start the application setting the listening of the `requestQueue`, ie. the consumer application `EligibilityCheckerApp`, and then the producer application. The producer application `ClinicalsApp` will print out:
```text
patient eligibility is: false
```
whereas the consumer application `EligibilityCheckerApp` will print out:
```text
Listener app started ...
received Patient: Patient{id=123, name='Bob', insuranceProvider='Blue Cross Blue Shield', copay=100.0, amountToBePayed=500.0}
Patient copay is: 100.0
Patient amount to be paid: 500.0
Listener app finished
```

## Load balancing
A simple way for obtaining load balancing when using JMS is to attach several listeners to the same "busy" queue, and run each of these listeners in _different threads_, either in the same application instance or not. The JMS provider will readily manage different threads reading messages from the same queue, and will provide to remove them from the queue whenever any thread invokes the method `receive()`. In the example below, we  _simulate_ this type of load balancing. It wouldn't be a simulation if the two consumer were running in different threads, but they are.
```java
public class EligibilityCheckerApp {

    public static void main(String[] args) throws NamingException, JMSException, InterruptedException {

        System.out.println("Listener app started ...");

        InitialContext initialContext = new InitialContext();
        Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            // we set two consumers attached to the same queue
            JMSConsumer consumer1 = jmsContext.createConsumer(requestQueue);
            JMSConsumer consumer2 = jmsContext.createConsumer(requestQueue);

            // We alternate the two consumers in consuming the messages in the same queue
            for (int i = 1; i <= 10; i+=2) {
                System.out.println("Consumer1: " + consumer1.receive());
                System.out.println("Consumer2: " + consumer2.receive());
            }
        }
        System.out.println("Listener app finished");
    }
}
```
```java
public class ClinicalsApp {

    public static void main(String[] args) throws NamingException, JMSException {

        InitialContext initialContext = new InitialContext();
        Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
        Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()) {

            Patient patient = new Patient(123, "Bob");
            patient.setInsuranceProvider("Blue Cross Blue Shield");
            patient.setCopay(100d);
            patient.setAmountToBePayed(500d);

            JMSProducer producer = jmsContext.createProducer();
            ObjectMessage objectMessage = jmsContext.createObjectMessage();
            objectMessage.setObject(patient);

            // send 10 messages to the queue
            for (int i = 1; i <= 10; i++)
                producer.send(requestQueue, patient);

        }
    }
}
```

## <i>Assignment</i>: CheckIn App

We'll be building a CheckIn App for the assignment at the end of section 6 of the course. The app will be composed in turn of two apps: a client side code app called `CheckInApp` and a server side code app called `ReservationSystem`. The communication between the two apps will be asynchronous, in a request/replay scenario using JMS.

The client code will send requests to the server code through a queue `requestQueue`. The reply to the client code will be sent back through a queue `replyQueue`. The request payload will be a pojo `Person` with fields id, firstName, lastName, birthDay, phone and email. The replay will be a MapMessage containing the boolean property `isReservationDone`. Reason for a failed reservation will be a failed validation of the `Person` pojo, or a ran out of available reservations slots (flights, rooms in a hotel, seats in a cinema etc.). Many validations could be implemented on the `Person` pojo, but for the sake of simplicity only one will be used on the person's age. The birthday field will be used to check for an age over 18 in order to successfully do a reservation.

We'll use a P2P messaging model. The messages will make use of the `replyTo` and `correlationId` to establish the request-response relationship. Load balancing will also be illustrated through several consumer, or listeners, attached to the same request queue.

Here is how I did this assignment. Sout lines has been commented to not clutter the output when running three listeners and one producer (to illustrate load balancing):
```java
// producer application. It sends messages to the requestQueue and expects the reply in the replyQueue
public class CheckingApp {
//    public Person(int id, String firstName, String lastName, LocalDate birthDay, String phone, String email) {

  public static void main(String[] args) throws NamingException, JMSException {

    InitialContext initialContext = new InitialContext();
    Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
    Queue replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

    try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
         JMSContext jmsContext = cf.createContext()) {

      Person person = new Person(123, "John", "Smith",
              LocalDate.parse("2003-11-24"), "+01 7896787788", "pepe@gmail.com");

      List<Person> personList = new ArrayList<>();
      for (int i = 0 ; i < 100; i++){
        personList.add(new Person(i, "John", "Smith",
                LocalDate.parse("2003-11-24"), "+01 7896787788", "pepe@gmail.com"));
      }


      JMSProducer producer = jmsContext.createProducer();
      producer.setJMSReplyTo(replyQueue);

      ObjectMessage message = jmsContext.createObjectMessage();

      //message.setJMSReplyTo(replyQueue);
      for (int i = 0 ; i < 100; i++) {
        message.setObject(personList.get(i));
        producer.send(requestQueue, message);
        System.out.printf("MessageId sent by producer: [%s]. PersonId[%s]:\n", message.getJMSMessageID(),
                ((Person)message.getObject()).getId());
      }

      System.out.println("\n\nWaiting 20 seconds for responses to arrive to replyQueue ...");
      Thread.sleep(20000);

      //Map<String, ObjectMessage> messages = new HashMap<>();
      //messages.put(message.getJMSMessageID(), message);

      JMSConsumer consumer = jmsContext.createConsumer(replyQueue);
      for (int i = 0 ; i < 100; i++) {
        MapMessage reply = (MapMessage) consumer.receive();
        System.out.printf("CorrelationId received in reply: [%s]\n", reply.getJMSCorrelationID());
      }
      //System.out.println("reply message: isReservationDone: "+ reply.getBoolean("isReservationDone"));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
```

```java
// listener application (it has a main() method)
public class ReservationSystemApp {
  public static void main (String[] args) throws NamingException, InterruptedException {
    System.out.println("Listener application started ...");

    InitialContext initialContext = new InitialContext();
    Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

    //ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

    ReservationSystemListener reservationSystemListener = applicationContext.getBean("reservationSystemListener",
            ReservationSystemListener.class);

    try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
         JMSContext jmsContext = cf.createContext()){

      JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
      consumer.setMessageListener(reservationSystemListener);
      Thread.sleep(15000);
    }
    System.out.println("Listener application ended");
  }
}
```
```java
// listener class of the consumer application
@Component
@PropertySource(value = "classpath:/application.properties")
public class ReservationSystemListener implements MessageListener {

    @Value("${minimumAgeYears}")
    private int minimumAgeYears;

    public void setMinimumAgeYears(int minimumAgeYears) {
        this.minimumAgeYears = minimumAgeYears;
    }

     @Override
    public void onMessage(Message message) {

        //System.out.println("listener method in ...");

        ObjectMessage objectMessage = (ObjectMessage)message;

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()){

            Queue replyQueue = (Queue) objectMessage.getJMSReplyTo();

            MapMessage mapMessage = jmsContext.createMapMessage();
            //mapMessage.setJMSCorrelationID(objectMessage.getJMSMessageID());

            // request processing
            Person person = (Person) objectMessage.getObject();
            mapMessage.setJMSCorrelationID(String.valueOf(person.getId()));
            //System.out.println("received person's birthday: "+ person.getBirthDay().toString());
            System.out.printf("processing Person of Id: [%s]\n", person.getId());

            LocalDate now = LocalDate.now();
            //System.out.println("Today is: "+ now.toString());
            int personAge = Period.between(person.getBirthDay(), now).getYears();
            //System.out.println("Person's age is: "+ personAge);
            if (personAge>= minimumAgeYears){
                //System.out.println("Person's age is above the minimum "+minimumAgeYears);
                mapMessage.setBoolean("isReservationDone", true);
            } else {
                //System.out.println("Person's age is below the minimum "+minimumAgeYears);
                mapMessage.setBoolean("isReservationDone", false);
            }

            JMSProducer producer = jmsContext.createProducer();
            producer.send(replyQueue, mapMessage);

            //System.out.println("listener method out");

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
```
```java
// Spring beans config class
//@Configuration
@ComponentScan("com.example.jms.p2p.checkingapp")
public class AppConfig {
}
```
```text
// resources/applications.propeties
minimumAgeYears=23
```
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

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>2.4</version>
                <configuration>
                    <archive>
                    <!--I will specify the main calls to be ran when running the jar-->
<!--                        <manifest>-->
<!--                            <mainClass>com.yourcompany.youapp.Main</mainClass>-->
<!--                        </manifest>-->
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-jar-with-dependencies</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
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
In the pom file notice the plugin maven-assembly-plugin to build a jar (`mvn package`) with all Spring dependencies included, so we can run the application as a standalone a application without passing any dependencies in the classpath, as shown below. 

To test my application with load balancing:
1. I start three instances of the consumer application, all listening to the same queue, requestQueue, and replying also to the same queue, replyQueue. Do this by running in three separate shells the program:
 `java -cp  /home/camilo/my_java_projects/JMS/target/jmsfundamentals-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.example.jms.p2p.checkingapp.ReservationSystemApp`
2. There will be only one instance of the producer application adding messages to the requestQueue:
 `java -cp  /home/camilo/my_java_projects/JMS/target/jmsfundamentals-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.example.jms.p2p.checkingapp.CheckingApp`

After I start the three consumers, or listeners, I start the producer application. It will quickly add 100 messages to the requestQueue, all of which will 
be consumed with load balance by the three consumers!

## PUB-SUB messaging
In the PUB-SUB messaging model the destination is called a _topic_. _Subscribers_ subscribe to this topic ahead of time (before messages start arriving to the topic). When a message arrive to the topic, the JMS provider will ensure it is broadcasted to _all_ subscribed subscribers to the topic and up-and-running at that moment. After it will delete the message from the topic. Different to the P2P model, now a same message will be received by different applications. The PUB-SUB model is used when an application needs to communicate a same event to several other applications.

The PUB-SUB model supports the so called **durable** subscription and the **shared** subscription, as we'll see below.
 
A simple example of application using the PUB-SUB model is shown below. There will be three identical subscribers `PayrollApp`, `SecurityApp` and `WellnessApp`, all subscribed to the topic `topic/empTopic`. This topic will be defined in the `jndi.properties` file with the line `topic.topic/empTopic=empTopicl`. The producer application will be `HrApp`, which will send a single message to the topic.
```java
// producer application
// prints: "message sent"
public class HrApp {

    public static void main(String[] args) throws NamingException {

        InitialContext context = new InitialContext();
        Topic topic = (Topic) context.lookup("topic/empTopic");

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
             JMSContext jmsContext = cf.createContext()){

            Employee employee = new Employee(1,"Paul", "White","pepe@gmail.com","developer","0122234344");
            JMSProducer producer = jmsContext.createProducer();
            // we send the message directly
            producer.send(topic, employee);
            System.out.println("message sent");
        }
    }
}
```
```java
// prints: In PayrollApp ...
//         Paul
public class PayrollApp {
    public static void main(String[] args) throws NamingException, JMSException {
        System.out.println("In PayrollApp ...");
       
        InitialContext context = new InitialContext();
        Topic topic = (Topic) context.lookup("topic/empTopic");
       
        try(ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
            JMSContext jmsContext = cf.createContext()){
       
            JMSConsumer consumer = jmsContext.createConsumer(topic);
            Message message = consumer.receive();
       
            Employee employee = message.getBody(Employee.class);
       
            System.out.println(employee.getFirstName());
        }
    }
}
```
```java
// identical for SecurityApp and WellnesApp
```
### Durable subscription ?
In the PUB-SUB model, a JMS provider will broadcast a message that has arrived to a topic to all subscribers the topic has at that moment, and that are able to receive the message at that moment too (are running). After, the provider will delete the message. However, there may be an application, or a subscriber, that really needs to receive the messages arrived to the topic, even if when these arrived, it was not up-and-running. In a normal scenario, this application will simply miss the message. To solve this issue, JMS providers allow for **durable** subscribers.

Durable subscribers are subscribers "known" to the topic, for which the JMS provider will keep every message in the topic until it is assured the subscribed received it. After it is deleted. This way, a subscriber that happens to be down when a message arrive to the topic, can receive it as soon as it is up again. Only then the JMS provider will delete the message from the topic.

Normally we create simple consumers with 
```java
JMSConsumer consumer = jmsContext.createConsumer(topic);
```
To make a topic "aware" of a consumer, we create instead a "durable consumer" attached to this topic with
```java
jmsContext.setClientID("SecurityApp");
JMSConsumer consumer = jmsContext.createDurableConsumer(topic, "SecurityApp");
```
Notice how a durable consumer needs to use an ID already set in the jmsContext ?. This is how we implement a durable subscriber:
```java
public class SecurityApp {
    public static void main(String[] args) throws NamingException, JMSException, InterruptedException {

        System.out.println("In SecurityApp ...");

        InitialContext context = new InitialContext();
        Topic topic = (Topic) context.lookup("topic/empTopic");

        try(ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
            JMSContext jmsContext = cf.createContext()){

            // "register" a durable subscriber in the JMS context with id "SecurityApp"
            jmsContext.setClientID("SecurityApp");

            //JMSConsumer consumer = jmsContext.createConsumer(topic);
            // get the durable subscriber with id "SecurityApp" defined above
            JMSConsumer consumer = jmsContext.createDurableConsumer(topic, "SecurityApp");

            // close the subscriber ? to simulate the application is down 
            consumer.close();
            Thread.sleep(10000);

            // open the subscriber again
            consumer = jmsContext.createDurableConsumer(topic, "SecurityApp");

            // receive the messages that are in the queue
            Message message = consumer.receive();
            Employee employee = message.getBody(Employee.class);

            System.out.println(employee.getFirstName());

            consumer.close();
            // unregister subscriber with id "SecurityApp"
            jmsContext.unsubscribe("SecurityApp");

        }
    }
}
```

### Shared consumer

There are also persistence massages, where messages are stored to a db or a file system.
ClientId, SubscriptionName





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
