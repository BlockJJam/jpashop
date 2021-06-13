package com.ex.jpashop.api;

import com.ex.jpashop.domain.Address;
import com.ex.jpashop.domain.Order;
import com.ex.jpashop.domain.OrderItem;
import com.ex.jpashop.domain.OrderStatus;
import com.ex.jpashop.repository.OrderRepository;
import com.ex.jpashop.repository.OrderSearch;
import com.ex.jpashop.repository.order.query.OrderFlatDto;
import com.ex.jpashop.repository.order.query.OrderItemQueryDto;
import com.ex.jpashop.repository.order.query.OrderQueryDto;
import com.ex.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItemList = order.getOrderItems();
            orderItemList.stream().forEach(o-> o.getItem().getName()); // Hibernate5Module이 이렇게 필요한 정보를 강제 초기화 하면, 추가 쿼리를 보내서라 필요한 정보를 가져옴
        }

        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orderList = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orderList.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> orderList = orderRepository.findAllWithItem();

        List<OrderDto> result = orderList.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value="offset", defaultValue="0") int offset,
            @RequestParam(value="limit", defaultValue="100") int limit
    ){
        // ToOne 요소를 가지고 온다
        List<Order> orderList = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> result = orderList.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5(){
        return orderQueryRepository.findAllByDtos_Optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> orderV6(){
        // 만약 API스펙이 OrderQueryDto라면?
        List<OrderFlatDto> result = orderQueryRepository.findAllByDtos_Flat();
        return result.stream()
                .collect(groupingBy(o-> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o-> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(),o.getCount()), toList())
                )).entrySet().stream()
                .map(e-> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(),e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
        //API 스펙이 OrderFlatDto인 경우
        //return orderQueryRepository.findAllByDtos_Flat();
    }

    @Getter
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItemList; // DTO 안에 Entity가 Wrapping 되어서도 안된다(문제가 생긴 것)

        public OrderDto(Order order){
            orderId = order.getId();
            name= order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();
            address = order.getMember().getAddress();
//            orderItemList= order.getOrderItems().stream()
//                .map(oi-> new OrderItemDto(oi))
//                .collect(Collectors.toList()); // 이걸 줄여보자
            orderItemList = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }
    }

    @Getter
    static class OrderItemDto{          // OrderItem 리스트를 뽑아낼 때도 DTO로 해결
        private String  itemName; // 상품명
        private int orderPrice;  // 주문 가격
        private int count; // 주문 수량


        public OrderItemDto(OrderItem oi) {
            itemName = oi.getItem().getName();
            orderPrice = oi.getOrderPrice();
            count = oi.getCount();
        }
    }
}
