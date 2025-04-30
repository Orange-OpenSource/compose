# Smart Contract as a Service (SCaaS)

Code coverage : [![Quality Gate Status](https://sqaas.dos.tech.orange/api/project_badges/measure?project=smartchain_compose_v3_backend_scaas_AYlOiSWzFhmrILXu8IGo&metric=alert_status&token=sqb_6e3f630d8c0bc68c34a1bd010888762bf2b5a8cd)](https://sqaas.dos.tech.orange/dashboard?id=smartchain_compose_v3_backend_scaas_AYlOiSWzFhmrILXu8IGo)

## Description

SCaaS is the backend instance of the Compose project. Compose is a software platform that enable to manage the lifecycle of Blockchain applications (smart-contracts). This includes designing and deploying these smart-contracts, as well to manage their interactions with the non-Blockchain IT systems. Compose might be deployed over various Blockchain networks (today, Ethereum or Alastria). Compose enables to design directly smart-contract and to deploy it, or to import existing smart-contracts. Once deployed, a REST API is automatically generated to ease interacting with it (input and output).

## Tools

| Tool          | Version                                                    | Description                                                                                                                                      |
|---------------|------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| SBT           | 1.10.2                                                     | The interactive build tool                                                                                                                       |
| JSE (OpenJDK) | 17                                                         | This release is the Reference Implementation of version 17 of the Java SE Platform, as specified by JSR 392 in the Java Community Process.       |
| MongoDB       | 5.0                                                        | MongoDB is an open source NoSQL database management program. NoSQL (Not only SQL) is used as an alternative to traditional relational databases. |

## Libraries

| Library              | Version           | Description                                                                                                                                                            |
|----------------------|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Web3J                | 4.12.2            | Web3j is a highly modular, reactive, type safe Java and Android library for working with Smart Contracts and integrating with clients (nodes) on the Ethereum network. |
| Scala                | 3.5.2             | Scala is a strong statically typed high-level general-purpose programming language that supports both object-oriented programming and functional programming.          |
| Play!                | 3.0.5             | Play is a high-productivity Java and Scala web application framework that integrates the components and APIs you need for modern web application development.          |
| Play 2 ReactiveMongo | 1.1.0-play30.RC13 | ReactiveMongo is a Scala driver that provides fully non-blocking and asynchronous I/O operations.                                                                      |
| Better-files         | 3.9.2             | better-files is a dependency-free pragmatic thin Scala wrapper around Java NIO.                                                                                        |


## Tests
In the tests directory there are tests written with the [ScalaTest](http://www.scalatest.org/) library.  
To launch them just type ```test``` in a running SBT session or simply type ```sbt test```
