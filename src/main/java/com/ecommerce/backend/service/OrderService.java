package com.ecommerce.backend.service;

import com.ecommerce.backend.document.Order;
import com.ecommerce.backend.document.Outbox;
import com.ecommerce.backend.dto.OrderRequest;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    public void createOrder(OrderRequest orderRequest, String username) {
        for (com.ecommerce.backend.dto.Order order : orderRequest.getOrders()) {
            Order orderEntity = Order.builder()
                    .username(username)
                    .productId(order.getProductId())
                    .quantity(order.getQuantity())
                    .build();
            orderRepository.save(orderEntity);

            try {
                String payload = objectMapper.writeValueAsString(orderEntity);
                Outbox outbox = Outbox.builder()
                        .aggregateId(orderEntity.getId())
                        .payload(payload)
                        .build();
                outboxRepository.save(outbox);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing order", e);
            }
        }
    }
}
