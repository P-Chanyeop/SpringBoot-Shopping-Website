package com.example.shop.entity;

import com.example.shop.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime orderDate;    // 주문일

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;    // 주문상태

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 주문 상품 정보들을 담아준다.
    // orderItem 객체를 order 객체의 orderItems 리스트에 추가
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        // Order 엔티티와 OrderItem 엔티티가 양방향 참조 관계이므로,
        // orderItem 객체에도 order 객체를 세팅
        orderItem.setOrder(this);
    }

    public static Order createOrder(Member memeber, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setMember(memeber);   // 상품을 주문한 회원의 정보를 세팅

        // 장바구니 페이지에서 한 번에 여러개의 상품을 주문하기에,
        // 여러개의 주문 상품을 담을 수 있도록 리스트 형태로 파라미터 값을 받으며
        // 주문 객체에 orderItem 객체를 추가
        for (OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
        }

        order.setOrderStatus(OrderStatus.ORDER);    // 주문 상태를 ORDER 로 세팅
        order.setOrderDate(LocalDateTime.now());    // 현재 시간을 주문 시간으로 세팅
        return order;
    }

    // 총 주문 금액을 구하는 메소드
    public int getTotalPrice(){
        int totalPrice = 0;

        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }

        return totalPrice;
    }

    // 주문 취소 시 주문 상태를 취소 상태로 변경해주는 메소드
    public void cancelOrder(){
        this.orderStatus = OrderStatus.CANCEL;

        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }
}
