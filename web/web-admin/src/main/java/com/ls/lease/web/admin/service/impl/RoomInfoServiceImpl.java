package com.ls.lease.web.admin.service.impl;

import com.ls.lease.common.constant.RedisConstant;
import com.ls.lease.model.entity.*;
import com.ls.lease.model.enums.ItemType;
import com.ls.lease.web.admin.mapper.*;
import com.ls.lease.web.admin.service.*;
import com.ls.lease.web.admin.vo.attr.AttrValueVo;
import com.ls.lease.web.admin.vo.graph.GraphVo;
import com.ls.lease.web.admin.vo.room.RoomDetailVo;
import com.ls.lease.web.admin.vo.room.RoomItemVo;
import com.ls.lease.web.admin.vo.room.RoomQueryVo;
import com.ls.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Autowired
    LeaseTermMapper leaseTermMapper;

    @Autowired
    PaymentTypeMapper paymentTypeMapper;

    @Autowired
    LabelInfoMapper labelInfoMapper;
    @Autowired
    FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private AttrValueMapper attrValueMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private RoomLabelService roomLabelService;

    @Autowired
    private RoomFacilityService roomFacilityService;

    @Autowired
    private RoomAttrValueService roomAttrValueService;

    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;

    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;


    @Override
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate = roomSubmitVo.getId() != null;
        super.saveOrUpdate(roomSubmitVo);
        if (isUpdate) {
            //1.删除图片列表
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphQueryWrapper.eq(GraphInfo::getItemId, roomSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);

            //2.删除标签列表
            LambdaQueryWrapper<RoomLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
            labelQueryWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(labelQueryWrapper);

            //3.
            LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(facilityQueryWrapper);
            //4.
            LambdaQueryWrapper<RoomAttrValue> attrQueryMapper = new LambdaQueryWrapper<>();
            attrQueryMapper.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(attrQueryMapper);

            //5.
            LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
            paymentQueryWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(paymentQueryWrapper);
            //6.
            LambdaQueryWrapper<RoomLeaseTerm> termQueryWrapper = new LambdaQueryWrapper<>();
            termQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(termQueryWrapper);

            //删除缓存
            String key = RedisConstant.APP_ROOM_PREFIX + roomSubmitVo.getId();
            redisTemplate.delete(key);

        }
        //1.保存新的graphInfoList
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoArrayList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfoArrayList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoArrayList);
        }
        //2.保存新的roomAttrValueList
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(attrValueIds)){
            ArrayList<RoomAttrValue> roomAttrValueArrayList = new ArrayList<>();
            for (Long attrValueId : attrValueIds) {
                RoomAttrValue roomAttrValue = new RoomAttrValue();
                roomAttrValue.setRoomId(roomSubmitVo.getId());
                roomAttrValue.setAttrValueId((attrValueId));
                roomAttrValueArrayList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueArrayList);
        }
        //3.保存新的facilityInfoList
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIds)){
            ArrayList<RoomFacility> roomFacilityArrayList = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIds) {
                RoomFacility roomFacility = new RoomFacility();
                roomFacility.setRoomId(roomSubmitVo.getId());
                roomFacility.setFacilityId((facilityInfoId));
                roomFacilityArrayList.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilityArrayList);
        }
        //4.保存新的labelInfoList
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if(!CollectionUtils.isEmpty(labelInfoIds)){
            ArrayList<RoomLabel> roomLabelArrayList = new ArrayList<>();
            for (Long labelInfoId : labelInfoIds) {
                RoomLabel roomLabel = new RoomLabel();
                roomLabel.setRoomId(roomSubmitVo.getId());
                roomLabel.setLabelId((labelInfoId));
                roomLabelArrayList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelArrayList);
        }
        //5.保存新的paymentTypeList
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if(!CollectionUtils.isEmpty(paymentTypeIds)){
            ArrayList<RoomPaymentType> roomPaymentTypeArrayList = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType roomPaymentType = new RoomPaymentType();
                roomPaymentType.setRoomId(roomSubmitVo.getId());
                roomPaymentType.setPaymentTypeId((paymentTypeId));
                roomPaymentTypeArrayList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeArrayList);
        }
        //6.保存新的leaseTermList
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermIds)) {
            ArrayList<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for (Long leaseTermId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder().roomId(roomSubmitVo.getId()).leaseTermId(leaseTermId).build();
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }
    }

    @Override
    public IPage<RoomItemVo> pageRoomItemByQuery(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageRoomItemByQuery(page, queryVo);
    }

    @Override
    public RoomDetailVo getRoomDetailById(Long id) {
        //1.房间基础信息
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        //2.图片列表
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, roomInfo.getId());
        //3属性值
        List<AttrValueVo> attrvalueVoList = attrValueMapper.selectListByRoomId(id);
        //4.所属公寓
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());
        //5.配套查询
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);
        //6.标签查询
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);
        //7.支付方式
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);
        //8.租赁信息
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);

        RoomDetailVo adminRoomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, adminRoomDetailVo);
        adminRoomDetailVo.setApartmentInfo(apartmentInfo);
        adminRoomDetailVo.setGraphVoList(graphVoList);
        adminRoomDetailVo.setAttrValueVoList(attrvalueVoList);
        adminRoomDetailVo.setFacilityInfoList(facilityInfoList);
        adminRoomDetailVo.setLabelInfoList(labelInfoList);
        adminRoomDetailVo.setPaymentTypeList(paymentTypeList);
        adminRoomDetailVo.setLeaseTermList(leaseTermList);

        return adminRoomDetailVo;
    }

    @Override
    public void removeRoomById(Long id) {
        //1.删除RoomInfo
        super.removeById(id);
        //2.删除graphInfoList
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoQueryWrapper.eq(GraphInfo::getItemType,ItemType.ROOM);
        graphInfoService.remove(graphInfoQueryWrapper);
        //3.删除facilityIfoList
        LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(RoomFacility::getRoomId,id);
        roomFacilityService.remove(facilityQueryWrapper);
        //4.删除labelInfoList
        LambdaQueryWrapper<RoomLabel>  roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId,id);
        roomLabelService.remove(roomLabelQueryWrapper);
        //5.属性值
        LambdaQueryWrapper<RoomAttrValue> attrQueryWrapper = new LambdaQueryWrapper<>();
        attrQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(attrQueryWrapper);
        //6.删除paymentTypeList
        LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
        paymentQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(paymentQueryWrapper);
        //7.leasetermlist
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(roomLeaseTermQueryWrapper);

        //删除缓存
        String key = RedisConstant.APP_ROOM_PREFIX+id;
        redisTemplate.delete(key);
    }
}




