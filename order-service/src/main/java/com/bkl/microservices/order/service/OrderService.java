package com.bkl.microservices.order.service;

import com.bkl.microservices.order.client.InventoryClient;
import com.bkl.microservices.order.dto.OrderRequest;
import com.bkl.microservices.order.event.OrderPlacedEvent;
import com.bkl.microservices.order.model.Order;
import com.bkl.microservices.order.repository.OrderRepository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {


    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void placeOrder(OrderRequest orderRequest) {
        try {
            // 1. Validate input
            if (orderRequest == null) {
                throw new IllegalArgumentException("Order request must not be null");
            }
            if (orderRequest.getUserDetails() == null || orderRequest.getUserDetails().getEmail() == null) {
                throw new IllegalArgumentException("User email is required");
            }

            // 2. Check inventory
            boolean isProductInStock = inventoryClient.isInStock(orderRequest.getSkuCode(), orderRequest.getQuantity());
            if (!isProductInStock) {
                throw new RuntimeException("Product with SkuCode " + orderRequest.getSkuCode() + " is not in stock");
            }

            // 3. Save order
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.getPrice());
            order.setSkuCode(orderRequest.getSkuCode());
            order.setQuantity(orderRequest.getQuantity());
            orderRepository.save(order);

            // 4. Publish Kafka event
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
            orderPlacedEvent.setEmail(orderRequest.getUserDetails().getEmail());
            orderPlacedEvent.setFirstName(orderRequest.getUserDetails().getFirstName());
            orderPlacedEvent.setLastName(orderRequest.getUserDetails().getLastName());
            log.info("Start - Sending OrderPlacedEvent {} to Kafka Topic", orderPlacedEvent);

            kafkaTemplate.send("order-placed", orderPlacedEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send OrderPlacedEvent to Kafka", ex);
                        } else {
                            log.info("Successfully sent OrderPlacedEvent to Kafka, offset={}",
                                    result.getRecordMetadata().offset());
                        }
                    });

            log.info("End - Order placed successfully");

        } catch (Exception e) {
            log.error("Error while placing order: {}", e.getMessage(), e);
            throw new RuntimeException("Could not place order", e);
        }
    }
}


