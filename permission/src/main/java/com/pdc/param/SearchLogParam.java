package com.pdc.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchLogParam {
    /**
     * LogType
     */
    private Integer type;
    /**
     * 操作前/后的值，即只保留上次和本次的值
     */
    private String beforeSeg;
    private String afterSeg;

    private String operator;
    /**
     * 开始时间和结束时间，即显示出时间在fromTime到toTime之间的记录
     * yyyy-MM-dd HH:mm:ss
     */
    private String fromTime;
    private String toTime;
}
