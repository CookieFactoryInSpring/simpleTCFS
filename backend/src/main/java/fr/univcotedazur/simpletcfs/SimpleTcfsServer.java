package fr.univcotedazur.simpletcfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class SimpleTcfsServer {

    public static void main(String[] args) {
        SpringApplication.run(SimpleTcfsServer.class, args);
    }

}
