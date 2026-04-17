package com.example.authservice.repository;

import com.example.authservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByProductTypeId(Long productTypeId);
    List<Product> findBySaleOff(boolean saleOff);
    List<Product> findByEnabled(boolean enabled);
    List<Product> findByProductTypeIdAndEnabled(Long productTypeId, boolean enabled);
}

