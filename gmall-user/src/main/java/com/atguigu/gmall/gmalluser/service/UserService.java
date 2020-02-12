package com.atguigu.gmall.gmalluser.service;

import com.atguigu.gmall.gmalluser.bean.UmsMember;
import com.atguigu.gmall.gmalluser.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    void updateUserByMemberId(String memberId, String username);

    void insertUser(String username, String password, String nickname, String phone, int status);

    void deleteUserByMemberId(String memberId);
}
