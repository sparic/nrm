package com.myee.niuroumian.domain;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by Ray.Fu on 2016/6/1.
 */
@Entity
@Table(name = "t_dish")
public class DishInfo {
    @Id
    @Column(name = "dish_id", columnDefinition = "INT")
    @TableGenerator(name = "PkGen_100", table = "ad_sequence", pkColumnName = "name", pkColumnValue = "T_Dish", valueColumnName = "currentnextsys", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PkGen_100")
    private Long dishId;

    @Column(name = "dish_name")
    private String dishName;

    @Column(name = "dish_type")
    private String dishType;

    @Column(name = "dish_price")
    private BigDecimal dishPrice;

    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, targetEntity = StoreInfo.class)
    @JoinColumn(name = "store_id")
//    @Column(name = "store_id")
    private StoreInfo storeInfo;

    @Column(name = "img")
    private String img;

    //是否为点餐菜品
    @Column(name = "if_quick")
    private Long ifQuick;

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public BigDecimal getDishPrice() {
        return dishPrice;
    }

    public void setDishPrice(BigDecimal dishPrice) {
        this.dishPrice = dishPrice;
    }

//    public Long getStoreId() {
//        return storeId;
//    }
//
//    public void setStoreId(Long storeId) {
//        this.storeId = storeId;
//    }
    public StoreInfo getStoreInfo() {
        return storeInfo;
    }

    public void setStoreInfo(StoreInfo storeInfo) {
        this.storeInfo = storeInfo;
    }

    public String getDishType() {
        return dishType;
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }

    public Long getIfQuick() {
        return ifQuick;
    }

    public void setIfQuick(Long ifQuick) {
        this.ifQuick = ifQuick;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
