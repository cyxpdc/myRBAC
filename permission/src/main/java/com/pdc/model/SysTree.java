package com.pdc.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 树结构抽取
 * @author PDC
 */
@Setter
@Getter
public class SysTree {
    private Integer id;

    private String name;
    /**
     * 父层级和当前层级
     */
    private Integer parentId;
    private String level;
    /**
     * 在当前level的下的顺序，用来排序
     */
    private Integer seq;
    /**
     * 备注
     */
    private String remark;
    /**
     * 操作者、操作时间、操作ip
     */
    private String operator;
    private Date operateTime;
    private String operateIp;
}
