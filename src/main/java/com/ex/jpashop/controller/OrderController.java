package com.ex.jpashop.controller;

import com.ex.jpashop.domain.Item;
import com.ex.jpashop.domain.Member;
import com.ex.jpashop.domain.Order;
import com.ex.jpashop.repository.OrderSearch;
import com.ex.jpashop.service.ItemService;
import com.ex.jpashop.service.MemberService;
import com.ex.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model){
        List<Member> memberList = memberService.findMembers();
        List<Item> itemList = itemService.findItems();
        OrderForm orderForm = new OrderForm(memberList, itemList);

        model.addAttribute("orderForm", orderForm);
        return "order/createOrderForm";
    }

    @PostMapping("/order")
    public String order(@Valid @ModelAttribute("form") OrderForm form, BindingResult result){
        if(result.hasErrors()){
            return "order/createOrderForm";
        }

        orderService.order(form.getMemberId(), form.getItemId(), form.getCount());
        return "redirect:/";
    }

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch")OrderSearch orderSearch, Model model){
        List<Order> orderList = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orderList);

        return "order/orderList";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId){
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
}
