package com.yangliu.oauth2.pojo;

import lombok.Data;

import javax.persistence.*;

/**
 * @author YL
 * @date 2023/07/24
 **/
@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "passwd")
    private String passwd;

    @Column(name = "user_role")
    private String userRole;

}
