package com.example.shop.repository;


import com.example.shop.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>,
        // ItemRepositoryCustom 인터페이스 상속, Querydsl로 구현한 상품 페이지 목록을 불러오는 getAdminItemPage() 메소드 사용가능
        QuerydslPredicateExecutor<Item>, ItemRepositoryCustom{

    // 상품명으로 검색
    List<Item> findByItemNm(String itemNm);

    // 상품명 or 상품 상세설명으로 검색
    List<Item> findByItemNmOrItemDetail(String itemNm, String itemDetail);

    // 지정 가격보다 값이 작은 데이터 검색
    List<Item> findByPriceLessThan(Integer price);

    // 지정 가격보다 값이 작은 데이터 검색 후 정렬
    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);

    // JPQL로 작성한 쿼리문  @Query 어노테이션을 활용
    @Query("select i from Item i where i.itemDetail like %:itemDetail% order by i.price desc ")
    List<Item> findByItemDetail(@Param("itemDetail") String itemDetail);


}
