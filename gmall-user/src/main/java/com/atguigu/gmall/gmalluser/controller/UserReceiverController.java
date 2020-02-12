package com.atguigu.gmall.gmalluser.controller;

import com.atguigu.gmall.gmalluser.service.UserReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserReceiverController {
    @Autowired
    UserReceiverService userReceiverService;

    @RequestMapping("insertUserReceiverService")
    @ResponseBody
    public String insertUserReceiverService(String memberId,String name,String phoneNumber,String postCode,String province,String city, String region, String detailAddress){
        userReceiverService.insertUserReceiverService(memberId,name,phoneNumber,postCode,province,city,region,detailAddress);
        return "用户地址添加成功";
    }

    @RequestMapping("deleteUserReceiverService")
    @ResponseBody
    public String deleteUserReceiverSerive(String UserReceiverServiceId){
        userReceiverService.deleteUserReceiverSerive(UserReceiverServiceId);
        return "用户地址删除成功";
    }

}
