package com.pura365.camera.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pura365.camera.domain.Device;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceRepository extends BaseMapper<Device> {
    // 如需自定义 SQL，可在这里增加方法并配合 XML 或注解实现
}
