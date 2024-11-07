package com.xiaohe.financial.asset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xiaohe.financial.asset.entity.TbAsset;

@RestController
@RequestMapping(value = "asset")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @PostMapping()
    public Integer addAsset(TbAsset asset) {
        return assetService.addAsset(asset);
    }

}
