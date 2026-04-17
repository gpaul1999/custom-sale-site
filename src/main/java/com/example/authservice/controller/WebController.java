package com.example.authservice.controller;

import com.example.authservice.dto.ProductResponse;
import com.example.authservice.dto.ProductTypeResponse;
import com.example.authservice.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final DataService dataService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/services")
    public String services(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean saleOnly,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            @RequestParam(required = false, defaultValue = "false") boolean filtered,
            Model model
    ) {
        List<ProductTypeResponse> productTypes = dataService.getAllProductTypes();

        Long activeTypeId = typeId;
        if (activeTypeId == null && !productTypes.isEmpty()) {
            activeTypeId = productTypes.get(0).getId();
        }

        Map<Long, List<ProductResponse>> productsByType = new LinkedHashMap<>();
        for (ProductTypeResponse pt : productTypes) {
            if (pt.getId().equals(activeTypeId) && filtered) {
                productsByType.put(pt.getId(), dataService.filterProducts(
                        activeTypeId, keyword, minPrice, maxPrice, saleOnly, brandId, sortBy));
            } else {
                productsByType.put(pt.getId(), dataService.getProductsByType(pt.getId()));
            }
        }

        // Brands available for the active tab (for dropdown)
        List<com.example.authservice.dto.BrandResponse> brandsForType =
                activeTypeId != null ? dataService.getBrandsByProductType(activeTypeId) : List.of();

        model.addAttribute("productTypes", productTypes);
        model.addAttribute("productsByType", productsByType);
        model.addAttribute("activeTypeId", activeTypeId);
        model.addAttribute("brandsForType", brandsForType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("saleOnly", Boolean.TRUE.equals(saleOnly));
        model.addAttribute("brandId", brandId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("filtered", filtered);
        return "services";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        DataService.ProductPageData data = dataService.getProductPageData(id);
        model.addAttribute("product", data.product());
        model.addAttribute("detail", data.detail());
        return "product-detail";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }
}
