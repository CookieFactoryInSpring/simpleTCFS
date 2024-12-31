package fr.univcotedazur.simpletcfs.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:persistence.properties")
public class PersistenceJpaConfig {

    // SpringBoot (not Spring) will initialize JPA and DB connection with the following properties:
    // spring.datasource.username
    // spring.datasource.password
    // spring.datasource.url

    // Easy setup -> 2 persistence.properties file, one in src/main/resources, the other one in test/resources

}
