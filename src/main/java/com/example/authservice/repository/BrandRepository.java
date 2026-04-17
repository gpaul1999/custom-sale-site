package com.example.authservice.repository;

import com.example.authservice.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByEnabled(boolean enabled);

    @Query("SELECT DISTINCT pd.brand FROM ProductDetail pd " +
           "WHERE pd.product.productType.id = :typeId " +
           "AND pd.brand IS NOT NULL " +
           "AND pd.brand.enabled = true " +
           "AND pd.product.enabled = true")
    List<Brand> findByProductTypeId(@Param("typeId") Long typeId);
}

