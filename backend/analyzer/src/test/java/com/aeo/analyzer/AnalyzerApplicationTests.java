package com.aeo.analyzer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.aeo.analyzer.repository.AuditReportRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
})
class AnalyzerApplicationTests {

	@MockitoBean
	private AuditReportRepository auditReportRepository;

	@Test
	void contextLoads() {

	}

}
