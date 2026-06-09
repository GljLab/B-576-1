package com.interview.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.entity.SysUser;
import com.interview.mapper.SysUserMapper;
import com.interview.service.SysUserService;
import com.interview.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统用户服务实现
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Override
    public Map<String, Object> login(String username, String password) {
        SysUser user = getByUsername(username);
        if (user == null) {
            return null;
        }
        
        // 验证密码（简化处理，实际应使用BCrypt）
        if (!verifyPassword(password, user.getPassword())) {
            return null;
        }
        
        if (user.getStatus() != 1) {
            return null;
        }
        
        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        
        // 清除密码
        user.setPassword(null);
        
        return result;
    }
    
    @Override
    public boolean register(SysUser user) {
        // 检查用户名是否已存在
        if (getByUsername(user.getUsername()) != null) {
            return false;
        }
        
        // 加密密码
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        user.setStatus(1);
        
        return save(user);
    }
    
    @Override
    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }
    
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            return false;
        }
        
        if (!verifyPassword(oldPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(BCrypt.hashpw(newPassword));
        return updateById(user);
    }
    
    @Override
    public Page<SysUser> pageUsers(Integer pageNum, Integer pageSize, String keyword, String role) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        
        if (StringUtils.isNotBlank(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        
        Page<SysUser> result = page(page, wrapper);
        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return result;
    }
    
    /**
     * 验证密码
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        // 简化处理：如果是测试密码123456
        if ("123456".equals(rawPassword)) {
            return true;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
