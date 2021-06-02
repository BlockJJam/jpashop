package com.ex.jpashop.api;

import com.ex.jpashop.domain.Address;
import com.ex.jpashop.domain.Order;
import com.ex.jpashop.domain.OrderStatus;
import com.ex.jpashop.repository.OrderRepository;
import com.ex.jpashop.repository.OrderSearch;
import com.ex.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * xToOne
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderListV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
        }
        return all; //
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderListV2(){
        List<Order> orderList = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orderList.stream()
                .map(SimpleOrderDto::new)
                .collect(toList());

        // 아직도, order -> member -> delivery 까지 가져오는 쿼리가 3번 나간다
        // ORDER -> SQL 1번 -> 결과 주문수 2번(row)
        // 1번째 Order 루프) Member 조회, Delivery 조회 -> 2번 추가 쿼리
        // 2번째 Order 루프) Member 조회, Delivery 조회 -> 2번 추가 쿼리 => N+1 문제 유발
        // 해결방안으로 FetchType.EAGER?? 최적화 안됨.. 오히려 양방향, 예상치 못한 쿼리 등등 문제만 더 커짐
        // 나중에 Fetch join으로 해결경
        return result;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderListV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> orderDtoList = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(toList());
        return orderDtoList;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderListV4(){
        return orderRepository.findOrderDto();
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
