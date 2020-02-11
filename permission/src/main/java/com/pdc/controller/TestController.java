package com.pdc.controller;

import com.pdc.common.ApplicationContextHelper;
import com.pdc.common.JsonData;
import com.pdc.dao.SysAclModuleMapper;
import com.pdc.exception.ParamException;
import com.pdc.exception.PermissionException;
import com.pdc.model.SysAclModule;
import com.pdc.param.TestVo;
import com.pdc.util.BeanValidator;
import com.pdc.util.JsonMapper;
import com.pdc.util.RedisTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/test")
@Slf4j
public class TestController {


    @RequestMapping("/hello.json")
    @ResponseBody
    public JsonData hello() {
        log.info("hello");
        throw new PermissionException("test exception");
        // return JsonData.success("hello, permission");
    }

    @RequestMapping("/validate.json")
    @ResponseBody
    public JsonData validate(TestVo vo) throws ParamException {
        log.info("validate");
        SysAclModuleMapper moduleMapper = ApplicationContextHelper.getBean(SysAclModuleMapper.class);
        SysAclModule module = moduleMapper.selectByPrimaryKey(1);
        log.info(JsonMapper.obj2String(module));
        BeanValidator.check(vo);
        return JsonData.success("test validate");
    }
}
