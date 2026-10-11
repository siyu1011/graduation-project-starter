package com.example.starter.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.starter.auth.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserMapper mapper;
    public UserService(UserMapper mapper) { this.mapper = mapper; }
    @Override public UserDetails loadUserByUsername(String username) {
        var user = mapper.selectOne(new QueryWrapper<UserEntity>().eq("username", username));
        if (user == null) throw new UsernameNotFoundException("账号或密码错误");
        return new SessionUser(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole(), user.getPasswordHash(), user.getEnabled() == 1);
    }
}
