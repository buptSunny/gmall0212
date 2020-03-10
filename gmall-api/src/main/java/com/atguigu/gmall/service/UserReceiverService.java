package com.atguigu.gmall.gmalluser.service;

public interface UserReceiverService {
    void insertUserReceiverService(String memberId, String name, String phoneNumber, String postCode, String province, String city, String region, String detailAddress);

    void deleteUserReceiverSerive(String userReceiverServiceId);
}
