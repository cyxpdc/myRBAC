package com.mmall.service;

import com.google.common.collect.Lists;
import com.mmall.beans.CacheKeyConstants;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysAclMapper;
import com.mmall.dao.SysRoleAclMapper;
import com.mmall.dao.SysRoleUserMapper;
import com.mmall.model.SysAcl;
import com.mmall.model.SysUser;
import com.mmall.util.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysCoreService {

    @Resource
    private SysAclMapper sysAclMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleAclMapper sysRoleAclMapper;
    @Resource
    private SysCacheService sysCacheService;

    /**
     * 获取当前用户权限列表
     * 需要多表关联：sysRoleUserMapper、sysRoleAclMapper、sysAclMapper
     * @return
     */
    public List<SysAcl> getCurrentUserAclList() {
        //获取当前用户id
        int userId = RequestHolder.getCurrentUser().getId();
        return getUserAclList(userId);
    }

    /**
     * 根据用户id获取权限点列表
     * @param userId
     * @return
     */
    public List<SysAcl> getUserAclList(int userId) {
        //超级管理员拥有所有权限
        if (isSuperAdmin()) {
            return sysAclMapper.getAll();
        }
        //根据用户id获取用户的角色id列表
        List<Integer> userRoleIdList = sysRoleUserMapper.getRoleIdListByUserId(userId);
        if (CollectionUtils.isEmpty(userRoleIdList)) {
            return Lists.newArrayList();
        }
        //根据用户的角色id列表获取权限点id列表，即获取角色权限列表方法
        List<Integer> userAclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(userRoleIdList);
        if (CollectionUtils.isEmpty(userAclIdList)) {
            return Lists.newArrayList();
        }
        //根据权限点id列表获取权限点
        return sysAclMapper.getByIdList(userAclIdList);
    }

    /**
     * 获取角色权限列表
     * @param roleId
     * @return
     */
    public List<SysAcl> getRoleAclList(int roleId) {
        //根据角色id列表获取权限点id列表
        List<Integer> aclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(Lists.<Integer>newArrayList(roleId));
        if (CollectionUtils.isEmpty(aclIdList)) {
            return Lists.newArrayList();
        }
        //根据权限点id列表获取权限点
        return sysAclMapper.getByIdList(aclIdList);
    }

    /**
     * 判断当前是否是否为超级管理员
     * 超级管理员自定义为admin
     * 可以是配置文件获取，可以指定某个用户，也可以指定某个角色
     * @return
     */
    public boolean isSuperAdmin() {
        SysUser sysUser = RequestHolder.getCurrentUser();
        if (sysUser.getMail().contains("admin")) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前用户是否可以访问此url
     * @param url
     * @return
     */
    public boolean hasUrlAcl(String url) {
        if (isSuperAdmin()) return true;

        List<SysAcl> urlAclList = sysAclMapper.getByUrl(url);
        //为空代表此接口无需任何权限
        if (CollectionUtils.isEmpty(urlAclList)) return true;
        //获取当前用户的权限点id
        List<SysAcl> userAclList = getCurrentUserAclListFromCache();
        Set<Integer> userAclIdSet = userAclList.stream().map(acl -> acl.getId()).collect(Collectors.toSet());

        boolean hasValidAcl = false;//是否有有效的权限点
        // 规则：只要有一个权限点有访问此url的权限，那么我们就返回true
        for (SysAcl acl : urlAclList) {
            // 判断一个用户是否具有某个权限点的访问权限
            // url的某个权限点无效,说明此权限点无须判断
            if (acl == null || acl.getStatus() != 1) {
                continue;
            }
            hasValidAcl = true;
            if (userAclIdSet.contains(acl.getId())) {
                return true;
            }
        }
        //如果没有有效的权限点，说明url谁都可以访问，返回true即可
        if (!hasValidAcl) {
            return true;
        }
        return false;
    }

    public List<SysAcl> getCurrentUserAclListFromCache() {
        int userId = RequestHolder.getCurrentUser().getId();
        String cacheValue = sysCacheService.getFromCache(CacheKeyConstants.USER_ACLS, String.valueOf(userId));
        if (StringUtils.isBlank(cacheValue)) {
            List<SysAcl> aclList = getCurrentUserAclList();
            if (CollectionUtils.isNotEmpty(aclList)) {
                sysCacheService.saveCache(JsonMapper.obj2String(aclList), 600, CacheKeyConstants.USER_ACLS, String.valueOf(userId));
            }
            return aclList;
        }
        return JsonMapper.string2Obj(cacheValue, new TypeReference<List<SysAcl>>() {
        });
    }
}
