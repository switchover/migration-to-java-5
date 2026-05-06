# Migration to Java 5

## Overview
This project is a migration of the codebase to Java 5.

## Build
```shell
mvn clean package
```
The build will produce a jar and Windows execution file in the target directory.

## How to use
```shell
java -jar target/migration-to-java-5.jar --help

 usage:  java -jar migration-to-java-5.jar

       Options                 Description       
 -t, --target <Dir.>     Target source directory 
 -v, --verbose           Verbose output          
 -V, --version           Show version information
 -h, --help              Display help information
```

Or run the executable file(```migration-to-java-5.exe```)
