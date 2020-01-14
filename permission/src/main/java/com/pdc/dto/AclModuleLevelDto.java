package com.pdc.dto;

import com.google.common.collect.Lists;
import com.pdc.model.SysAclModule;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Getter
@Setter
@ToString
public class AclModuleLevelDto extends SysAclModule {
    /**
     * “权限模块列表”和“角色与权限”使用
     */
    private List<AclModuleLevelDto> aclModuleList = Lists.newArrayList();
    /**
     * “角色与权限”使用
     */
    private List<AclDto> aclList = Lists.newArrayList();

    public static AclModuleLevelDto adapt(SysAclModule aclModule) {
        AclModuleLevelDto dto = new AclModuleLevelDto();
        BeanUtils.copyProperties(aclModule, dto);
        return dto;
    }
}
