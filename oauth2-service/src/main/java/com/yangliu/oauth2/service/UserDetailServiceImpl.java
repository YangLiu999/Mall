package com.yangliu.oauth2.service;

import com.yangliu.oauth2.pojo.User;
import com.yangliu.oauth2.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author YL
 * @date 2023/07/25
 **/
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.queryByUserName(username);
        if (user != null){
            return new org.springframework.security.core.userdetails.User(user.getUserName()
            ,user.getPasswd(), AuthorityUtils.createAuthorityList(user.getPasswd()));
        }else {
            throw  new UsernameNotFoundException("user not found");
        }
    }
}
