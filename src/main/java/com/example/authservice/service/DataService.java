package com.example.authservice.service;

import com.example.authservice.dto.*;
import com.example.authservice.entity.*;
import com.example.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataService {

    private final ProductTypeRepository productTypeRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;

    public List<ProductTypeResponse> getAllProductTypes() {
        return productTypeRepository.findByEnabled(true).stream()
                .map(this::toProductTypeResponse)
                .collect(Collectors.toList());
    }

    public ProductTypeResponse getProductTypeById(Long id) {
        ProductType pt = productTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductType not found: " + id));
        return toProductTypeResponse(pt);
    }

    public List<BrandResponse> getAllBrands() {
        return brandRepository.findByEnabled(true).stream()
                .map(this::toBrandResponse)
                .collect(Collectors.toList());
    }

    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + id));
        return toBrandResponse(brand);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findByEnabled(true).stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return toProductResponse(product);
    }

    public List<ProductResponse> getProductsByType(Long productTypeId) {
        return productRepository.findByProductTypeIdAndEnabled(productTypeId, true).stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    public List<BrandResponse> getBrandsByProductType(Long productTypeId) {
        return brandRepository.findByProductTypeId(productTypeId).stream()
                .map(this::toBrandResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> filterProducts(
            Long productTypeId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean saleOnly,
            Long brandId,
            String sortBy
    ) {
        Sort sort = switch (sortBy == null ? "" : sortBy) {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name_asc"   -> Sort.by("syntax").ascending();
            default           -> Sort.by("id").ascending();
        };
        return productRepository.findAll(
                ProductSpecification.filter(productTypeId, keyword, minPrice, maxPrice, saleOnly, brandId, true),
                sort
        ).stream().map(this::toProductResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getSaleOffProducts() {
        return productRepository.findByEnabled(true).stream()
                .filter(p -> p.isSaleOff())
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    public ProductDetailResponse getProductDetailById(Long id) {
        ProductDetail detail = productDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductDetail not found: " + id));
        return toProductDetailResponse(detail);
    }

    public ProductPageData getProductPageData(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        if (!product.isEnabled()) {
            throw new RuntimeException("Product is not available: " + productId);
        }
        ProductResponse productResponse = toProductResponse(product);
        ProductDetailResponse detailResponse = product.getProductDetail() != null
                ? toProductDetailResponse(product.getProductDetail())
                : null;
        return new ProductPageData(productResponse, detailResponse);
    }

    public record ProductPageData(ProductResponse product, ProductDetailResponse detail) {}

    private ProductTypeResponse toProductTypeResponse(ProductType pt) {
        return new ProductTypeResponse(pt.getId(), pt.getSyntax(), pt.getDescription());
    }

    private BrandResponse toBrandResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getLogo(),
                brand.getLongDescription()
        );
    }

    private ProductResponse toProductResponse(Product product) {
        BigDecimal salePrice = null;
        if (product.isSaleOff()
                && product.getPrice() != null
                && product.getSalePercent() != null
                && product.getSalePercent() > 0) {
            BigDecimal discount = product.getPrice()
                    .multiply(BigDecimal.valueOf(product.getSalePercent()))
                    .divide(BigDecimal.valueOf(100));
            salePrice = product.getPrice().subtract(discount);
        }
        return new ProductResponse(
                product.getId(),
                product.getSyntax(),
                product.getDescription(),
                product.getPrice(),
                product.isSaleOff(),
                product.getSalePercent(),
                salePrice,
                product.getImages(),
                product.getProductType() != null ? product.getProductType().getId() : null,
                product.getProductType() != null ? product.getProductType().getSyntax() : null
        );
    }

    private ProductDetailResponse toProductDetailResponse(ProductDetail detail) {
        LocalDate today = LocalDate.now();
        List<PromotionResponse> promotions = detail.getPromotions() != null
                ? detail.getPromotions().stream()
                    .filter(p -> p.isEnabled())   // chỉ show promotion đang bật
                    .map(p -> new PromotionResponse(
                            p.getId(),
                            p.getTitle(),
                            p.getDescription(),
                            p.getStartDate(),
                            p.getEndDate(),
                            !today.isBefore(p.getStartDate()) && !today.isAfter(p.getEndDate())
                    ))
                    .collect(Collectors.toList())
                : Collections.emptyList();

        return new ProductDetailResponse(
                detail.getId(),
                detail.isVat(),
                detail.isInStock(),
                detail.getShortDescription(),
                detail.getSummaryDescription(),
                detail.getDetailDescription(),
                detail.getFinalDescription(),
                detail.getTechnicalFunctions(),
                promotions,
                detail.getBrand() != null ? detail.getBrand().getId() : null,
                detail.getBrand() != null ? detail.getBrand().getName() : null,
                detail.getBrand() != null ? detail.getBrand().getLogo() : null,
                detail.getBrand() != null ? detail.getBrand().getLongDescription() : null
        );
    }
}

