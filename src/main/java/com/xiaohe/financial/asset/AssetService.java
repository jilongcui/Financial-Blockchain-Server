package com.xiaohe.financial.asset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xiaohe.financial.asset.entity.TbAsset;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class AssetService {

    @Autowired
    private AssetMapper assetMapper;

    /** 
     * Add Asset entity
     * @param asset
     * @return Integer
     */
    public Integer addAsset(TbAsset asset) {
        return assetMapper.addAsset(asset);
    }
}
