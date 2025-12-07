package com.pura365.camera.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pura365.camera.domain.DeviceMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMessageRepository extends BaseMapper<DeviceMessage> {
    // 如需按设备/未读状态等条件查询，可在 Service 中通过 QueryWrapper 实现
}
