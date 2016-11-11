package com.myee.niuroumian.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Ray.Fu on 2016/6/12.
 */
public class OrderDisplay {
    private Integer count;

    private List<OrderBasicInfo> orderBasicInfoList;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<OrderBasicInfo> getOrderBasicInfoList() {
        return orderBasicInfoList;
    }

    public void setOrderBasicInfoList(List<OrderBasicInfo> orderBasicInfoList) {
        this.orderBasicInfoList = orderBasicInfoList;
    }
}
