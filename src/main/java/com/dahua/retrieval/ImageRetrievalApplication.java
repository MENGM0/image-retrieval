package com.dahua.retrieval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

//@SpringBootApplication
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class ImageRetrievalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageRetrievalApplication.class, args);
	}

}
