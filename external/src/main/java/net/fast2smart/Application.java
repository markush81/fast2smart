package net.fast2smart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by markus on 22/10/2016.
 */
@SpringBootApplication
@SuppressWarnings({"squid:S1118"})
public class Application {

    @SuppressWarnings({"squid:S2095"})
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
