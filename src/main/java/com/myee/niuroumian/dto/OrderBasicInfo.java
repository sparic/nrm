package com.myee.niuroumian.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Ray.Fu on 2016/6/12.
 */
public class OrderBasicInfo {

    private String orderName;

    private Long orderId;

    private BigDecimal orderPrice;

    private List<OrderRelDto> orderRelDtoList;

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public List<OrderRelDto> getOrderRelDtoList() {
        return orderRelDtoList;
    }

    public void setOrderRelDtoList(List<OrderRelDto> orderRelDtoList) {
        this.orderRelDtoList = orderRelDtoList;
    }

    @Override
    public String toString() {
        return "OrderBasicInfo{" +
                "orderName='" + orderName + '\'' +
                ", orderId=" + orderId +
                ", orderPrice=" + orderPrice +
                ", orderRelDtoList=" + orderRelDtoList +
                '}';
    }
}
