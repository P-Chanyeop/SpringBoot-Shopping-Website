package com.example.shop.service;

import com.example.shop.dto.CartDetailDto;
import com.example.shop.dto.CartItemDto;
import com.example.shop.dto.CartOrderDto;
import com.example.shop.dto.OrderDto;
import com.example.shop.entity.Cart;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Item;
import com.example.shop.entity.Member;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.ItemRepository;
import com.example.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
// 장바구니에 상품을 담는 로직 구현 service
public class CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public Long addCart(CartItemDto cartItemDto, String email) {
        // 장바구니에 담을 상품 엔티티 조회
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        // 현재 로그인한 회원 엔티티 조회
        Member member = memberRepository.findByEmail(email);

        // 현재 로그인한 회원의 장바구니 엔티티 조회
        Cart cart = cartRepository.findByMemberId(member.getId());

        // 상품을 처음으로 장바구니에 담을 경우 해당 회원의 장바구니 엔티티로 생성
        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        // 현재 상품이 장바구니에 이미 들어있는지 조회
        CartItem savedCartItem =
                cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        // 장바구니에 이미 있던 상품일 경우, 기존 수량에 현재 장바구니에 담을 수량 만큼 더한다.
        if (savedCartItem != null) {
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else {
            // 장바구니 엔티티, 상품 엔티티, 장바구니에 담을 수량을 이용하여 CartItem 엔티티 생성
            CartItem cartItem =
                    CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            // 장바구니에 들어갈 상품을 저장
            return cartItem.getId();
        }
    }


    // 현재 로그인한 회원의 정보를 이용하여 장바구니에 들어있는 상품 조회
    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email) {

        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        // 현재 로그인한 회원의 장바구니 엔티티 조회
        Cart cart = cartRepository.findByMemberId(member.getId());
        // 장바구니에 상품을 한 번도 안담았을 경우, 장바구니 엔티티가 없으므로 빈 리스트 반환
        if (cart == null) {
            return cartDetailDtoList;
        }

        // 장바구니에 담겨있는 상품 정보를 조회
        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());

        return cartDetailDtoList;
    }

    // 장바구니 상품의 수량을 업데이트하는 로직, 자바스크립트에서 상품번호를 조작할 수 있으므로,
    // 현재 로그인한 회원과 해당 장바구니 상품을 저장한 회원이 같은지 검사
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {
        // 현재 로그인한 회원 조회
        Member curMember = memberRepository.findByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);

        // 장바구니 상품을 저장한 회원 조회
        Member savedMember = cartItem.getCart().getMember();

        // 현재 로그인한 회원과 장바구니 상품을 저장한 회원이 다를 경우 false,
        // 같으면 true 반환
        if (!StringUtils.equals(curMember.getEmail(),
                savedMember.getEmail())){
            return false;
        }

        return true;
    }

    // 장바구니 상품의 수량을 업데이트하는 메소드
    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);

        cartItem.updateCount(count);
    }

    // 장바구니의 상품을 삭제하는 메소드
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);
    }

    // 주문 로직으로 전달할 orderDto 리스트 생성 및 주문 로직 호출,
    // 주문한 상품은 장바구니에서 제거하는 로직 구현
    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email) {
        List<OrderDto> orderDtoList = new ArrayList<>();
        // 장바구니 페이지에서 잔달받은 주문 상품 번호를 이용하여 주문 로직으로 전달할
        // orderDto를 만든다.
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }

        // 장바구니에 담은 상품을 주문하도록 주문 로직 호출
        Long orderId = orderService.orders(orderDtoList, email);

        // 주문한 상품들을 장바구니에서 제거
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);

            cartItemRepository.delete(cartItem);
        }

        return orderId;
    }
}
