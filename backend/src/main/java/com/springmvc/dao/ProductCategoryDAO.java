package com.springmvc.dao;

import com.springmvc.dto.ProductCategory;
import com.springmvc.pojo.ProductCategoryQuery;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProductCategoryDAO {
    /**
     *  根据指定的条件获取数据库记录数
     *
     * @param example
     */
    long countByExample(ProductCategoryQuery example);

    /**
     *  根据指定的条件删除数据库符合条件的记录
     *
     * @param example
     */
    int deleteByExample(ProductCategoryQuery example);

    /**
     *  根据主键删除数据库的记录
     *
     * @param categoryId
     */
    int deleteByPrimaryKey(Integer categoryId);

    /**
     *  新写入数据库记录
     *
     * @param record
     */
    int insert(ProductCategory record);

    /**
     *  动态字段,写入数据库记录
     *
     * @param record
     */
    int insertSelective(ProductCategory record);

    /**
     *  根据指定的条件查询符合条件的数据库记录
     *
     * @param example
     */
    List<ProductCategory> selectByExample(ProductCategoryQuery example);

    /**
     *  根据指定主键获取一条数据库记录
     *
     * @param categoryId
     */
    ProductCategory selectByPrimaryKey(Integer categoryId);

    /**
     *  动态根据指定的条件来更新符合条件的数据库记录
     *
     * @param record
     * @param example
     */
    int updateByExampleSelective(@Param("record") ProductCategory record, @Param("example") ProductCategoryQuery example);

    /**
     *  根据指定的条件来更新符合条件的数据库记录
     *
     * @param record
     * @param example
     */
    int updateByExample(@Param("record") ProductCategory record, @Param("example") ProductCategoryQuery example);

    /**
     *  动态字段,根据主键来更新符合条件的数据库记录
     *
     * @param record
     */
    int updateByPrimaryKeySelective(ProductCategory record);

    /**
     *  根据主键来更新符合条件的数据库记录
     *
     * @param record
     */
    int updateByPrimaryKey(ProductCategory record);
}