package com.springmvc.controller;

import com.springmvc.annotation.AccessPermission;
import com.springmvc.annotation.PermissionRequired;
import com.springmvc.dto.*;
import com.springmvc.service.ProductInstockService;
import com.springmvc.utils.ParamUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import java.util.List;

import static com.springmvc.utils.ParamUtils.toIntList;

@Controller
@RequestMapping("/product-instock")
public class ProductInstockController {

    @Resource
    ProductInstockService productInstockService;

    @RequestMapping(value = "/getStatistics", method = RequestMethod.POST)
    @ResponseBody
    public StatisticsMode getStatistics() {
        return productInstockService.getStatistics();
    }

    @RequestMapping(value="/audit",method = RequestMethod.POST)
    @ResponseBody
    @PermissionRequired(AccessPermission.PRODUCT_INSTOCK_AUDIT)
    public String audit(String idList){
        productInstockService.audit(ParamUtils.toIntList(idList));
        return "success";
    }

    @RequestMapping(value="/unaudit",method = RequestMethod.POST)
    @ResponseBody
    @PermissionRequired(AccessPermission.PRODUCT_INSTOCK_AUDIT)
    public String unaudit(String idList){
        productInstockService.unaudit(ParamUtils.toIntList(idList));
        return "success";
    }

    @RequestMapping(value = "/finish", method = RequestMethod.POST)
    @ResponseBody
    @PermissionRequired(AccessPermission.PRODUCT_INSTOCK_FINISH)
    public String finish(@RequestParam Integer billId) {
        productInstockService.finish(billId);
        return "success";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @PermissionRequired(AccessPermission.PRODUCT_INSTOCK_ADD)
    public ProductInstockBill add(@RequestParam Integer fromPrincipal, @RequestParam Integer productSource,
                                  @RequestParam String remark, @RequestParam String productList) {
        return productInstockService.addProductInsockBill(fromPrincipal, productSource, remark,
                ParamUtils.jsonToList(productList, ProductInstockBillProduct.class));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    @PermissionRequired(AccessPermission.PRODUCT_INSTOCK_UPDATE)
    public ProductInstockBill update(@RequestParam Integer billId, @RequestParam Integer fromPrincipal, @RequestParam Integer productSource,
                                     @RequestParam String remark, @RequestParam String productList) {
        return productInstockService.updateProductInsockBill(billId, fromPrincipal,  productSource, remark,
                ParamUtils.jsonToList(productList,ProductInstockBillProduct.class));
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public PageMode<ProductInstockBill> search(@RequestParam Integer current, @RequestParam Integer limit,
                                               String sortColumn, String sort, String searchKey, Integer state,
                                               Long beginTime, Long endTime) {
        return productInstockService.pageProductInstockBill(current, limit, sortColumn, sort, searchKey, state,
                ParamUtils.toDate(beginTime), ParamUtils.toDate(endTime));
    }

    @RequestMapping(value = "/getById", method = RequestMethod.POST)
    @ResponseBody
    public ProductInstockBill getById(@RequestParam Integer billId) {
        return productInstockService.getProductInstockBillById(billId);
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @ResponseBody
    @PermissionRequired(AccessPermission.PRODUCT_INSTOCK_REMOVE)
    public String remove(@RequestParam String idList) {
        productInstockService.removeProductBill(ParamUtils.toIntList(idList));
        return "success";
    }
}
