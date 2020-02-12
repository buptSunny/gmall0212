package com.atguigu.gmall.gmalluser.controller;

import com.atguigu.gmall.gmalluser.bean.UmsMember;
import com.atguigu.gmall.gmalluser.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.gmalluser.service.UserService;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "hello zcx";
    }
    @RequestMapping("insertUser")
    @ResponseBody
    public String insertUser(String username,String password,String nickname,String phone,int status){
        userService.insertUser(username,password,nickname,phone,status);
        return "Ok,添加用户成功";
    }

    @RequestMapping("deleteUserByMemberId")
    @ResponseBody
    public String deleteUserByMemberId(String memberId){
        userService.deleteUserByMemberId(memberId);
        return "Ok,删除用户成功";
    }


    @RequestMapping("updateUser")
    @ResponseBody
    public void updateUserByMemberId(String memberId, String username){
        userService.updateUserByMemberId(memberId,username);
    }

    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){
        List<UmsMember> umsMembers= userService.getAllUser();
        return umsMembers;
    }

    @RequestMapping("getReceiveAddressByMemberId")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId){
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses =   userService.getReceiveAddressByMemberId(memberId);
        return umsMemberReceiveAddresses;
    }

}
