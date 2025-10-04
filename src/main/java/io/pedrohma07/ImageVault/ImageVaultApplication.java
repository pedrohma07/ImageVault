package io.pedrohma07.ImageVault;

import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.TimeZone;

@EnableAsync
@SpringBootApplication(exclude = { S3AutoConfiguration.class })
@EnableTransactionManagement
@CrossOrigin(origins = "*")
public class ImageVaultApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

	public static void main(String[] args) {
		SpringApplication.run(ImageVaultApplication.class, args);
	}

}
