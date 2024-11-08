package com.xiaohe.financial.asset.entity;

import java.time.LocalDateTime;

import com.webank.webase.node.mgr.base.entity.BaseQueryParam;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AssetParam extends BaseQueryParam {
    private Integer id;
    private String assetName;
    private String assetType;
    private String cid;
    private String targetCompany;
}
