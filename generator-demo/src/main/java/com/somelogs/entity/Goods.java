package com.somelogs.entity;

import java.util.Date;

public class Goods {
    /**
     * id: 
     */
    private Long id;

    /**
     * create_time: 创建时间
     */
    private Date createTime;

    /**
     * update_time: 更新时间
     */
    private Date updateTime;

    /**
     * goods_name: 商品名称
     */
    private String goodsName;

    /**
     * stock: 库存
     */
    private Integer stock;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName == null ? null : goodsName.trim();
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}