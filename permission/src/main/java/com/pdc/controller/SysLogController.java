package com.pdc.controller;

import com.pdc.beans.PageQuery;
import com.pdc.common.JsonData;
import com.pdc.param.SearchLogParam;
import com.pdc.service.SysLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@Controller
@RequestMapping("/sys/log")
public class SysLogController {

    @Resource
    private SysLogService sysLogService;

    /**
     * 返回权限操作记录页面
     * @return
     */
    @RequestMapping("/log.page")
    public ModelAndView page() {
        return new ModelAndView("log");
    }

    /**
     * 将指定log的afterSeq还原到beforeSeq
     * 只针对更新进行还原，新增和删除不还原
     * @param id logId
     * @return
     */
    @RequestMapping("/recover.json")
    @ResponseBody
    public JsonData recover(@RequestParam("id") int id) {
        sysLogService.recover(id);
        return JsonData.success();
    }

    /**
     * 分页查询记录
     * @param searchLogParam
     * @param page
     * @return
     */
    @RequestMapping("/page.json")
    @ResponseBody
    public JsonData searchPage(SearchLogParam searchLogParam, PageQuery page) {
        return JsonData.success(sysLogService.searchPageList(searchLogParam, page));
    }
}
