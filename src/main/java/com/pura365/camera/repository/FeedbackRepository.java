package com.pura365.camera.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pura365.camera.domain.Feedback;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeedbackRepository extends BaseMapper<Feedback> {
}