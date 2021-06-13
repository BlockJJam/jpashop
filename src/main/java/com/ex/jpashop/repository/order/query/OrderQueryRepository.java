package com.ex.jpashop.repository.order.query;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos(){
        // toOne( member, delivery)를 먼저 조회
        List<OrderQueryDto> result = findOrders();

        // toMany( orderitem)를 loop를 돌며 조회
        result.forEach( o-> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItem(orderItems);
        });

        // N+1문제 존재
        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId){
        return em.createQuery(
                "select new com.ex.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders(){
        return em.createQuery(
                "select new com.ex.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.orderStatus, d.address ) " +
                        " from Order o"+
                        " join o.member m"+
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDtos_Optimization() {
        List<OrderQueryDto> result = findOrders();

        // result에서 order id 리스트 도출
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());

        // where in 을 활용해 orderId에 해당하는 orderItem 리스트 도출
        List<OrderItemQueryDto> orderItems = findOrderItemMap(orderIds);

        // orderItems를 map으로 최적화 -> Stream의 groupingBy를 사용
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));

        // order의 id == orderItem의 id인 orderItem을 map에서 꺼내 order에 setOrderItem 설정
        result.forEach(o-> o.setOrderItem(orderItemMap.get(o.getOrderId())));

        // query 2번으로 최적화 -> 메모리에서 엔티티에 필요한 데이터들을 매핑
        return result;
    }

    private List<OrderItemQueryDto> findOrderItemMap(List<Long> orderIds) {
        return em.createQuery(
                "select new com.ex.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
    }

    public List<OrderFlatDto> findAllByDtos_Flat() {
        // Query는 1번
        // order data는 중복이 들어갈 수 밖에 없음
        // 페이징은 안됨 -> Order를 기준으로 가져오지 못하고, 쿼리 결과 중에서 페이징을 하게 됨
        // 애플리케이션에서 추가 작업이 크다
        return  em.createQuery(
                "select new" +
                        " com.ex.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.orderStatus, d.address,i.name , oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m"+
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
