package com.example.shop.repository;

import com.example.shop.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {

    List<ItemImg> findByItemIdOrderByIdAsc(Long itemID);

    // 상품의 대표 이미지를 찾는 쿼리 메소드 추가
    // 구매 이력 페이지에서 상품의 대표 이미지를 보여주기 위함
    ItemImg findByItemIdAndRepimgYn(Long itemId, String repimgYn);
}
