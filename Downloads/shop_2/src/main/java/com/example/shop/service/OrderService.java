package com.example.shop.service;


import com.example.shop.dto.OrderDto;
import com.example.shop.dto.OrderHistDto;
import com.example.shop.dto.OrderItemDto;
import com.example.shop.entity.*;
import com.example.shop.repository.ItemImgRepository;
import com.example.shop.repository.ItemRepository;
import com.example.shop.repository.MemberRepository;
import com.example.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

// 주문 로직을 작성하기 위한 클래스
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemImgRepository itemImgRepository;

    public Long order(OrderDto orderDto, String email) {
        // 주문할 상품을 조회
        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        // 현재 로그인한 회원의 이메일 정보를 이용하여 회원 정보를 조회
        Member member = memberRepository.findByEmail(email);

        List<OrderItem> orderItemList = new ArrayList<>();
        // 주문할 상품 엔티티와 주문 수량을 이용하여 주문 상품 엔티티를 생성
        OrderItem orderItem =
                OrderItem.createOrderItem(item, orderDto.getCount());
        orderItemList.add(orderItem);

        // 회원 정보와 주문할 상품 리스트 정보를 이용하여 주문 엔티티를 생성
        Order order = Order.createOrder(member, orderItemList);
        // 생성한 주문 엔티티를 저장한다.환
        orderRepository.save(order);

        return order.getId();
    }

    // 주문 목록 조회 메소드
    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {
        // 유저의 아이디와 페이징 조건을 이용하여 주문 목록을 조회
        List<Order> orders = orderRepository.findOrders(email, pageable);
        // 유저의 주문 총 개수를 조회
        Long totalCount = orderRepository.countOrder(email);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        // 주문 리스트를 순회하면서 구매 이력 페이지에 전달할 DTO 생성
        // for 문을 순회하면서 order.getOrderItems()를 호출할때마다 조회 쿼리문이 추가적으로 실행되기떄문에
        // order 리스트의 사이즈 만큼의 쿼리문이 실행된다.
        // order 의 주문 아이디를 "where order_id in (209, 210, 211, 212)" 이런식으로
        // in 쿼리를 사용하여 한번에 조회할 수 있다면 여러개가 실행될 쿼리를 하나로 줄일 수 있다. => default_batch_size 지정
        // JPA 사용할 때 성능 이슈가 발생할 수 있는 N + 1 문제를 많이 만나게 되는데 조심해야 한다.
        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                // 주문한 상품의 대표 이미지를 조회
                ItemImg itemImg = itemImgRepository.
                        findByItemIdAndRepimgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto =
                        new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }

            orderHistDtos.add(orderHistDto);
        }

        // 페이지 구현 객체를 생성하여 반환
        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);
    }

    // 주문 취소 로직 구현
    // 현재 로그인한 사용자와 주문 데이터를 생성한 사용자가 같은지 검사 후 ,
    // 같을 때는 true를, 같지 않을 경우 false를 반환
    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email) {
        Member curMember = memberRepository.findByEmail(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        Member savedMember = order.getMember();

        if (!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())) {
            return false;
        }

        return true;
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        // 주문 취소 상태로 변경하면 변경 감지 기능의 의해서 트랜잭션이 끝날 때 update 쿼리가 실행된다.
        order.cancelOrder();
    }

    // 장바구니에서 주문할 상품 데이터를 전달받아, 주문을 생성하는 로직
    public Long orders(List<OrderDto> orderDtoList, String email) {

        Member member = memberRepository.findByEmail(email);
        List<OrderItem> orderItemList = new ArrayList<>();

        // 주문할 상품 리스트를 만든다
        for (OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            orderItemList.add(orderItem);
        }

        // 현재 로그인한 회원과, 주문 상품 목록을 이용하여 주문 엔티티를 만든다.
        Order order = Order.createOrder(member, orderItemList);
        // 주문 데이터 저장
        orderRepository.save(order);

        return order.getId();
    }
}
