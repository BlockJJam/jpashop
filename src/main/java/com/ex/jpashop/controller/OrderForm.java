package com.ex.jpashop.controller;

import com.ex.jpashop.domain.Item;
import com.ex.jpashop.domain.Member;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderForm {
    private Long id;

    private List<Member> memberList = new ArrayList<>();

    @NotEmpty
    private Long memberId;
    private List<Item> itemList = new ArrayList<>();

    @NotEmpty
    private Long itemId;
    @Min(1)
    private int count;

    public OrderForm(List<Member> memberList, List<Item> itemList){
        this.memberList = memberList;
        this.itemList = itemList;
    }

}
