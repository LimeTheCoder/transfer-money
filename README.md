# Money Transfer API

### Third-party libraries 
1. ActFramework - A simple web framework
2. H2 - SQL database
3. Guice - DI framework
4. Mockito, JUnit, Hamcrest - for testing purposes

### Implementation

Layered architecture was used with following components: controllers, services, repositories.
Transactions are handled on application level with locks (DB transactions could be used instead).

### How to 

#### run

    mvn clean compile exec:java
