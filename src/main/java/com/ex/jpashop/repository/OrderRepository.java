package com.ex.jpashop.repository;

import com.ex.jpashop.domain.Member;
import com.ex.jpashop.domain.Order;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Predicate;

@Repository
public class OrderRepository {
    private final EntityManager em;

    public OrderRepository(EntityManager em){
        this.em = em;
    }

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch){
        //language= JPQL
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if(orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status= :status";
        }

        // 회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            if(isFirstCondition){
                jpql += " where";
                isFirstCondition= false;
            }else{
                jpql += " and";
            }
            jpql+= " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); // 최대 1000건
        if(orderSearch.getOrderStatus() != null){
            query= query.setParameter("status", orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())){
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    // JPA Criteria로 처리
    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER);
        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if(orderSearch.getOrderStatus() != null){
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            Predicate name = cb.like(m.<String>get("name"), "%"+
                    orderSearch.getMemberName()+"%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    public List<Order> findAll(OrderSearch orderSearch){
        return em.createQuery("select o from Order o join o.member m" +
                " where o.orderStatus = :status"+
                " and m.name like :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                .setFirstResult(0)
                .setMaxResults(10)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(){
        return em.createQuery(
                "select o from Order o"+
                        " join fetch o.member m"+
                        " join fetch o.delivery d", Order.class)
                .getResultList();
    }

    public List<OrderSimpleQueryDto> findOrderDto(){
        return em.createQuery(
                "select new com.ex.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.orderStatus, d.address) from Order o"+
                        " join o.member m"+
                        " join o.delivery d", OrderSimpleQueryDto.class
        ).getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit){
        return em.createQuery(
                "select o from Order o"+
                        " left outer join fetch o.member m"+
                        " left outer join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
    public List<Order> findAllWithItem(){
        return em.createQuery("select distinct o from Order o"+
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" +
                " join fetch oi.item i", Order.class)
                .getResultList();
        // 이대로 하면, 데이터가 2배로 뻥튀기 되버린다
        // join을 했을 때, orderItems 요소가 order 1개당 2개씩 있기 때문에, fetch join을 하면 같은 데이터가 2번씩 나온다
        // distinct를 jpa에서 쓰면, ( 요소가 하나라도 다르면 중복이 아니라고보는 DB와 다르게)
        // 불러온 Entity(id)가 같을 때 중복으로 보고 구별한다
        // 조심1) 일대다 fetch join에서는 페이징이 불가하는 걸 명심 -> 왜? 일대다에서 페치 + 페이징이 동시에 들어가면서 뻥튀기된 데이터를 처리하기전에 고려하게 된다
        // 조심2) 컬렉션 페치 조인은 1개만 사용 가능, 즉 join한 컬렉션에서 또 fetch join을 부르면 안된다 -> 데이터 부정합 조회 유발
    }
}
