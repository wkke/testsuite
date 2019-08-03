package com.richway.admin.controller;


import com.richway.admin.common.controller.BaseController;
import com.richway.admin.entity.SysDept;
import com.richway.admin.service.ISysDeptService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * [权限管理] 部门表 前端控制器
 * </p>
 *
 * @author wang chen chen
 * @since 2018-10-23
 */

@Slf4j
@Api(tags = "部门")
@RestController
@RequestMapping("/dept")
public class SysDeptController extends BaseController<SysDept, Integer, ISysDeptService> {

}
