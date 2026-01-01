package com.aeo.analyzer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Disabled;


@SpringBootTest
@Disabled("Skipping context load test in CI due to missing MongoDB")
class AnalyzerApplicationTests {



	@Test
	void contextLoads() {

	}

}
