package com.mmall.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;
/**
 * 权限相关更新记录表sys_log
 * 用来权限恢复
 * @author pdc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysLog {
    private Integer id;
    /**
     * 不同表用不同type表示
     * 1：部门，2：用户，3：权限模块，4：权限，5：角色，6：角色用户关系，7：角色权限关系
     */
    private Integer type;
    /**
     * 用来实现日志恢复操作可以使用，加上oldValue和newValue
     * 存储所记录的表的主键
     * 角色权限表和角色用户表则存的是角色id
     * 因为是对角色对应的用户、权限进行调整
     */
    private Integer targetId;

    private String operator;

    private Date operateTime;

    private String operateIp;
    /**
     * 当前是否复原过，0：没有，1：复原过
     */
    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator == null ? null : operator.trim();
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public String getOperateIp() {
        return operateIp;
    }

    public void setOperateIp(String operateIp) {
        this.operateIp = operateIp == null ? null : operateIp.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}