package com.yangliu.oauth2.repo;

import com.yangliu.oauth2.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author YL
 * @date 2023/07/25
 **/
@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    User queryByUserName(String userName);

}
