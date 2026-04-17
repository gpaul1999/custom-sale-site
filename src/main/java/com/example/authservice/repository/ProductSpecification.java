package com.example.authservice.repository;

import com.example.authservice.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filter(
            Long productTypeId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean saleOnly,
            Long brandId,
            boolean enabledOnly
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (enabledOnly) {
                predicates.add(cb.isTrue(root.get("enabled")));
            }

            if (productTypeId != null) {
                predicates.add(cb.equal(root.get("productType").get("id"), productTypeId));
            }

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("syntax")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (Boolean.TRUE.equals(saleOnly)) {
                predicates.add(cb.isTrue(root.get("saleOff")));
            }

            if (brandId != null) {
                // join Product → ProductDetail → Brand
                var detail = root.join("productDetail", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(cb.equal(detail.get("brand").get("id"), brandId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

