package bigdata.cdc.utils;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.taobao.api.ApiException;

public class Notify {


    private String token;
    private OapiRobotSendRequest.Text text;
    private OapiRobotSendRequest request;
    private DingTalkClient client;

    public Notify(String token) {
        this.token = token;
        client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?access_token=" + this.token);
        request = new OapiRobotSendRequest();
        request.setMsgtype("text");
        text = new OapiRobotSendRequest.Text();
    }



    public void sendDingTalk(String msgContent) {

        text.setContent("【同步】" + msgContent);
        request.setText(text);
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setIsAtAll(true);
        request.setAt(at);


        try {
            client.execute(request);

        } catch (ApiException e) {
            System.out.println(e.getErrMsg());
        }
    }

}
