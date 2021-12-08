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

The Messaging Server provides useful services such as fault tolerance, load balancing, scalability, transaction management etc. Example of MOMs (JMS providers) are:
- Apache ActiveMQ
- SonicMQ
- IBM WebsphereMQ
- TibcoMQ
- RabbitMQ

![image info](./pictures/messaging_system.png)

