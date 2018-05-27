package com.springmvc.controller;

import com.springmvc.annotation.AccessPermission;
import com.springmvc.annotation.PermissionRequired;
import com.springmvc.dto.Admin;
import com.springmvc.dto.Material;
import com.springmvc.dto.PageMode;
import com.springmvc.service.MaterialService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Controller
@RequestMapping("/material")
public class MaterialController {

    @Resource
    MaterialService materialService;

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public PageMode<Material> search(@RequestParam Integer current, @RequestParam Integer limit,
                                     String sortColumn, String sort, String searchKey) {
        return materialService.pageMaterial(current, limit, sortColumn, sort, searchKey);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    @PermissionRequired(AccessPermission.MATERIAL_UPDATE)
    public Material update(@RequestParam Integer materialId, @RequestParam BigDecimal cost) {
        return materialService.updateCost(materialId, cost);
    }
}
