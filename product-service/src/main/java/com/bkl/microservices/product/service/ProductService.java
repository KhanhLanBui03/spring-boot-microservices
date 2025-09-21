package com.bkl.microservices.product.service;

import com.bkl.microservices.product.dto.ProductRequest;
import com.bkl.microservices.product.dto.ProductResponse;
import com.bkl.microservices.product.model.Product;
import com.bkl.microservices.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <-- QUAN TRỌNG: Thêm lại import này
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setSkuCode(productRequest.getSkuCode());
        product.setPrice(productRequest.getPrice());
        productRepository.save(product);

        log.info("Product created successfully: {}", product.getId());

        return new ProductResponse(product.getId(), product.getName(), product.getDescription(),product.getSkuCode(), product.getPrice());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getDescription(),product.getSkuCode(), product.getPrice()))
                .collect(Collectors.toList());
    }
}