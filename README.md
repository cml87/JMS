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
 
P2P messaging supports **asynchronous fire and forget**, which means that the producer application will send the message to the JMS provider and will forget it. The consumer application will then consume and process it however it wants. However, it also supports **synchronous request/replay messaging**. In this case, after the producer applications send a message to the queue, the consumer application receives it, process it, and send a message back to the producer app. through a different queue. The producer will read this message as a response.

![image info](./pictures/point_to_point2.png)

### Publish/Subscribe
In the Publish/Subscribe (PUB-SUB) messaging model the messages are published to a virtual channel called **topic**. We will have only one producer, but many consumers called "Subscribers". The same message will be received my multiple subscribers (applications). 

In the PUB-SUB messaging model messages are automatically broadcasted the consumers, without them having to request or pull the topic. In other words, it is a push model. So, after the producer sends the message to the topic, the JMS provider will ensure that that message is sent to all the subscribers subscribed to that topic.

![image info](./pictures/publish_subscribe.png)


## Apache ActiveMQ installation
I installed Apache ActiveMQ 2.19 by downloading it from https://activemq.apache.org/components/artemis/download/ and unzipping it in /opt.

Once "installed" I need to go to `/opt/apache-artemis-2.19.0/bin/` and run 
```text
$ ./bin/artemis create brockers/mybroker
```
to _create_ a JMS broker, or server. I decided to create my brokers inside `/opt/apache-artemis-2.19.0/brokers/`, but they can be created anywhere. The broker name I chose is `mybroker`. Now go to the `bin` directory inside the created broker directory and run
```text
$ artemis run
```
sudo privileges may be needed depending on where we created the server. This command will create a set of predefined queues and topics on the fly. Startup logs will be print out with all the  useful information about the started services, similar to when we start an application server such as Wildfly.

The file `mybroker/etc/broker.xml` will be a configuration file with lots of configuration, including queues and topics. We can edit this file directly, or in the jndi.properties file of our project, to create queues.

## Components of the JMS 1.x API
The 7 important components (classes) of the JMS 1.x API are:
1. Connection Factory
2. Destination: a queue in case of P2P messaging, or a topic, in case of PUB-SUB messaging.
3. Connection
4. Session
5. Message
6. Message Producer
7. Message Consumer
 
The ConnectionFactory and the Destination are provided by the JMS provider, which will create and put them in the JNDI registry ?, from where we can retrieve them. From the ConnectionFactory we get a Connection. From the Connection we then get a Session.

A Session is a unit of work. We can create any number of session using a single connection to the JMS provider (server?). From the Session we can create a Message and a MessageProducer to send the message. In the consumer part of the application we'll also use a Session to create a MessageConsumer to consume the message. We will have queue producers/consumers and topic producer/consumer.

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

The javax and Spring dependencies are not strictly needed, but I include them because I want to use Spring and annotations configuration. ActiveMQ will read a properties file `jndi.properties` in the resources directory.