package com.xiaohe.financial.asset;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.xiaohe.financial.asset.entity.TbAsset;

import lombok.extern.log4j.Log4j2;
import node.mgr.test.base.TestBase;

@Log4j2
public class AssetServiceTest extends TestBase {

    @Autowired
    private AssetService assetService;
    // org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'com.xiaohe.financial.asset.AssetServiceTest': Unsatisfied dependency expressed through field 'assetService'; nested exception is org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.xiaohe.financial.asset.AssetService' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}


    @Test
    public void contextLoads() {
        // 这个测试只是为了确认 Spring 上下文可以成功加载
        assertNotNull(assetService);
    }

    @Test
    public void addAsset()
    {
        TbAsset asset = new TbAsset();
        asset.setAssetName("南京大苏宁科技有限公司的采购合同");
        asset.setAssetType("1");
        asset.setCid("DSNUYETWOWEXDADT");
        asset.setTargetCompany("苏宁科技");
        Integer count = assetService.addAsset(asset);
        log.info("count:{}", count);
        Assert.assertTrue(count > 0);
    }

}
