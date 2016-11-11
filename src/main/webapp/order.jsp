<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="com.myee.niuroumian.controller.WebWxController" %>
<%@ page import="com.myee.niuroumian.response.WeixinCfg" %>
<%@ page import="weixin.popular.bean.sns.SnsToken" %>
<%@ page import="com.myee.niuroumian.controller.OrderController" %>

<!DOCTYPE html>

<html lang="zh-cmn-Hans">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <title>美味点点</title>
    <%
        String openId="";
        WebWxController wwcc = new WebWxController();
        if(request.getParameter("code")==null){
            response.sendRedirect("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxe67244505b4041b6&redirect_uri=http%3A%2F%2Fpay.myee7.com%2Fnrm/order.jsp/&response_type=code&scope=snsapi_userinfo&state=#wechat_redirect");
        } else {
            SnsToken snsToken = wwcc.cfgSnsToken(request.getParameter("code"));
            openId = snsToken.getOpenid();
        }
    %>
    <script>
        if(sessionStorage.getItem("openid") == null){
            sessionStorage.openid ="<%=openId %>";
        }
    </script>
</head>
<body ontouchstart>
<div class="container" id="cm_app"></div>


</div>
</body>