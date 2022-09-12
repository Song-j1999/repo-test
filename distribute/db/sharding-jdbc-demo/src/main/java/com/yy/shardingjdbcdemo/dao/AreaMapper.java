package com.yy.shardingjdbcdemo.dao;

import java.util.List;

import com.yy.shardingjdbcdemo.model.Area;
import com.yy.shardingjdbcdemo.model.AreaExample;
import org.apache.ibatis.annotations.Param;

public interface AreaMapper {

    long countByExample(AreaExample example);


    int deleteByExample(AreaExample example);


    int deleteByPrimaryKey(Integer id);


    int insert(Area record);


    int insertSelective(Area record);


    List<Area> selectByExample(AreaExample example);


    Area selectByPrimaryKey(Integer id);


    int updateByExampleSelective(@Param("record") Area record, @Param("example") AreaExample example);

    int updateByExample(@Param("record") Area record, @Param("example") AreaExample example);


    int updateByPrimaryKeySelective(Area record);


    int updateByPrimaryKey(Area record);
}