package com.pdc.dto;

import com.pdc.model.SysAcl;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@ToString
public class AclDto extends SysAcl {

    /**
     * 是否要默认选中，当前用户选中其某个角色时，此角色的权限点被勾选，即此字段为true:SysTreeService#roleTree
     */
    private boolean checked = false;

    /**
     * 当前用户是否有此权限操作
     */
    private boolean hasAcl = false;

    public static AclDto adapt(SysAcl acl) {
        AclDto dto = new AclDto();
        BeanUtils.copyProperties(acl, dto);
        return dto;
    }
}
