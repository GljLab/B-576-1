package com.interview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.entity.SysUser;

import java.util.Map;

/**
 * 系统用户服务接口
 */
public interface SysUserService extends IService<SysUser> {
    
    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password);
    
    /**
     * 用户注册
     */
    boolean register(SysUser user);
    
    /**
     * 根据用户名查询
     */
    SysUser getByUsername(String username);
    
    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 分页查询用户
     */
    Page<SysUser> pageUsers(Integer pageNum, Integer pageSize, String keyword, String role);
}
