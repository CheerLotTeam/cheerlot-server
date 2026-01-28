package com.gms.cheerlot.cache;

import com.gms.cheerlot.cache.dto.ResetResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheLoader cacheLoader;

    @PostMapping("/reset")
    public ResponseEntity<ResetResult> reset() {
        ResetResult result = cacheLoader.reset();
        return ResponseEntity.ok(result);
    }
}
