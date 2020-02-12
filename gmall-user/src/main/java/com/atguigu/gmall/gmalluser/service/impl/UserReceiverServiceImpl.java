package com.atguigu.gmall.gmalluser.service.impl;


import com.atguigu.gmall.gmalluser.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.gmalluser.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.gmalluser.service.UserReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserReceiverServiceImpl implements UserReceiverService {
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;


    @Override
    public void insertUserReceiverService(String memberId, String name, String phoneNumber, String postCode, String province, String city, String region, String detailAddress) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        umsMemberReceiveAddress.setName(name);
        umsMemberReceiveAddress.setPhoneNumber(phoneNumber);
        umsMemberReceiveAddress.setPostCode(postCode);
        umsMemberReceiveAddress.setProvince(province);
        umsMemberReceiveAddress.setCity(city);
        umsMemberReceiveAddress.setRegion(region);
        umsMemberReceiveAddress.setDetailAddress(detailAddress);
        umsMemberReceiveAddressMapper.insert(umsMemberReceiveAddress);
    }

    @Override
    public void deleteUserReceiverSerive(String userReceiverServiceId) {
        umsMemberReceiveAddressMapper.deleteByPrimaryKey(userReceiverServiceId);
    }


}
