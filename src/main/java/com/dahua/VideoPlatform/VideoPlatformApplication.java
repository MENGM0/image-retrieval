package com.dahua.VideoPlatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

//@SpringBootApplication
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class VideoPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoPlatformApplication.class, args);
	}

}
