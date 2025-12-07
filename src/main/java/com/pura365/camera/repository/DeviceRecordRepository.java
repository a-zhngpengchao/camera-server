package com.pura365.camera.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pura365.camera.domain.DeviceRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceRecordRepository extends BaseMapper<DeviceRecord> {
}