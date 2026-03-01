package com.dkgeneric.filenet.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The base spring boot class
 */
@SpringBootApplication(scanBasePackages = { "com.davita.ecm.*" })
public class FilenetContentLibApplication {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(FilenetContentLibApplication.class, args);

	}
}
