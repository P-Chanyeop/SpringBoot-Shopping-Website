package com.example.shop.repository;

import com.example.shop.dto.ItemSearchDto;
import com.example.shop.dto.MainItemDto;
import com.example.shop.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


// Querydsl 과 Spring Data Jpa를 함께 사용하기 위해서는 사용자 정의 레파지토리를 정의해야한다
// 1. 사용자 정의 인터페이스 작성
// 2. 사용자 정의 인터페이스 구현
// 3. Spring Data Jpa 레파지토리에서 사용자 정의 인터페이스 상속

// 사용자 정의 인터페이스 작성
public interface ItemRepositoryCustom {

    // 상품 조회 조건을 담고 있는 itemSearchDto 객체와 페이징 정보를 담고 있는 pageable 객체를 파라미터로
    // 받는 getAdminItemPage 메소드를 정의. 반환 데이터로 Page<Item> 객체를 반환.
    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    // 메인 페이지에 보여줄 상품 리스트를 가져오는 메소드
    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto,
                                      Pageable pageable);
}
