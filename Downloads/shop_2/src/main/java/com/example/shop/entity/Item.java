package com.example.shop.entity;

import com.example.shop.dto.ItemFormDto;
import com.example.shop.constant.ItemSellStatus;
import com.example.shop.exception.OutOfStockException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name="item")
@Getter
@Setter
@ToString
public class Item extends BaseEntity{

    @Id
    @Column(name="item_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;    // 상품 ID

    @Column(nullable = false, length = 50)
    private String itemNm;  // 상품명

    @Column(name = "price", nullable = false)
    private int price;  // 가격

    @Column(nullable = false)
    private int stockNumber;    // 재고수량

    @Lob
    @Column(nullable = false)
    private String itemDetail;    // 상품 상세 설명

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;  // 상품 판매 상태

    // 상품 업데이트 로직 구현 (변경 감지 기능 사용)
    public void updateItem(ItemFormDto itemFormDto) {
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
    }

    // 상품을 주문할 경우 상품의 재고를 감소시키는 기능 구현
    // 엔티티 클래스 안에 비지니스 로직을 메소드로 작성하면 코드의 재사용과 데이터의
    // 변경 포인트를 한군데로 모을 수 있다는 장점이 있다.
    public void removeStock(int stockNumber) {
        // 상품의 재고 수량에서 주문 후 남은 재고 수량을 구함
        int restStock = this.stockNumber - stockNumber;

        // 상품의 재고가 주문 수량보다 작을 경우 잭 부족 예외를 발생시킨다.
        if (restStock < 0){
            throw new OutOfStockException("상품의 재고가 부족 합니다. (현재 재고 수량 : "
                    + this.stockNumber + ")");
        }
        // 주문 후 남은 재고 수량을 상품의 현재 재고 값으로 할당
        this.stockNumber = restStock;
    }

    // 주문취소 시 상품의 재고를 더해주기 위한 메소드
    public void addStock(int stockNumber) {
        this.stockNumber += stockNumber;
    }
}
