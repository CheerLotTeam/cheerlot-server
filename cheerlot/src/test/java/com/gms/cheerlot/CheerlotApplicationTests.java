package com.gms.cheerlot;

import com.gms.cheerlot.cache.CacheLoader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CheerlotApplicationTests {

	@MockitoBean
	private CacheLoader cacheLoader;

	@Test
	void contextLoads() {
	}

}
