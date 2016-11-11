package com.myee.niuroumian.service.impl;

import com.myee.niuroumian.dao.DishDao;
import com.myee.niuroumian.domain.DishInfo;
import com.myee.niuroumian.domain.OrderInfo;
import com.myee.niuroumian.service.RedisOperation;
import com.myee.niuroumian.service.WeixinService;
import com.myee.niuroumian.util.AppPayBean;
import com.myee.niuroumian.util.ControllerUtil;
import me.chanjar.weixin.mp.bean.WxMpCustomMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import weixin.popular.api.PayMchAPI;
import weixin.popular.bean.paymch.Unifiedorder;
import weixin.popular.bean.paymch.UnifiedorderResult;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ray.Fu on 2016/6/1.
 */
@Service
public class WeixinServiceImpl extends RedisOperation implements WeixinService{

    @Autowired
    private DishDao dishDao;

    @Autowired
    protected WeixinServiceImpl(RedisTemplate redisTemplate) {
        super(redisTemplate);
    }
//    @Autowired
//       protected WeixinServiceImpl(DishDao dishDao) {
////        super(redisTemplate);
//        this.dishDao = dishDao;
//    }



    @Override
    public Map findQuickOrderMenu(Long storeId) {
        Map map = new HashMap();
        List<DishInfo> list = dishDao.queryAllDishByStoreId(storeId);
        String dishList = "";
        String storeName = null;
        for(DishInfo d : list){
            String temp = d.getDishName() + "("+ d.getDishType() +")"+ " ¥"+ d.getDishPrice() + "  <a href=\"http://pay.myee7.com/nrm/order.jsp?dishid="+d.getDishId()+"\">下单</a>\n\n";
            dishList += temp;
            storeName = d.getStoreInfo().getStoreName();
        }
        map.put("dishList","欢迎光临"+ storeName +"\n" +
//                " ****** 快速点餐 *****\n" +
                "\n" + dishList +
                "如没有您需要的菜品，请点击<a href=\"http://pay.myee7.com/nrm/\">查看菜品</a>\n" +
                "或点击下方\"立即下单\"按钮");
        return map;
    }

    /**
     * 微信支付统一
     * @param orderInfo
     * @param openId
     * @return
     */
    @Override
    public AppPayBean payUnifiedorder( OrderInfo orderInfo, String openId) {
        AppPayBean appPayBean = new AppPayBean();
        Unifiedorder unifiedorder = new Unifiedorder();
        unifiedorder.setNonce_str(ControllerUtil.getRandomStringByLength(32));
        unifiedorder.setBody("商品名称");//商品名称
        unifiedorder.setNotify_url("http://pay.myee7.com/nrm/wxitf/busNoticeWeiXin.do");//回调
        unifiedorder.setOut_trade_no(orderInfo.getOrderId().toString());
        unifiedorder.setSpbill_create_ip("210.14.72.168");// 这里需要服务器地址，就用这个图片地址了，因为图片也是这个地址
//        unifiedorder.setTotal_fee("1");
        String price = orderInfo.getOrderPrice().multiply(new BigDecimal(100)).toString();
        unifiedorder.setTotal_fee(price.substring(0, price.indexOf(".")));
        unifiedorder.setAttach("4;");

        unifiedorder.setAppid("wxe67244505b4041b6");//
        unifiedorder.setMch_id("1295359601");//商户号
        unifiedorder.setDevice_info("WEB");
        unifiedorder.setTrade_type("JSAPI");
        unifiedorder.setOpenid(openId);//微信OPENID

        UnifiedorderResult unifiedorderResult =  PayMchAPI.payUnifiedorder(unifiedorder, "QsxnytrdFGJSKoefsdfeHSBXCNyeufjE");
        Map<String, Object> payMap = new HashMap<String, Object>();
        payMap.put("appId", "wxe67244505b4041b6");
        Long timeStamp = System.currentTimeMillis() / 1000;
        payMap.put("timeStamp", timeStamp);
        String nonce_str = ControllerUtil.getRandomStringByLength(32);
        payMap.put("nonceStr", nonce_str);
        payMap.put("signType", "MD5");
        String prepay_id = unifiedorderResult.getPrepay_id();
        payMap.put("package", "prepay_id=" + prepay_id);
        String paySign = ControllerUtil.getSign(payMap,"QsxnytrdFGJSKoefsdfeHSBXCNyeufjE");
        appPayBean.setTimeStamp(timeStamp.toString());
        appPayBean.setSign(paySign);
        appPayBean.setNonce_str(nonce_str);
        appPayBean.setPrepayId(prepay_id);
        appPayBean.setOrderId(orderInfo.getOrderId());
        return appPayBean;
    }

    /**
     * 往消费者推送排号信息 TODO
     * @param tokenNum
     */
    @Override
    public void pushTokenToCustomer(int tokenNum, String openId, String fromUser) {
        System.out.println("支付成功后，获得的排号：" + tokenNum + "用户OPENID->" + openId + "公众号ID->" + fromUser);
        WxMpXmlOutMessage.TEXT().content("" + tokenNum).fromUser(fromUser).toUser(openId).build();
    }

    @Override
    public void setUrlAndShopId(String url, Long shopId) {
        set(url,shopId);
    }

    @Override
    public Long getShopIdByUrl(String url) {
        return get(url,Long.class);
    }

}
