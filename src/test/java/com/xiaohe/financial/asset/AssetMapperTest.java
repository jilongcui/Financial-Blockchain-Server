package com.xiaohe.financial.asset;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.xiaohe.financial.asset.entity.AssetParam;
import com.xiaohe.financial.asset.entity.TbAsset;

import lombok.extern.log4j.Log4j2;
import node.mgr.test.base.TestBase;

@Log4j2
@Transactional // 确保每个测试之后回滚，以防止污染数据库状态
public class AssetMapperTest extends TestBase {

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate; // 用于直接访问数据库

    
    @Test
    public void contextLoads() {
        // 这个测试只是为了确认 Spring 上下文可以成功加载
        assertNotNull(assetMapper);
    }

    @Test
    public void addAssetTest() {
        TbAsset asset = new TbAsset();
        asset.setAssetName("南京大漠大科技有限公司的采购合同");
        asset.setAssetType("1");
        asset.setCid("DEIUYETWOWEXDADT8Y7");
        asset.setTargetCompany("南京大漠");
        asset.setCreateTime(LocalDateTime.now());
        assetMapper.addAsset(asset);

        // 使用 SQL 查询确认插入是否成功（或通过返回主键自行设计验证）

        // TbAsset inserted = sqlSessionTemplate.selectOne(
        //     "SELECT * FROM tb_asset WHERE asset_name = #{assetName}", asset
        // );

        // 使用 SQL 查询确认插入是否成功（或通过返回主键自行设计验证）
        Map<String, Object> params = new HashMap<>();
        params.put("assetName", "南京大漠大科技有限公司的采购合同");

        TbAsset inserted = sqlSessionTemplate.selectOne(
            "com.xiaohe.financial.asset.AssetMapper.selectByName",
            params
        );

        Assertions.assertNotNull(inserted, "Inserted asset should not be null");

        // Assertions.assertNotNull(inserted, "Inserted asset should not be null");
        // Assertions.assertEquals(newAsset.getAssetName(), inserted.getName());
        // Assertions.assertEquals(newAsset.getCid(), inserted.getCid());
        
        // 如果asset表有自增ID，可以确认主键生成：
        // Assertions.assertNotNull(inserted.getId(), "ID should be generated by database");
    }

    @Test
    public void listOfAsset() {
        AssetParam assetParam = new AssetParam();
        assetParam.setAssetType("1");
        assetParam.setPageSize(10);
        assetParam.setStart(0);
        List<TbAsset> assetList = assetMapper.listOfAsset(assetParam);
        log.info("assetList:{}", assetList);
        Assertions.assertTrue(assetList.size() > 0);
    }

    @Test
    public void listOfAssetOfNone() {
        AssetParam assetParam = new AssetParam();
        assetParam.setAssetType("2");
        assetParam.setPageSize(10);
        assetParam.setStart(0);
        List<TbAsset> assetList = assetMapper.listOfAsset(assetParam);
        Assertions.assertTrue(assetList.size() == 0);
    }

}
