package com.bkl.microservices.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor   // Sinh constructor có đủ tham số
@NoArgsConstructor    // Sinh constructor rỗng
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private String skuCode;
    private BigDecimal price;
}
