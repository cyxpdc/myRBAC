package com.pdc.model;

/**
 * 将text类型的oldValue和newValue提取出来单独成类
 * 从性能考虑，因为text不一定每次都需要使用
 * @author pdc
 */
public class SysLogWithBLOBs extends SysLog {
    private String oldValue;

    private String newValue;

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue == null ? null : oldValue.trim();
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue == null ? null : newValue.trim();
    }
}