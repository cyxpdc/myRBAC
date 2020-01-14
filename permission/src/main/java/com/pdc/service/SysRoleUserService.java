package com.pdc.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pdc.beans.LogType;
import com.pdc.common.RequestHolder;
import com.pdc.dao.SysLogMapper;
import com.pdc.dao.SysRoleUserMapper;
import com.pdc.dao.SysUserMapper;
import com.pdc.model.SysLogWithBLOBs;
import com.pdc.model.SysRoleUser;
import com.pdc.model.SysUser;
import com.pdc.util.IpUtil;
import com.pdc.util.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class SysRoleUserService {

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysLogMapper sysLogMapper;

    /**
     * 根据角色id获取对应的用户列表
     * @param roleId
     * @return
     */
    public List<SysUser> getUserListByRoleId(int roleId) {
        List<Integer> userIdList = sysRoleUserMapper.getUserIdListByRoleId(roleId);
        if (CollectionUtils.isEmpty(userIdList)) {
            return Lists.newArrayList();
        }
        return sysUserMapper.getUserListByIdList(userIdList);
    }

    /**
     * 修改当前角色的对应用户
     * 类似SysRoleAclService#changeRoleAcls
     * @param roleId
     * @param afterUserIdList
     */
    public void  changeRoleUsers(int roleId, List<Integer> afterUserIdList) {
        List<Integer> beforeUserIdList = sysRoleUserMapper.getUserIdListByRoleId(roleId);
        if (beforeUserIdList.size() == afterUserIdList.size()) {
            Set<Integer> beforeUserIdSet = Sets.newHashSet(beforeUserIdList);
            Set<Integer> afterUserIdSet = Sets.newHashSet(afterUserIdList);
            beforeUserIdSet.removeAll(afterUserIdSet);
            if (CollectionUtils.isEmpty(beforeUserIdSet)) {
                return;//即如果新旧用户id相同，即无须修改，直接返回
            }
        }
        updateRoleUsers(roleId, afterUserIdList);
        saveRoleUserLog(roleId, beforeUserIdList, afterUserIdList);
    }

    @Transactional
    public void updateRoleUsers(int roleId, List<Integer> userIdList) {
        sysRoleUserMapper.deleteByRoleId(roleId);

        if (CollectionUtils.isEmpty(userIdList)) {
            return;
        }
        List<SysRoleUser> roleUserList = Lists.newArrayList();
        for (Integer userId : userIdList) {
            SysRoleUser roleUser = SysRoleUser.builder().roleId(roleId).userId(userId).operator(RequestHolder.getCurrentUser().getUsername())
                    .operateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest())).operateTime(new Date()).build();
            roleUserList.add(roleUser);
        }
        sysRoleUserMapper.batchInsert(roleUserList);
    }

    private void saveRoleUserLog(int roleId, List<Integer> before, List<Integer> after) {
        SysLogWithBLOBs sysLog = new SysLogWithBLOBs();
        sysLog.setType(LogType.TYPE_ROLE_USER);
        sysLog.setTargetId(roleId);
        sysLog.setOldValue(before == null ? "" : JsonMapper.obj2String(before));
        sysLog.setNewValue(after == null ? "" : JsonMapper.obj2String(after));
        sysLog.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysLog.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysLog.setOperateTime(new Date());
        sysLog.setStatus(1);
        sysLogMapper.insertSelective(sysLog);
    }
}
