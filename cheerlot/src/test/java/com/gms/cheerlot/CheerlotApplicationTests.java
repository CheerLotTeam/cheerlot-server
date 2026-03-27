package com.gms.cheerlot;

import com.gms.cheerlot.cache.CacheLoader;
import com.gms.cheerlot.gameschedule.service.GameScheduleFetcher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CheerlotApplicationTests {

	@MockitoBean
	private CacheLoader cacheLoader;

	@MockitoBean
	private GameScheduleFetcher gameScheduleFetcher;

	@Test
	void contextLoads() {
	}

}
