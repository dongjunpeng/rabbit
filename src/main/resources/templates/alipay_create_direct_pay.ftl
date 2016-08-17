<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>海逍遥接入支付宝平台</title>
    </head>
        <form id="alipaysubmit" name="alipaysubmit" action="https://mapi.alipay.com/gateway.do?_input_charset=utf-8" method="get">
            <input type="hidden" name="payment_type" value="1"/>
            <input type="hidden" name="out_trade_no" value="2"/>
            <input type="hidden" name="service" value="create_direct_pay_by_user"/>
            <input type="hidden" name="_input_charset" value="utf-8"/>
            <input type="hidden" name="sign_type" value="MD5"/>
            <input type="hidden" name="partner" value="${alipay.partner}"/>
            <input type="hidden" name="seller_id" value="${alipay.seller_id}"/>
            <input type="hidden" name="price" value="${price}"/>
            <input type="hidden" name="total_fee" value="${total_fee}"/>
            <input type="hidden" name="sign" value="${sign}"/>
            <input type="hidden" name="return_url" value="${alipay.return_url}"/>
            <input type="hidden" name="notify_url" value="${alipay.notify_url}"/>
            <input type="submit" value="确认" style="display:none;">
        </form>
        <script>document.forms['alipaysubmit'].submit();</script>
    <body>
    </body>
</html>
