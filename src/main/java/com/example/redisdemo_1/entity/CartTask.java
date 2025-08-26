package com.example.redisdemo_1.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 购物车任务实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartTask implements Serializable {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 商品id
     */
    private String productId;
    /**
     * 订单id
     */
    private String orderId;


}
