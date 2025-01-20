package com.example.capitalgains;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/capital-gains")
public class CapitalGainController {
    private final CapitalGainService capitalGainService;

    public CapitalGainController(CapitalGainService capitalGainService) {
        this.capitalGainService = capitalGainService;
    }

    @GetMapping
    public ResponseEntity<?> getCapitalGains(
        @RequestParam(required = false) Integer numsharesgt,
        @RequestParam(required = false) Integer numshareslt
    ) {
        try {
            float gains = capitalGainService.calculateCapitalGains(numsharesgt, numshareslt);
            return ResponseEntity.ok(gains);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error calculating capital gains: " + e.getMessage()));
        }
    }
}

class CapitalGainsResponse {
    private final float capitalGains;

    public CapitalGainsResponse(float capitalGains) {
        this.capitalGains = capitalGains;
    }

    public float getCapitalGains() {
        return capitalGains;
    }
}

class ErrorResponse {
    private final String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
} 