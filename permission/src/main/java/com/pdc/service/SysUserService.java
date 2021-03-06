package com.pdc.service;

import com.google.common.base.Preconditions;
import com.pdc.beans.Mail;
import com.pdc.beans.PageQuery;
import com.pdc.beans.PageResult;
import com.pdc.common.RequestHolder;
import com.pdc.dao.SysUserMapper;
import com.pdc.exception.ParamException;
import com.pdc.model.SysUser;
import com.pdc.param.UserParam;
import com.pdc.util.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysLogService sysLogService;

    /**
     * 新增用户
     * @param userParam
     */
    public void save(UserParam userParam) {
        BeanValidator.check(userParam);
        if(checkTelephoneExist(userParam.getTelephone(), userParam.getId())) {
            throw new ParamException("电话已被占用");
        }
        if(checkEmailExist(userParam.getMail(), userParam.getId())) {
            throw new ParamException("邮箱已被占用");
        }
        String password = PasswordUtil.randomPassword();
        //加密
        String encryptedPassword = MD5Util.encrypt(password);
        SysUser user = SysUser.builder().username(userParam.getUsername()).telephone(userParam.getTelephone()).mail(userParam.getMail())
                .password(encryptedPassword).deptId(userParam.getDeptId()).status(userParam.getStatus()).remark(userParam.getRemark()).build();
        user.setOperator(RequestHolder.getCurrentUser().getUsername());
        user.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        user.setOperateTime(new Date());
        //发邮箱将密码发给用户
        Mail mail = new Mail();
        mail.setSubject("请收密码");
        mail.setMessage(password);
        Set<String> emailSet = new HashSet<>();
        emailSet.add(user.getMail());
        mail.setReceivers(emailSet);
        MailUtil.send(mail);
        sysUserMapper.insertSelective(user);
        sysLogService.saveUserLog(null, user);
    }

    /**
     * 更新
     * @param param
     */
    public void update(UserParam param) {
        BeanValidator.check(param);
        if(checkTelephoneExist(param.getTelephone(), param.getId())) {
            throw new ParamException("电话已被占用");
        }
        if(checkEmailExist(param.getMail(), param.getId())) {
            throw new ParamException("邮箱已被占用");
        }
        SysUser before = sysUserMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before, "待更新的用户不存在");
        SysUser after = SysUser.builder().id(param.getId()).username(param.getUsername()).telephone(param.getTelephone()).mail(param.getMail())
                .deptId(param.getDeptId()).status(param.getStatus()).remark(param.getRemark()).build();
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperateTime(new Date());
        sysUserMapper.updateByPrimaryKeySelective(after);
        sysLogService.saveUserLog(before, after);
    }

    /**
     * 校验邮箱和电话
     * 传id是为了更新的时候可以用此方法来做校验，代码复用
     * 比如某次更新并没有更新邮箱，而sql中并没有加AND id ！= #{id}，则会得出此条数据的mail和参数mail相同，导致返回结果>0，更新失败
     * @param mail
     * @param userId
     * @return
     */
    public boolean checkEmailExist(String mail, Integer userId) {
        return sysUserMapper.countByMail(mail, userId) > 0;
    }
    public boolean checkTelephoneExist(String telephone, Integer userId) {
        return sysUserMapper.countByTelephone(telephone, userId) > 0;
    }

    public SysUser findByKeyword(String keyword) {
        return sysUserMapper.findByKeyword(keyword);
    }

    /**
     * 得到分页列表
     * @param deptId
     * @param page
     * @return
     */
    public PageResult<SysUser> getPageByDeptId(int deptId, PageQuery page) {
        BeanValidator.check(page);
        //得到指定部门的总记录数
        int count = sysUserMapper.countByDeptId(deptId);
        if (count > 0) {
            List<SysUser> list = sysUserMapper.getPageByDeptId(deptId, page);
            return PageResult.<SysUser>builder().total(count).data(list).build();
        }
        return PageResult.<SysUser>builder().build();
    }

    public List<SysUser> getAll() {
        return sysUserMapper.getAll();
    }
}
