package com.cm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cm.dto.AccessoryInboundDTO;
import com.cm.entity.Accessory;
import com.cm.vo.AccessoryVO;

public interface AccessoryService extends IService<Accessory> {
    void inbound(AccessoryInboundDTO dto, String operator);
    AccessoryVO findByBarcode(String barcode);
}