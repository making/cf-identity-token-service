package lol.maki.cts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CfTokenServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfTokenServiceApplication.class, args);
	}

}
