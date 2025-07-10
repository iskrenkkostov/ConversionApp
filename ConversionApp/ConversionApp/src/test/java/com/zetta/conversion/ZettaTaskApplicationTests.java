package com.zetta.conversion;

import com.zetta.conversion.service.ConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ZettaTaskApplicationTests {

	@Autowired
	private ConversionService conversionService;

	@Test
	void contextLoads() {
		assertNotNull(conversionService);
	}

}
