package com.springmvc.service;

import com.springmvc.dao.*;
import com.springmvc.dao.MaterialOutstockBillMaterialDAO;
import com.springmvc.dto.*;
import com.springmvc.exception.BadRequestException;
import com.springmvc.pojo.*;
import com.springmvc.utils.ParamUtils;
import com.springmvc.utils.RequestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;


@Service("MaterialOutstockService")
@Transactional

public class MaterialOutstockService extends BaseService {

    @Resource
    private MaterialDAO materialDAO;

    @Resource
    private WarehouseDAO warehouseDAO;

    @Resource
    private AdminDAO adminDAO;

    @Resource
    private MaterialOutstockBillDAO materialOutstockBillDAO;

    @Resource
    private MaterialOutstockBillMaterialDAO materialOutstockBillMaterialDAO;

    @Resource
    private DrawMaterialBillDAO drawMaterialBillDAO;

    /**
     * 获取统计信息
     *
     * @return
     */
    public StatisticsMode getStatistics() {
        List<StatisticsData> preWeek = new ArrayList<StatisticsData>();
        for (int i = -6; i <= 0; ++i) {
            MaterialOutstockBillQuery materialOutstockBillQuery = new MaterialOutstockBillQuery();
            Date beginTime = ParamUtils.getCertainDate(i);
            Date endTime = ParamUtils.getCertainDate(i + 1);
            materialOutstockBillQuery.or()
                    .andBillTimeGreaterThanOrEqualTo(beginTime)
                    .andBillTimeLessThan(endTime);
            preWeek.add(new StatisticsData(ParamUtils.dateConvert(beginTime, "yyyy-MM-dd"),
                    materialOutstockBillDAO.countByExample(materialOutstockBillQuery)));
        }

        List<StatisticsData> history = new ArrayList<StatisticsData>();
        for (int i = 1; i <= 3; ++i) {
            MaterialOutstockBillQuery materialOutstockBillQuery = new MaterialOutstockBillQuery();
            materialOutstockBillQuery.or().andBillStateEqualTo(i);
            history.add(new StatisticsData("" + i, materialOutstockBillDAO.countByExample(materialOutstockBillQuery)));
        }

        return new StatisticsMode(preWeek, history);
    }

    /**
     * 添加物料出库单
     *
     * 将主表信息保存：material_outstock_bill
     * 将关联的从表信息保存：material_outstock_bill_product
     * 添加日志信息：LogType.MATERIAL_OUTSTOCK, Operate.ADD
     * 若为退料入库，为相应退料单设置仓库负责人信息
     *
     * @param toPrincipal
     * @param materialWhereabouts
     * @param relatedBill
     * @param remark
     * @param materialList
     * @return
     */
    public MaterialOutstockBill addMaterialOutstockBill(Integer toPrincipal, Integer materialWhereabouts, Integer relatedBill,
                                                        String remark, List<MaterialOutstockBillMaterial> materialList) {
        Admin loginAdmin = RequestUtils.getLoginAdminFromCache();

        MaterialOutstockBill materialOutstockBill = new MaterialOutstockBill();
        materialOutstockBill.setBillNo("MO" + ParamUtils.dateConvert(new Date(), "yyMMddHHmmssSSS"));
        materialOutstockBill.setToPrincipal(toPrincipal);
        materialOutstockBill.setWarehousePrincipal(loginAdmin.getAdminId());
        materialOutstockBill.setBillTime(new Date());
        materialOutstockBill.setMaterialWhereabouts(materialWhereabouts);
        if (materialWhereabouts.equals(1)) {
            materialOutstockBill.setRelatedBill(relatedBill);
        }
        materialOutstockBill.setBillState(1);
        materialOutstockBill.setRemark(remark);
        materialOutstockBill.setCreateAt(new Date());
        materialOutstockBill.setCreateBy(loginAdmin.getAdminId());
        materialOutstockBill.setUpdateAt(new Date());
        materialOutstockBill.setUpdateBy(loginAdmin.getAdminId());
        materialOutstockBillDAO.insertSelective(materialOutstockBill);

        // 检查库存数量
        checkStockQuantity(materialList);
        // 将关联的从表信息保存
        for (MaterialOutstockBillMaterial material: materialList) {
            material.setBillId(materialOutstockBill.getBillId());
            materialOutstockBillMaterialDAO.insert(material);
        }

        addLog(LogType.MATERIAL_OUTSTOCK, Operate.ADD, materialOutstockBill.getBillId());

        // 若为领料入库，为相应领料单设置仓库负责人信息
        if (materialOutstockBill.getMaterialWhereabouts().equals(1)) {
            DrawMaterialBill drawMaterialBill = drawMaterialBillDAO.selectByPrimaryKey(materialOutstockBill.getRelatedBill());
            if (drawMaterialBill == null) {
                throw new BadRequestException(DRAW_MATERIAL_BILL_NOT_EXIST);
            }
            if (!drawMaterialBill.getBillState().equals(2)) {
                throw new BadRequestException(DRAW_MATERIAL_BILL_NOT_AUDIT);
            }
            drawMaterialBill = new DrawMaterialBill();
            drawMaterialBill.setBillId(materialOutstockBill.getRelatedBill());
            drawMaterialBill.setWarehousePrincipal(materialOutstockBill.getWarehousePrincipal());
            drawMaterialBillDAO.updateByPrimaryKeySelective(drawMaterialBill);
        }

        return getMaterialOutstockBillById(materialOutstockBill.getBillId());
    }

    /**
     * 更新物料出库单
     *
     * 进行必要的检查：是否为待审核状态
     * 更新主表信息：material_outstock_bill
     * 更新关联的从表信息：material_outstock_bill_material
     * 添加日志信息：LogType.MATERIAL_OUTSTOCK, Operate.UPDATE
     *
     * @param billId
     * @param toPrincipal
     * @param materialWhereabouts
     * @param remark
     * @param materialList
     * @return
     */
    public MaterialOutstockBill updateMaterialOutstockBill(Integer billId, Integer toPrincipal, Integer materialWhereabouts, Integer relatedBill,
                                                           String remark, List<MaterialOutstockBillMaterial> materialList) {

        checkBillState(Collections.singletonList(billId), 1);
        Admin loginAdmin = RequestUtils.getLoginAdminFromCache();

        MaterialOutstockBill materialOutstockBill = new MaterialOutstockBill();
        materialOutstockBill.setBillId(billId);
        materialOutstockBill.setToPrincipal(toPrincipal);
//        materialOutstockBill.setMaterialWhereabouts(materialWhereabouts);
//        materialOutstockBill.setRelatedBill(relatedBill);
        materialOutstockBill.setRemark(remark);
        materialOutstockBill.setUpdateAt(new Date());
        materialOutstockBill.setUpdateBy(loginAdmin.getAdminId());
        materialOutstockBillDAO.updateByPrimaryKeySelective(materialOutstockBill);

        // 先删除关联的从表信息
        MaterialOutstockBillMaterialQuery materialOutstockBillMaterialQuery = new MaterialOutstockBillMaterialQuery();
        materialOutstockBillMaterialQuery.or().andBillIdEqualTo(materialOutstockBill.getBillId());
        materialOutstockBillMaterialDAO.deleteByExample(materialOutstockBillMaterialQuery);
        // 检查库存数量
        checkStockQuantity(materialList);
        // 再将关联的从表信息保存
        for (MaterialOutstockBillMaterial material : materialList){
            material.setBillId(materialOutstockBill.getBillId());
            materialOutstockBillMaterialDAO.insert(material);
        }
        addLog(LogType.MATERIAL_OUTSTOCK, Operate.UPDATE, materialOutstockBill.getBillId());
        return getMaterialOutstockBillById(materialOutstockBill.getBillId());
    }

    /**
     * 查询物料出库单信息（单个）
     *
     * @param billId 单据编号
     * @return
     */
    public MaterialOutstockBill getMaterialOutstockBillById(int billId) {
        MaterialOutstockBill materialOutstockBill = materialOutstockBillDAO.selectByPrimaryKey(billId);

        String fromPrincipalName = adminDAO.selectByPrimaryKey(materialOutstockBill.getToPrincipal()).getTrueName();
        materialOutstockBill.setToPrincipalName(fromPrincipalName);
        String warehousePrincipalName = adminDAO.selectByPrimaryKey(materialOutstockBill.getWarehousePrincipal()).getTrueName();
        materialOutstockBill.setWarehousePrincipalName(warehousePrincipalName);

        Integer i = materialOutstockBill.getBillState();
        if (i > 1) {
            String auditName = adminDAO.selectByPrimaryKey(materialOutstockBill.getAuditBy()).getTrueName();
            materialOutstockBill.setAuditName(auditName);
        }
        if (i > 2) {
            String finishName = adminDAO.selectByPrimaryKey(materialOutstockBill.getFinishBy()).getTrueName();
            materialOutstockBill.setFinishName(finishName);
        }

        if (materialOutstockBill.getMaterialWhereabouts().equals(1)) {
            String relatedBillNo = drawMaterialBillDAO.selectByPrimaryKey(materialOutstockBill.getRelatedBill()).getBillNo();
            materialOutstockBill.setRelatedBillNo(relatedBillNo);
        }

        MaterialOutstockBillMaterialQuery materialOutstockBillMaterialQuery = new MaterialOutstockBillMaterialQuery();
        MaterialOutstockBillMaterialQuery.Criteria criteria = materialOutstockBillMaterialQuery.or();
        criteria.andBillIdEqualTo(materialOutstockBill.getBillId());
        List<MaterialOutstockBillMaterial> result = materialOutstockBillMaterialDAO.selectByExample(materialOutstockBillMaterialQuery);
        for(MaterialOutstockBillMaterial item: result) {
            Material material = materialDAO.selectByPrimaryKey(item.getMaterialId());
            if (material != null) {
                item.setMaterialNo(material.getMaterialNo());
                item.setMaterialName(material.getMaterialName());
            }
            Admin principal = adminDAO.selectByPrimaryKey(item.getPrincipal());
            if (principal != null) {
                item.setPrincipalName(principal.getTrueName());
            }
            Warehouse warehouse = warehouseDAO.selectByPrimaryKey(item.getWarehouse());
            if (warehouse != null) {
                item.setWarehouseName(warehouse.getWarehouseName());
            }
        }
        materialOutstockBill.setMaterialList(result);
        return materialOutstockBill;
    }

    /**
     * 查询物料入库单信息（分页）
     *
     * 将主表信息取出：（同时包含总记录数）
     * 搜索字段：编号、领料人、仓库负责人
     * 筛选字段：状态
     *
     * @param current 当前位置
     * @param limit 一次读取长度
     * @param sortColumn 按哪一列排序
     * @param sort  排序方式 升序 降序
     * @param searchKey 关键字查找
     * @param state 单据状态
     * @return
     */
    public PageMode<MaterialOutstockBill> pageMaterialOutstockBill(Integer current, Integer limit, String sortColumn, String sort,
                                                                 String searchKey, Integer state, Date beginTime, Date endTime) {
        MaterialOutstockBillQuery materialOutstockBillQuery = new MaterialOutstockBillQuery();
        materialOutstockBillQuery.setOffset((current-1)*limit);
        materialOutstockBillQuery.setLimit(limit);
        // 若未指定则默认按照时间排序
        if(ParamUtils.isNull(sortColumn)) {
            sortColumn = "billTime";
            sort = "desc";
        }
        materialOutstockBillQuery.setOrderByClause(ParamUtils.camel2Underline(sortColumn) + " " + sort);

        // 查询编号
        MaterialOutstockBillQuery.Criteria criteria = materialOutstockBillQuery.or();
        if (!ParamUtils.isNull(searchKey)) {
            criteria.andBillNoLike("%" + searchKey + "%");
        }
        if (!ParamUtils.isNull(state) && !state.equals(-1)) {
            criteria.andBillStateEqualTo(state);
        }
        if (!ParamUtils.isNull(beginTime)) {
            criteria.andBillTimeGreaterThanOrEqualTo(beginTime);
        }
        if (!ParamUtils.isNull(endTime)) {
            criteria.andBillTimeLessThanOrEqualTo(endTime);
        }
        // 查询领料人
        List<Integer> adminIdList = searchAdminByTrueName(searchKey);
        criteria = materialOutstockBillQuery.or();
        if (!ParamUtils.isNull(searchKey)) {
            if (adminIdList.size() == 0) {
                criteria.andToPrincipalEqualTo(0);
            } else {
                criteria.andToPrincipalIn(adminIdList);
            }
        }
        if (!ParamUtils.isNull(state) && !state.equals(-1)) {
            criteria.andBillStateEqualTo(state);
        }
        if (!ParamUtils.isNull(beginTime)) {
            criteria.andBillTimeGreaterThanOrEqualTo(beginTime);
        }
        if (!ParamUtils.isNull(endTime)) {
            criteria.andBillTimeLessThanOrEqualTo(endTime);
        }
        // 查询仓库负责人
        criteria = materialOutstockBillQuery.or();
        if (!ParamUtils.isNull(searchKey)) {
            if (adminIdList.size() == 0) {
                criteria.andWarehousePrincipalEqualTo(0);
            } else {
                criteria.andWarehousePrincipalIn(adminIdList);
            }
        }
        if (!ParamUtils.isNull(state) && !state.equals(-1)) {
            criteria.andBillStateEqualTo(state);
        }
        if (!ParamUtils.isNull(beginTime)) {
            criteria.andBillTimeGreaterThanOrEqualTo(beginTime);
        }
        if (!ParamUtils.isNull(endTime)) {
            criteria.andBillTimeLessThanOrEqualTo(endTime);
        }

        List<MaterialOutstockBill> result = materialOutstockBillDAO.selectByExample(materialOutstockBillQuery);

        for(MaterialOutstockBill bill: result) {
            String fromPrincipalName = adminDAO.selectByPrimaryKey(bill.getToPrincipal()).getTrueName();
            bill.setToPrincipalName(fromPrincipalName);
            String warehousePrincipalName = adminDAO.selectByPrimaryKey(bill.getWarehousePrincipal()).getTrueName();
            bill.setWarehousePrincipalName(warehousePrincipalName);
        }
        return new PageMode<MaterialOutstockBill>(result, materialOutstockBillDAO.countByExample(materialOutstockBillQuery));
    }

    /**
     * 删除物料出库单
     *
     * 进行必要的检查：是否为待审核状态
     * 删除主表信息：material_outstock_bill
     * 删除关联的从表信息：material_outstock_bill_product
     * 添加日志信息：LogType.MATERIAL_OUTSTOCK, Operate.REMOVE
     *
     * @param billIdList
     */
    public void removeMaterialOutstockBill(List<Integer> billIdList) {
        checkBillState(billIdList, 1);

        //删除出库单
        MaterialOutstockBillQuery materialOutstockBillQuery = new MaterialOutstockBillQuery();
        materialOutstockBillQuery.or().andBillIdIn(billIdList);
        materialOutstockBillDAO.deleteByExample(materialOutstockBillQuery);
        //删除关联
        MaterialOutstockBillMaterialQuery materialOutstockBillMaterialQuery = new MaterialOutstockBillMaterialQuery();
        materialOutstockBillMaterialQuery.or().andBillIdIn(billIdList);
        materialOutstockBillMaterialDAO.deleteByExample(materialOutstockBillMaterialQuery);
        addLog(LogType.MATERIAL_OUTSTOCK, Operate.REMOVE,billIdList);
    }

    /**
     * 审核物料出库单
     *
     * @param idList 单据编号
     */
    public void audit(List<Integer> idList) {
        checkBillState(idList, 1);
        Admin loginAdmin = RequestUtils.getLoginAdminFromCache();

        MaterialOutstockBill materialOutstockBill = new MaterialOutstockBill();
        materialOutstockBill.setBillState(2);
        materialOutstockBill.setAuditBy(loginAdmin.getAdminId());
        materialOutstockBill.setAuditAt(new Date());

        MaterialOutstockBillQuery materialOutstockBillQuery = new MaterialOutstockBillQuery();
        materialOutstockBillQuery.or().andBillIdIn(idList);
        materialOutstockBillDAO.updateByExampleSelective(materialOutstockBill, materialOutstockBillQuery);
        // 添加日志
        addLog(LogType.MATERIAL_OUTSTOCK, Operate.AUDIT, idList);
    }

    /**
     * 反审核物料出库单
     *
     * @param idList 单据编号
     */
    public void unaudit(List<Integer> idList) {
        checkBillState(idList, 2);
        Admin loginAdmin = RequestUtils.getLoginAdminFromCache();

        MaterialOutstockBill materialOutstockBill = new MaterialOutstockBill();
        materialOutstockBill.setBillState(1);
        materialOutstockBill.setAuditBy(loginAdmin.getAdminId());
        materialOutstockBill.setAuditAt(new Date());

        MaterialOutstockBillQuery materialOutstockBillQuery = new MaterialOutstockBillQuery();
        materialOutstockBillQuery.or().andBillIdIn(idList);
        materialOutstockBillDAO.updateByExampleSelective(materialOutstockBill, materialOutstockBillQuery);
        // 添加日志
        addLog(LogType.MATERIAL_OUTSTOCK, Operate.UNAUDIT, idList);
    }

    /**
     * 完成物料出库单
     * 若为领料出库，将相关领料单转为已完成状态
     *
     * @param billId 单据编号
     */
    public void finish(Integer billId) {
        checkBillState(Collections.singletonList(billId), 2);
        Admin loginAdmin = RequestUtils.getLoginAdminFromCache();

        MaterialOutstockBill materialOutstockBill = new MaterialOutstockBill();
        materialOutstockBill.setBillId(billId);
        materialOutstockBill.setBillState(3);
        materialOutstockBill.setFinishBy(loginAdmin.getAdminId());
        materialOutstockBill.setFinishAt(new Date());
        materialOutstockBillDAO.updateByPrimaryKeySelective(materialOutstockBill);

        // 若为领料出库，将相关领料单转为已完成状态
        materialOutstockBill = materialOutstockBillDAO.selectByPrimaryKey(materialOutstockBill.getBillId());
        if (materialOutstockBill != null && materialOutstockBill.getMaterialWhereabouts().equals(1)
                && !ParamUtils.isNull(materialOutstockBill.getRelatedBill())) {
            DrawMaterialBill drawMaterialBill = new DrawMaterialBill();
            drawMaterialBill.setBillId(materialOutstockBill.getRelatedBill());
            drawMaterialBill.setBillState(3);
            drawMaterialBill.setFinishBy(loginAdmin.getAdminId());
            drawMaterialBill.setFinishAt(new Date());
            drawMaterialBillDAO.updateByPrimaryKeySelective(drawMaterialBill);
        }

        // 添加日志
        addLog(LogType.MATERIAL_OUTSTOCK, Operate.FINISH, billId);
    }

    private void checkBillState(List<Integer> idList, int state) {
        MaterialOutstockBillQuery materialOutstockBillQuery = new MaterialOutstockBillQuery();
        materialOutstockBillQuery.or().andBillIdIn(idList)
                .andBillStateNotEqualTo(state);
        if (materialOutstockBillDAO.countByExample(materialOutstockBillQuery) > 0) {
            if (state == 1 ) {
                throw new BadRequestException(BILL_STATE_NOT_UNAUDIT);
            }
            if (state == 2) {
                throw new BadRequestException(BILL_STATE_NOT_AUDIT);
            }
        }
    }

    private void checkStockQuantity(List<MaterialOutstockBillMaterial> materialList) {
        Map<Integer, Integer> materialMap = new HashMap<Integer, Integer>();
        for (MaterialOutstockBillMaterial material : materialList) {
            if (!materialMap.containsKey(material.getMaterialId())) {
                materialMap.put(material.getMaterialId(), 0);
            }
            materialMap.put(material.getMaterialId(), materialMap.get(material.getMaterialId()) + material.getQuantity());
        }

        for (Integer materialId : materialMap.keySet()) {
            MaterialQuery materialQuery = new MaterialQuery();
            materialQuery.or().andMaterialIdEqualTo(materialId);
            List<MaterialStockRecord> materialStockRecordList = materialDAO.selectWithStockByExample(materialQuery);
            if (materialStockRecordList.size() == 0) {
                throw new BadRequestException(STOCK_QUANTITY_NOT_ENOUGH);
            }
            if (materialStockRecordList.get(0).getLeftAmount() < materialMap.get(materialId)) {
                throw new BadRequestException(STOCK_QUANTITY_NOT_ENOUGH + "，编号：" + materialStockRecordList.get(0).getMaterialNo());
            }
        }
    }

    private List<Integer> searchAdminByTrueName(String searchKey) {
        AdminQuery adminQuery = new AdminQuery();
        AdminQuery.Criteria criteria = adminQuery.or();
        criteria.andTrueNameLike("%" + searchKey + "%");
        List<Admin> adminList = adminDAO.selectByExample(adminQuery);
        List<Integer> adminIdList = new ArrayList<Integer>();
        for (Admin admin : adminList) {
            adminIdList.add(admin.getAdminId());
        }
        return adminIdList;
    }

    private static final String DRAW_MATERIAL_BILL_NOT_EXIST = "相关领料单不存在";
    private static final String DRAW_MATERIAL_BILL_NOT_AUDIT = "相关领料单不是已审核状态";
    private static final String BILL_STATE_NOT_UNAUDIT = "单据不是待审核状态";
    private static final String BILL_STATE_NOT_AUDIT = "单据不是已审核状态";
    private static final String STOCK_QUANTITY_NOT_ENOUGH = "物料库存数量不足";
}
