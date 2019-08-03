package com.richway.admin.service.impl;

import com.richway.admin.common.service.impl.BaseServiceImpl;
import com.richway.admin.entity.SysRole;
import com.richway.admin.mapper.SysRoleMapper;
import com.richway.admin.service.ISysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * [权限管理] 角色表 服务实现类
 * </p>
 *
 * @author wang chen chen
 * @since 2018-10-23
 */

@Slf4j
@Service
public class SysRoleServiceImpl extends BaseServiceImpl<SysRole, Integer, SysRoleMapper> implements ISysRoleService {

    @Override
    public Set<SysRole> selectByPrimaryKeyCollection(List<Integer> ids) {
        return null;
    }

    @Override
    public Set<SysRole> selectByUserName(String username) {
        return baseMapper.selectByUserName(username);
    }

}
