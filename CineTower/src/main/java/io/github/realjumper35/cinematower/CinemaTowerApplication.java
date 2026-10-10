package io.github.realjumper35.cinematower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Spring cherche les composants à partir de ce package : tout le code de
// l'application vit dessous (domaine, source, persistance…).
@SpringBootApplication
public class CinemaTowerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinemaTowerApplication.class, args);
    }
}
