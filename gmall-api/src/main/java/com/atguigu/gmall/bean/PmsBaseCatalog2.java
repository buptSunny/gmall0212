package com.atguigu.gmall.beans;

import com.atguigu.gmall.bean.BaseCatalog3;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @param
 * @return
 */
public class PmsBaseCatalog2 implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String name;
    @Column
    private String catalog1Id;

    @Transient
    private List<BaseCatalog3> catalog3List;

    public List<BaseCatalog3> getCatalog3List() {
        return catalog3List;
    }

    public void setCatalog3List(List<BaseCatalog3> catalog3List) {
        this.catalog3List = catalog3List;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatalog1Id() {
        return catalog1Id;
    }

    public void setCatalog1Id(String catalog1Id) {
        this.catalog1Id = catalog1Id;
    }
}
