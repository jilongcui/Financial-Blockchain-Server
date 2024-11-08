package com.xiaohe.financial.asset;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.xiaohe.financial.asset.entity.AssetParam;
import com.xiaohe.financial.asset.entity.TbAsset;

@Repository
@Mapper
public interface AssetMapper {

    /**
     * Add new Asset data.
     * @param asset
     * @return int
     */
    int addAsset(TbAsset asset);

    /**
     * Query Asset data by id.
     * @param id
     * @return TbAsset
     */
    TbAsset queryById(Integer id);

    /**
     * Query Asset data by name.
     * @param assetName
     * @return TbAsset
     */
    TbAsset queryByName(String assetName);

    /**
     *Query Asset data by type.
     * @param assetType
     * @return TbAsset
     */
    
    /** 
     * list asset by page
     **/
    List<TbAsset> listOfAsset(AssetParam assetParam);

}
