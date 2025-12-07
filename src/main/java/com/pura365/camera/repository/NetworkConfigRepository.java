package com.pura365.camera.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pura365.camera.domain.NetworkConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NetworkConfigRepository extends BaseMapper<NetworkConfig> {
    // 后续如需复杂查询可使用 QueryWrapper 在 Service 层实现
}
