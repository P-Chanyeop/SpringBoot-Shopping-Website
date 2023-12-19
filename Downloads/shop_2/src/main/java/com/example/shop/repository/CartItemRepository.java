package com.example.shop.repository;

import com.example.shop.dto.CartDetailDto;
import com.example.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 카틑 아이디와 상품 아이디를 이용해서 상품이 장바구니에 들어있는지 조회
    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    // 장바구니 페이지에 전달할 CartDetailDto 리스트를 쿼리로 조회하는 JPQL문 작성
    // 연관 관계 매핑을 지연 로딩으로 설정할 경우, 엔티티에 매핑된 다른 엔티팉를 조회할 때
    // 추가적으로 쿼리문이 싱행되므로 성능 최적화가 필요한 경우, 아래와 같이 DTO 생성자를 통하여
    // 반환값으로 DTO 객체를 생성할 수 있다.
    @Query("select new com.example.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl) " +
            // CartDetailDto 생성자를 이용하여 DTO를 반환할 때는 위 코드처럼
            // new 키워드와 DTO의 패키지, 클래스명을 적는다
            "from CartItem ci, ItemImg im " +
            "join ci.item i " +
            "where ci.cart.id = :cartId " +
            // 장바구니에 담겨있는 상품의 대표 이미지만 가져 오도록 조건문 작성
            "and im.item.id = ci.item.id " +
            "and im.repimgYn = 'Y' " +
            "order by ci.regTime desc"
    )
    List<CartDetailDto> findCartDetailDtoList(Long cartId);
}
