package com.xiaohe.financial.asset.entity;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TbAsset {
    private Integer id;
    private String assetName;
    private String assetType;
    private String cid;
    private String targetCompany;
    private LocalDateTime createTime;
}
