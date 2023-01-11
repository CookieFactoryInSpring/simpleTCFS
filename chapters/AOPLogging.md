# AOP, logging, and monitoring

  * Author: Philippe Collet

## Basic logging in Spring

SpringBoot also follows the "convention over configuration" principle for logging. From SpringBoot 2.x and 3.x a default logging framework and a default implementation are provided. SpringBoot uses SLF4J, the Simple Logging Facade for Java, and a default Logback implementation. Log4J2 is also available, but needs some specific setup in the pom.xml. In our implementation, we will stick with Logback.

As a basic example we will add a log of level INFO when entering the method that updates the cart:

```java
@Component
public class CartHandler implements CartModifier, CartProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CartHandler.class);

...

    public Item update(Long customerId, Item item) throws NegativeQuantityException, CustomerIdNotFoundException {
        Customer customer = customerFinder.retrieveCustomer(customerId);
        // some very basic logging (see the AOP way for a more powerful approach, in class ControllerLogger)
        LOG.info("TCFS:Cart-Component: Updating cart of {} with {}", customer.getName(), item);

...
}
```
It is easy:

* declare a static Logger attribute
* use it to log in the method

In SpringBoot, the default logging level of the Logger is preset to INFO, meaning that TRACE and DEBUG messages are not visible, but our message will be visible. In the next sections, we will see a finer configuration and above all, how to make all this way much smarter.

## Some tuning over the logging

### Configuration

Configuration of logs can be done by changing some properties, either by passing them as parameters to maven execution, or by adding them to the `application.properties` file (in the `resources`dir). For example :

    logging.level.root=OFF

would turn off all logs at all levels.

More configuration options are available in a configuration file (and XML file named `logback-spring.xml`). When found by SpringBoot, it is automatically used to configure the way logs are writtent. In TCF we use the following:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <property name="LOGS" value="./logs"/>

    <appender name="Console"
              class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <Pattern>
                %black(%d{ISO8601}) %highlight(%-5level) [%blue(%t)] %magenta(%C{1}): %msg%n%throwable
            </Pattern>
        </layout>
    </appender>

  <appender name="RollingFile"
              class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOGS}/tcfs-logger.log</file>
    <encoder
           class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
       <Pattern>%d %p %C{1} [%t] %m%n</Pattern>
     </encoder>

        <rollingPolicy
                class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <!-- rollover daily and when the file reaches 10 MegaBytes -->
            <fileNamePattern>${LOGS}/archived/tcfs-logger-%d{yyyy-MM-dd}.%i.log
            </fileNamePattern>
            <!-- each file should be at most 10MB, keep 10 days worth of history, but at most 50MB -->
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>10</maxHistory>
            <totalSizeCap>50MB</totalSizeCap>
        </rollingPolicy>
    </appender>

    <!-- LOG everything at INFO level -->
    <root level="info">
        <appender-ref ref="RollingFile"/>
        <appender-ref ref="Console"/>
    </root>

    <!-- LOG TCFS elements at TRACE level -->
    <logger name="fr.univcotedazur.simpletcfs" level="trace" additivity="false">
        <appender-ref ref="RollingFile"/>
        <appender-ref ref="Console"/>
    </logger>

</configuration>
```

There are several configuration elements in it:

   * Some changes on the pattern layout of each line of log (in the "appender" Console), an appender being a device where logs are written.
   * The definition of a new appender (the default is Console) to make rolling files inside a `logs` directory (i.e, files named automatically on a daily basis). As you're certainly going to shutdown frequently the server, you should only have one `tcfs-logger.log` file. 
   * The configuration of the two appenders and log within them, everything at INFO level and up being written, as well as the TRACE level for our `simpletcfs` packages.

### Testing configuration

On the testing side, the previous configuration will be reused by default, but we could want to tune it better, for example, to focus logs on the most important parts when running tests while reducing the overall log size.

First, we can get rid of the Spring banner and some logs from the test context (except errors) by creating a separate `application.properties` file in the `test/resources` directory:

```
bank.host.baseurl=http://localhost:9090
# Property to set the visible level on all logs (OFF will turn off all of them)
# logging.level.root=OFF

# springdoc url for swagger UI
springdoc.swagger-ui.path=/doc
```

Note that this new file overrides the one from the source code side, and if we do not define the bank URL, there will be an injection error in the Spring container setup as this property is injected as `@Value`. It also shows that we could have a different URL setup for testing if needed.

Then we can add a `logback-test.xml` file in the `test/resources` directory to configure LogBack:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <include resource="org/springframework/boot/logging/logback/base.xml" />
  <logger name="org.springframework" level="ERROR"/>
</configuration>
```

Here, we reuse the default configuration and we only restrict logs on the whole Spring framework to `ERROR`. This configuration will only be used during testing.

## A smarter logging strategy with AOP

The thing with the basic solution is that it is quite cumbersome. One has to put Logger declaration and statements everywhere. While there are other frameworks that reduces the burden (look at Lombok for example), the technical part of Logging is always the same kind of scenarios:

   * you want to be notified when the program enters a certain set of methods,
   * you want to be notified when the program exits the same set,
   * and you might want to know whether it exits normally or with an exception...

Let's be smart and let's use the Aspect-Oriented Programming (AOP) paradigm to do so. Actually the whole implementation of Spring containers intensively uses AOP implemented with proxies around components to do almost all technical stuff within the Spring framework. The next section gives some elements and pointers to understand the paradigm more deeply, if needed.

As an example, we want to log all public methods in all REST controllers so that we have some information on entry and exit (maybe alors when exiting with an exception). With AOP, it will be easy :

1. Include spring-boot-starter-aop in your pom.xml :

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```

2. Add `@EnableAspectJAutoProxy` to your configuration class (to tell SpringBoot that you active AspectJ support):

```java
@SpringBootApplication
@EnableAspectJAutoProxy
public class SimpleTcfsServer {
...
```

3. Add a pointcut that defines a pattern that is matched against method signatures as they run (see next section for details) + Add an aspect component that defines when you want to run your code in relation to the pointcut (e.g, before, after the matched methods):

```java
@Aspect
@Component
public class ControllerLogger {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerLogger.class);

    @Pointcut("execution(public * fr.univcotedazur.simpletcfs.controllers..*(..))")
    private void allControllerMethods() {
    } // This enables to attach the pointcut to a method name we can reuse below

    @Before("allControllerMethods()")
    public void logMethodNameAndParametersAtEntry(JoinPoint joinPoint) {
        LOG.info("TCFS:Rest-Controller: {}:Called {} {}", joinPoint.getThis(), joinPoint.getSignature().getName(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "allControllerMethods()", returning = "resultVal")
    public void logMethodReturningProperly(JoinPoint joinPoint, Object resultVal) {
        LOG.info("TCFS:Rest-Controller: {}:Returned {} with value {}", joinPoint.getThis(), joinPoint.getSignature().getName(), resultVal);
    }

    @AfterThrowing(pointcut = "allControllerMethods()", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Exception exception) {
        LOG.warn("TCFS:Rest-Controller: {}:Exception from {} with exception {}", joinPoint.getThis(), joinPoint.getSignature().getName(), exception.getMessage());
    }
```

In our case, the `@Pointcut` annotation defines, through the AspectJ syntax, the signature of methods that are going to be "intercepted". The the name of the annotated method (`allControllerMethods()`) will be used as a reference for three *advices* :

   * One before the method, which uses the `JoinPoint` object to log the caller object (the controller object) and all the parameter values being passed;
   * One after if the method exits correctly;
   * One after an exception throwing, which changes the log level to WARNING when logging.

As a result, all component implementations (`@Component` classes in the `controllers` package) have their public methods logged with only this setup!

## AOP Basics

[For the moment the best introduction is the one from Spring itself.](https://docs.spring.io/spring-framework/reference/core/aop.html)



