# arion-master
Arion Master: Regional GW Control Plane

## Introduction

The Arion Master is deployed and launched in each region, responsible for the Arion related vpc metadata's efficient propagation and consistency. It exposes grpc APIs to accept user vpc inputs and for each Arion Wing controller to get programming states from. 

![image](https://user-images.githubusercontent.com/83976250/176780305-13837c87-3ae7-498e-a939-fe57e9dadac9.png)

It provides expandability to adapt to different databases, just need to integrate the database write and read (or more advanced features like watch if exists) APIs with the grpc wrapper. So far, the database we are using is https://hazelcast.com/ 

## Benchmark
With the current design and implementation, Arion Master's performance is evaluated in https://github.com/futurewei-cloud/arion/blob/main/benchmark/Arion_Master_Performance.md 
