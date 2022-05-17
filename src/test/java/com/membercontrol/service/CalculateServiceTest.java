package com.membercontrol.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class CalculateServiceTest {
	@Autowired
	private CalculateService service;

	@Test
	void paySlipsOutput() {
	}

	@Test
	void readCSV() throws IOException {
		service.readCSV();
	}

	@Test
	void calculateSalary() {
	}

	@Test
	void createCsv() {
	}
}
