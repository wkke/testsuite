package com.richway.admin.controller;


import com.richway.admin.common.controller.BaseController;
import com.richway.admin.entity.SysRole;
import com.richway.admin.service.ISysRoleService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * [权限管理] 角色表 前端控制器
 * </p>
 *
 * @author wang chen chen
 * @since 2018-10-23
 */

@Slf4j
@Api(tags = "角色")
@RestController
@RequestMapping("/role")
public class SysRoleController extends BaseController<SysRole, Integer, ISysRoleService> {

}
