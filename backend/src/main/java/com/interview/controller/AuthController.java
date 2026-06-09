package com.interview.controller;

import com.interview.common.Result;
import com.interview.entity.SysUser;
import com.interview.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录、注册等接口")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private SysUserService userService;
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        
        Map<String, Object> result = userService.login(username, password);
        
        if (result == null) {
            return Result.error("用户名或密码错误");
        }
        
        return Result.success("登录成功", result);
    }
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody SysUser user) {
        if (userService.register(user)) {
            return Result.success("注册成功", null);
        }
        return Result.error("注册失败，用户名可能已存在");
    }
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<SysUser> getUserInfo(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.unauthorized();
        }
        
        SysUser user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        
        return Result.success(user);
    }
    
    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestAttribute("userId") Long userId,
                                       @RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("请输入原密码和新密码");
        }
        
        if (userService.changePassword(userId, oldPassword, newPassword)) {
            return Result.success("密码修改成功", null);
        }
        
        return Result.error("原密码错误");
    }
    
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功", null);
    }
}
