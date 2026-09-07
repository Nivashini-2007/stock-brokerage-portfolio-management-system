package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.MarginResponse;
import com.stockbroker.backend.security.SecurityUtils;
import com.stockbroker.backend.service.MarginService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/margin")
public class MarginController {

    private final MarginService marginService;

    public MarginController(MarginService marginService) {
        this.marginService = marginService;
    }

    @GetMapping("/{clientId}")
    public MarginResponse getMargin(
            @PathVariable Long clientId) {

        SecurityUtils.assertCanAccessClient(clientId);
        return marginService.getMargin(clientId);
    }
}
