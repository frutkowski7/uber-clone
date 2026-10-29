package com.uber_clone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class UberCloneApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		// System.setProperty to sposób mockowania zmiennych środowiskowych
		// zapisanych w pamięci operacyjnej JVM, a nie lokalnie. (Żyją tyle ile
		// aplikacja)
		dotenv.entries().forEach((entry) -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(UberCloneApplication.class, args);

	}

}
