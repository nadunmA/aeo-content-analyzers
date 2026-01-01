package com.aeo.analyzer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.aeo.analyzer.repository.AuditReportRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AnalyzerApplicationTests {

	@MockitoBean
	private AuditReportRepository auditLogRepository;

	@Test
	void contextLoads() {
	}

}
