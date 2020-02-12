package com.atguigu.gmall.gmalluser.mapper;

import com.atguigu.gmall.gmalluser.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<UmsMember> {
    List<UmsMember> selectAllUser();
    //通用mapper
}
