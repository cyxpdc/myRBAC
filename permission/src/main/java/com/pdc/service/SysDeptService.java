package com.pdc.service;

import com.google.common.base.Preconditions;
import com.pdc.common.RequestHolder;
import com.pdc.dao.SysDeptMapper;
import com.pdc.dao.SysUserMapper;
import com.pdc.exception.ParamException;
import com.pdc.model.SysDept;
import com.pdc.param.DeptParam;
import com.pdc.util.BeanValidator;
import com.pdc.util.IpUtil;
import com.pdc.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SysDeptService {

    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysLogService sysLogService;

    public void save(DeptParam param) {
        BeanValidator.check(param);
        if(checkExist(param.getParentId(), param.getName(), param.getId())) {
            throw new ParamException("同一层级下存在相同名称的部门，保存失败");
        }
        //封装部门表
        SysDept dept = SysDept.builder().name(param.getName()).parentId(param.getParentId())
                .seq(param.getSeq()).remark(param.getRemark()).build();

        dept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()), param.getParentId()));
        dept.setOperator(RequestHolder.getCurrentUser().getUsername());
        dept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        dept.setOperateTime(new Date());
        sysDeptMapper.insertSelective(dept);
        sysLogService.saveDeptLog(null, dept);
    }

    public void update(DeptParam param) {
        BeanValidator.check(param);
        //需要传id，因为可能更新的只是seq，如果没传的话，就会把本身查出来，导致这里返回true，更新失败
        if(checkExist(param.getParentId(), param.getName(), param.getId())) {
            throw new ParamException("同一层级下已经有此名称的部门，无法更新为此部门，否则重复");
        }

        SysDept before = sysDeptMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before, "待更新的部门不存在");

        SysDept after = SysDept.builder().id(param.getId()).name(param.getName()).parentId(param.getParentId())
                .seq(param.getSeq()).remark(param.getRemark()).build();
        after.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()), param.getParentId()));
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperateTime(new Date());

        updateWithChildDept(before, after);
        sysLogService.saveDeptLog(before, after);
    }

    /**
     * 更新的时候需要考虑子部门的层级是否需要更新
     * 子部门更新逻辑：如果before的level和after的level不同：
     * 根据level取出before的所有子部门，将这些子部门的level更新为after的level+before的level之后的数字
     * （因为子部门的子部门也要更新，不能只是+before的id，否则只更新了子部门）
     * 比如level为0.1的部门要改为0，那么0.1.1、0.1.2等则变为0 + .1 = 0.1、0 + .2 = 0.2
     * 需要使用事务
     * @param before
     * @param after
     */
    @Transactional
    public void updateWithChildDept(SysDept before, SysDept after) {
        String newLevelPrefix = after.getLevel();
        String oldLevelPrefix = before.getLevel();
        if (!newLevelPrefix.equals(oldLevelPrefix)) {
            List<SysDept> deptList = sysDeptMapper.getChildDeptListByLevel(oldLevelPrefix);//oldLevelPrefix的所有子部门
            if (CollectionUtils.isNotEmpty(deptList)) {
                for (SysDept dept : deptList) {
                    String level = dept.getLevel();
                    if (level.indexOf(oldLevelPrefix) == 0) {//双重验证，可以没有
                        level = newLevelPrefix + level.substring(oldLevelPrefix.length());
                        dept.setLevel(level);
                    }
                }
                sysDeptMapper.batchUpdateLevel(deptList);
            }
        }
        sysDeptMapper.updateByPrimaryKey(after);//before和after的id是一样的
    }

    private boolean checkExist(Integer parentId, String deptName, Integer deptId) {
        return sysDeptMapper.countByNameAndParentId(parentId, deptName, deptId) > 0;
    }

    /**
     * 得到某个部门id的level
     * @param deptId
     * @return
     */
    private String getLevel(Integer deptId) {
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        if (dept == null) return null;
        return dept.getLevel();
    }

    /**
     * 删除部门
     * @param deptId
     */
    public void delete(int deptId) {
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        Preconditions.checkNotNull(dept, "待删除的部门不存在，删除失败");
        if (sysDeptMapper.countByParentId(dept.getId()) > 0) {
            throw new ParamException("当前部门下面有子部门，删除失败");
        }
        if(sysUserMapper.countByDeptId(dept.getId()) > 0) {
            throw new ParamException("当前部门下面有用户，删除失败");
        }
        sysDeptMapper.deleteByPrimaryKey(deptId);
    }
}
