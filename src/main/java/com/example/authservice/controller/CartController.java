package com.example.authservice.controller;

import com.example.authservice.dto.ProductResponse;
import com.example.authservice.service.DataService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CartController {

    private static final String CART_COOKIE = "cart";
    private static final int MAX_AGE = 7 * 24 * 3600;

    private final ObjectMapper objectMapper;
    private final DataService dataService;

    // ─── Page ─────────────────────────────────────────────────────────────────
    @GetMapping("/cart")
    public String cartPage() {
        return "cart";
    }

    // ─── API: read cart ────────────────────────────────────────────────────────
    @GetMapping("/api/cart")
    @ResponseBody
    public List<CartItem> getCart(HttpServletRequest request) {
        return readCart(request);
    }

    // ─── API: add ─────────────────────────────────────────────────────────────
    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<List<CartItem>> addToCart(@RequestBody CartRequest req,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        List<CartItem> items = readCart(request);
        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getProductId().equals(req.getProductId()))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + Math.max(1, req.getQuantity()));
        } else {
            items.add(new CartItem(req.getProductId(), Math.max(1, req.getQuantity())));
        }
        writeCart(items, response);
        return ResponseEntity.ok(items);
    }

    // ─── API: update quantity ─────────────────────────────────────────────────
    @PostMapping("/api/cart/update")
    @ResponseBody
    public ResponseEntity<List<CartItem>> updateCart(@RequestBody CartRequest req,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
        List<CartItem> items = readCart(request);
        if (req.getQuantity() <= 0) {
            items.removeIf(i -> i.getProductId().equals(req.getProductId()));
        } else {
            items.stream()
                    .filter(i -> i.getProductId().equals(req.getProductId()))
                    .findFirst()
                    .ifPresent(i -> i.setQuantity(req.getQuantity()));
        }
        writeCart(items, response);
        return ResponseEntity.ok(items);
    }

    // ─── API: remove ──────────────────────────────────────────────────────────
    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<List<CartItem>> removeFromCart(@RequestBody CartRequest req,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) {
        List<CartItem> items = readCart(request);
        items.removeIf(i -> i.getProductId().equals(req.getProductId()));
        writeCart(items, response);
        return ResponseEntity.ok(items);
    }

    // ─── API: clear ───────────────────────────────────────────────────────────
    @PostMapping("/api/cart/clear")
    @ResponseBody
    public ResponseEntity<Void> clearCart(HttpServletResponse response) {
        writeCart(new ArrayList<>(), response);
        return ResponseEntity.ok().build();
    }

    // ─── API: products info for cart ──────────────────────────────────────────
    @PostMapping("/api/cart/products")
    @ResponseBody
    public ResponseEntity<List<ProductResponse>> getCartProducts(@RequestBody List<Long> ids) {
        List<ProductResponse> result = new ArrayList<>();
        for (Long id : ids) {
            try {
                result.add(dataService.getProductById(id));
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(result);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private List<CartItem> readCart(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return new ArrayList<>();
        for (Cookie c : cookies) {
            if (CART_COOKIE.equals(c.getName())) {
                try {
                    String json = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8);
                    return objectMapper.readValue(json, new TypeReference<>() {});
                } catch (Exception e) {
                    return new ArrayList<>();
                }
            }
        }
        return new ArrayList<>();
    }

    private void writeCart(List<CartItem> items, HttpServletResponse response) {
        try {
            String json = objectMapper.writeValueAsString(items);
            String encoded = URLEncoder.encode(json, StandardCharsets.UTF_8);
            Cookie cookie = new Cookie(CART_COOKIE, encoded);
            cookie.setPath("/");
            cookie.setMaxAge(items.isEmpty() ? 0 : MAX_AGE);
            response.addCookie(cookie);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ─── DTOs ─────────────────────────────────────────────────────────────────
    @Data
    public static class CartItem {
        private Long productId;
        private int quantity;

        public CartItem() {}
        public CartItem(Long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }

    @Data
    public static class CartRequest {
        private Long productId;
        private int quantity;
    }
}

