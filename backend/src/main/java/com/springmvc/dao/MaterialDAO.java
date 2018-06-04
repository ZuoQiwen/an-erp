package com.springmvc.dao;

import com.springmvc.dto.Material;
import com.springmvc.dto.MaterialLack;
import com.springmvc.dto.MaterialStockCostRecord;
import com.springmvc.dto.MaterialStockRecord;
import com.springmvc.pojo.MaterialQuery;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MaterialDAO {
    /**
     *  根据指定的条件获取数据库记录数
     *
     * @param example
     */
    long countByExample(MaterialQuery example);

    /**
     *  根据指定的条件删除数据库符合条件的记录
     *
     * @param example
     */
    int deleteByExample(MaterialQuery example);

    /**
     *  根据主键删除数据库的记录
     *
     * @param materialId
     */
    int deleteByPrimaryKey(Integer materialId);

    /**
     *  新写入数据库记录
     *
     * @param record
     */
    int insert(Material record);

    /**
     *  动态字段,写入数据库记录
     *
     * @param record
     */
    int insertSelective(Material record);

    /**
     *  根据指定的条件查询符合条件的数据库记录
     *
     * @param example
     */
    List<Material> selectByExample(MaterialQuery example);

    /**
     *  根据指定主键获取一条数据库记录
     *
     * @param materialId
     */
    Material selectByPrimaryKey(Integer materialId);

    /**
     *  动态根据指定的条件来更新符合条件的数据库记录
     *
     * @param record
     * @param example
     */
    int updateByExampleSelective(@Param("record") Material record, @Param("example") MaterialQuery example);

    /**
     *  根据指定的条件来更新符合条件的数据库记录
     *
     * @param record
     * @param example
     */
    int updateByExample(@Param("record") Material record, @Param("example") MaterialQuery example);

    /**
     *  动态字段,根据主键来更新符合条件的数据库记录
     *
     * @param record
     */
    int updateByPrimaryKeySelective(Material record);

    /**
     *  根据主键来更新符合条件的数据库记录
     *
     * @param record
     */
    int updateByPrimaryKey(Material record);

    /*
     * 获取一个带有类别名称的数据库记录
     *
     * @param example
     */
    List<Material> selectWithCategoryNameByExample(MaterialQuery example);

    /*
     *   根据指定的条件查询符合条件的数据库记录（物料库存）
     *
     * @param example 查询条件
     * @return 一页记录
     */
    List<MaterialStockRecord> selectWithStockByExample(MaterialQuery example);

    /**
     *   根据指定的条件统计符合条件的数据库记录（物料库存）
     *
     * @param example 查询条件
     * @return 一页记录
     */
    MaterialStockRecord statisticsWithStockByExample(MaterialQuery example);

    /**
     *
     *  搜索物料的数量
     * @param example
     */
    List<MaterialStockCostRecord> selectWithStockCostByExample(MaterialQuery example);

    /**
     *   根据指定的条件查询符合条件的数据库记录（物料缺料情况）
     *   库存，待领料，带出库
     * @param example 查询条件
     * @return 一页记录
     */
    List<MaterialLack> selectWithLackByExample(MaterialQuery example);

    /**
     *
     *  统计物料的数量
     * @param example
     */
    MaterialStockCostRecord statisticsWithStockCostByExample(MaterialQuery example);
}