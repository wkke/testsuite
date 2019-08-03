package com.richway.admin.mapper;

import com.richway.admin.common.mapper.BaseMapper;
import com.richway.admin.entity.SysRole;

import java.util.Set;

public interface SysRoleMapper extends BaseMapper<SysRole, Integer> {

    Set<SysRole> selectByUserName(String username);

}