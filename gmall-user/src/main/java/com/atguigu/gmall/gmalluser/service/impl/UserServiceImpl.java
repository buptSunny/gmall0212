package com.atguigu.gmall.gmalluser.service.impl;

import com.atguigu.gmall.gmalluser.bean.UmsMember;
import com.atguigu.gmall.gmalluser.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.gmalluser.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.gmalluser.mapper.UserMapper;
import com.atguigu.gmall.gmalluser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMemberList =  userMapper.selectAll();
        return umsMemberList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        Example e = new Example(UmsMemberReceiveAddress.class);
        e.createCriteria().andEqualTo("memberId",memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(e);
        return umsMemberReceiveAddresses;
    }

    @Override
    public void updateUserByMemberId(String memberId, String username) {
        UmsMember umsMember = new UmsMember();
        umsMember.setId(memberId);
        umsMember.setUsername(username);
        userMapper.updateByPrimaryKeySelective(umsMember);
    }

    @Override
    public void insertUser(String username, String password, String nickname, String phone, int status) {
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(username);
        umsMember.setPassword(password);
        umsMember.setNickname(nickname);
        umsMember.setPhone(phone);
        umsMember.setStatus(status);
        userMapper.insertSelective(umsMember);
    }

    @Override
    public void deleteUserByMemberId(String memberId) {
        userMapper.deleteByPrimaryKey(memberId);
    }
}
