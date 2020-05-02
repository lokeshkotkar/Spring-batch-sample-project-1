package com.example.batchprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/*
 * this project is for built a batch job that reads  data from a spreadsheet, processed it, and wrote it to a database.
 */
@SpringBootApplication
public class BatchProcessingApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(BatchProcessingApplication.class, args);
	}
}
