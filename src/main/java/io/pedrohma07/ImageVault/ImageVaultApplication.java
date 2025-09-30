package io.pedrohma07.ImageVault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ImageVaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageVaultApplication.class, args);
	}

}
