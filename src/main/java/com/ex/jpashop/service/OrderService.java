package com.ex.jpashop.service;

import com.ex.jpashop.domain.*;
import com.ex.jpashop.repository.ItemRepository;
import com.ex.jpashop.repository.MemberRepository;
import com.ex.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Long order(Long memberId, Long itemId, int count){

        // entity select
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        // delivery info create
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // order item create
        OrderItem orderItem  = OrderItem.craeteOrderItem(item, item.getPrice(), count);

        // order create
        Order order = Order.createOrder(member, delivery, orderItem);

        orderRepository.save(order);

        return order.getId();
    }
}
