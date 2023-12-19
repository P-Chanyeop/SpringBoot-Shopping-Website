package com.example.shop.exception;

// 상품을 주문했을때 실제 재고가 없어 결품처리가 되는 현상 방지
// 재고의 수가 상품의 주문 수량보다 적을 때 발생시킬 exception 정의
public class OutOfStockException extends RuntimeException{

    public OutOfStockException(String message) {
        super(message);
    }
}
