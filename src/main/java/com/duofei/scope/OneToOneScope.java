package com.duofei.scope;

import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;

/**
 * 一对一域
 * @author duofei
 * @date 2019/8/16
 */
public class OneToOneScope extends BaseScope {

    /**
     * 接收方
     */
    private WebRtcEndpoint callingTo;
    /**
     * 发送方
     */
    private WebRtcEndpoint callingFrom;

    public OneToOneScope(String id, MediaPipeline mediaPipeline){
        super(id,mediaPipeline);
    }

    public OneToOneScope(String id){
        super(id);
    }

    public WebRtcEndpoint getCallingTo() {
        return callingTo;
    }

    public WebRtcEndpoint getCallingFrom() {
        return callingFrom;
    }

    public void setCallingTo(WebRtcEndpoint callingTo) {
        this.callingTo = callingTo;
    }

    public void setCallingFrom(WebRtcEndpoint callingFrom) {
        this.callingFrom = callingFrom;
    }

    /**
     * 建立连接
     * @author duofei
     * @date 2019/8/16
     */
    public void connect(){
        callingFrom.connect(callingTo);
        callingTo.connect(callingFrom);
    }

}
