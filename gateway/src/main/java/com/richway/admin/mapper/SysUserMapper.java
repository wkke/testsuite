package com.richway.admin.mapper;

import com.richway.admin.common.mapper.BaseMapper;
import com.richway.admin.entity.SysUser;

public interface SysUserMapper extends BaseMapper<SysUser, String> {

    SysUser selectByUserName(String username);

}