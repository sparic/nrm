package com.myee.niuroumian.service.impl;

import com.myee.niuroumian.dao.DishDao;
import com.myee.niuroumian.dao.OrderInfoDao;
import com.myee.niuroumian.domain.DishInfo;
import com.myee.niuroumian.dto.OrderBasicInfo;
import com.myee.niuroumian.dto.OrderDisplay;
import com.myee.niuroumian.domain.OrderInfo;
import com.myee.niuroumian.dto.OrderRelDto;
import com.myee.niuroumian.service.OrderService;
import com.myee.niuroumian.service.RedisKeys;
import com.myee.niuroumian.service.RedisOperation;
import com.myee.niuroumian.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Jelynn on 2016/6/2.
 */
@Service
public class OrderServiceImpl extends RedisOperation implements OrderService{

    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private OrderInfoDao orderInfoDao;

    @Autowired
    private DishDao dishDao;

//    @Autowired
//    private OrderDaoImpl orderDao;

    @PersistenceContext
    private EntityManager entityManager;

    private  Timestamp start;
    private   Timestamp end ;

    @Autowired
    public OrderServiceImpl(RedisTemplate redisTemplate) {
        super(redisTemplate);
    }

//    public OrderServiceImpl() {
//    }

//    public OrderServiceImpl(OrderInfoDao orderInfoDao) {
//        this.orderInfoDao = orderInfoDao;
//    }

    @Transactional
    public OrderInfo createOrder(OrderInfo orderInfo){
        return orderInfoDao.save(orderInfo);
    }

    public int updateOrderState(OrderInfo orderInfo){
        return orderInfoDao.updateOrderState(orderInfo.getShopId(), orderInfo.getOrderId(), orderInfo.getOrderState(), orderInfo.getUpdateTime());
    }

    /**
     * @param orderInfo
     * @return
     */
    @Override
    public int updatePayState(OrderInfo orderInfo) {
        return orderInfoDao.updatePayState(orderInfo.getShopId(), orderInfo.getOrderId(), orderInfo.getPayState(), orderInfo.getPayTime());
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return orderInfoDao.findOne(orderId);
    }

    @Override
    public int generateWaitNo(Long shopId) {
        start = TimeUtil.getTodayStart();
        end = TimeUtil.getTodayEnd();
        List<Integer> list = orderInfoDao.generateWaitNo(shopId, start, end);
        int waitNo;
        if(list == null || list.size() == 0){
            waitNo = 1;
        }else{
            waitNo = list.get(0)+1;
        }
        return waitNo;
    }

    @Override
    public  List<OrderInfo> queryNotRepastOrder(Long shopId) {
        start = TimeUtil.getTodayStart();
        end = TimeUtil.getTodayEnd();
        LOG.info("   query not repast order for  "+shopId+"  from "+start+" to   "+end);
        return orderInfoDao.queryNotRepastOrder(shopId, start, end);
    }

    @Override
    public OrderDisplay queryCustomerOrder(String openId, Long shopId, Long start, Long end) {
        //根据条件查询订单表
        OrderDisplay orderDisplay = new OrderDisplay();
        Map map = new HashMap();
        if(shopId != null) {
            //如果店铺ID不为空，则查询某家店铺的顾客的所有已支付的订单
            Integer startInt = start.intValue();
            Integer endInt = end.intValue();
            map = listContractBorrows(openId, shopId, startInt, endInt);
        } else {
            Integer startInt = start.intValue();
            Integer endInt = end.intValue();
            //如果店铺ID为空，则查询某顾客的所有已支付的订单
            map = listContractBorrows(openId,shopId,startInt,endInt);
        }
        orderDisplay.setCount(Integer.parseInt(map.get("count").toString()));
        List<OrderBasicInfo> listBi = new ArrayList<>();
        for (Long orderId : (List<Long>)map.get("list")) {
            String sql = "SELECT New com.myee.niuroumian.dto.OrderRelDto(oi.orderId,oi.count,oi.orderPrice,ui.nickName,oii.orderItemId,di.dishName,oii.quantity,di.img) FROM OrderItemInfo oii,OrderInfo oi,DishInfo di,UserInfo ui where oii.orderId = oi.orderId" +
                    " and oii.dishInfo.dishId = di.dishId and oi.userId = ui.openId" +
                    " and oi.payState = 1 and oi.orderId="+orderId+" and oi.userId = '"+openId+"' and oi.shopId = "+shopId+ " ORDER BY oi.createTime desc";
            Query query = entityManager.createQuery(sql);
            List<OrderRelDto> allInfoList = query.getResultList();
            //查询该订单总信息
            OrderBasicInfo orderBasicInfo = new OrderBasicInfo();
            //订单ID
            orderBasicInfo.setOrderId(orderId);
            //订单名称
            orderBasicInfo.setOrderName(allInfoList.get(0).getNickName());
            //总价
            orderBasicInfo.setOrderPrice(allInfoList.get(0).getOrderPrice());
            //订单明细列表
            orderBasicInfo.setOrderRelDtoList(allInfoList);
            listBi.add(orderBasicInfo);
        }
        orderDisplay.setOrderBasicInfoList(listBi);
        return orderDisplay;
    }

    @Override
    public OrderDisplay queryShopOwnerOrder(Long shopId, Long start, Long end) {
        //根据条件查询订单表
        OrderDisplay orderDisplay = new OrderDisplay();
        Map map = new HashMap();
        if(shopId != null) {
            //如果店铺ID不为空，则查询某家店铺的顾客的所有已支付的订单
            Integer startInt = start.intValue();
            Integer endInt = end.intValue();
            map = listContractBorrows(null, shopId, startInt, endInt);
        } else {
            Integer startInt = start.intValue();
            Integer endInt = end.intValue();
            //如果店铺ID为空，则查询某顾客的所有已支付的订单
            map = listContractBorrows(null,shopId,startInt,endInt);
        }
        orderDisplay.setCount(Integer.parseInt(map.get("count").toString()));
        List<OrderBasicInfo> listBi = new ArrayList<>();
        for (Long orderId : (List<Long>)map.get("list")) {
            String sql = "SELECT New com.myee.niuroumian.dto.OrderRelDto(oi.orderId,oi.count,oi.orderPrice,ui.nickName,oii.orderItemId,di.dishName,oii.quantity,di.img) FROM OrderItemInfo oii,OrderInfo oi,DishInfo di,UserInfo ui where oii.orderId = oi.orderId" +
                    " and oii.dishInfo.dishId = di.dishId and oi.userId = ui.openId" +
                    " and oi.payState = 1 and oi.orderId="+orderId+" and oi.shopId = "+shopId+ " ORDER BY oi.createTime desc";
            Query query = entityManager.createQuery(sql);
            LOG.info("============"+query.toString());
            List<OrderRelDto> allInfoList = query.getResultList();
            //查询该订单总信息
            OrderBasicInfo orderBasicInfo = new OrderBasicInfo();
            //订单ID
            orderBasicInfo.setOrderId(orderId);
            //订单名称
            orderBasicInfo.setOrderName(allInfoList.get(0).getNickName());
            //总价
            orderBasicInfo.setOrderPrice(allInfoList.get(0).getOrderPrice());
            //订单明细列表
            orderBasicInfo.setOrderRelDtoList(allInfoList);
            listBi.add(orderBasicInfo);
        }
        orderDisplay.setOrderBasicInfoList(listBi);
        return orderDisplay;
    }

    @Override
    public DishInfo findById(Long dishId) {
        return dishDao.findOne(dishId);
    }

    @Override
    public void setCurrentRepastNOToRedis(int value,Long shopId) {
        String redisKey = RedisKeys.getCurrentRepastNO(shopId);
        set(redisKey,value);
    }

    @Override
    public int getCurrentRepastNO(Long shopId) {
        String redisKey = RedisKeys.getCurrentRepastNO(shopId);
        return Integer.parseInt(getSimple(redisKey).toString());
    }

    public Map listContractBorrows(String openId, Long shopId,int startNum, int endNum) {
        String sql = null;
        String countSql = null;
        Integer count = null;
        Map map = new HashMap();
        if (shopId != null) {
            sql = "SELECT distinct oi.orderId FROM OrderItemInfo oii,OrderInfo oi,DishInfo di,UserInfo ui where oii.orderId = oi.orderId" +
                    " and oii.dishInfo.dishId = di.dishId and oi.userId = ui.openId" +
                    " and oi.payState = 1 "+
                    (openId==null?"":" and oi.userId = '"+openId+"'")+
                     " and oi.shopId = "+shopId+ " ORDER BY oi.createTime desc";
            countSql = "SELECT count(distinct oi.orderId) FROM OrderItemInfo oii,OrderInfo oi,DishInfo di,UserInfo ui where oii.orderId = oi.orderId" +
                    " and oii.dishInfo.dishId = di.dishId and oi.userId = ui.openId" +
                    " and oi.payState = 1 "+
                    (openId==null?"":" and oi.userId = '"+openId+"'")+
                    " and oi.shopId = "+shopId+ " ORDER BY oi.createTime desc";
        } else {
            sql = "SELECT distinct oi.orderId FROM OrderItemInfo oii,OrderInfo oi,DishInfo di,UserInfo ui where oii.orderId = oi.orderId" +
                    " and oii.dishInfo.dishId = di.dishId and oi.userId = ui.openId" +
                    " and oi.payState = 1 " +
                    (openId==null?"":" and oi.userId = '"+openId+"'")+
                    " ORDER BY oi.createTime desc";
            countSql = "SELECT count(distinct oi.orderId) FROM OrderItemInfo oii,OrderInfo oi,DishInfo di,UserInfo ui where oii.orderId = oi.orderId" +
                    " and oii.dishInfo.dishId = di.dishId and oi.userId = ui.openId" +
                    " and oi.payState = 1 " +
                    (openId==null?"":" and oi.userId = '"+openId+"'")+
                    " ORDER BY oi.createTime desc";
        }
        Query query = entityManager.createQuery(sql);
        //查询总数不带分页
        query.setFirstResult(startNum);
        query.setMaxResults(endNum);
        List<Long> allList = query.getResultList();
        Query countQuery = entityManager.createQuery(countSql);
        Object object = countQuery.getSingleResult();
        map.put("count", object.toString());
        map.put("list",allList);
        return  map;
    }


}
