package com.duofei.message.dispatcher;

import com.duofei.message.MsgHandle;
import com.duofei.message.MsgHandler;
import com.duofei.message.model.UserMessage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * http 相关消息处理
 * @author duofei
 * @date 2019/9/23
 */
@Component
public class HttpMsgDispatcher implements MsgDispatcher {

    private static Map<String, MsgHandle> msgHandleMap = new HashMap<>(8);

    @Override
    public Map<String, MsgHandle> msgHandles() {
        return msgHandleMap;
    }

    /**
     * 获取可用于视频文件上传的地址
     * @author duofei
     * @date 2019/9/23
     * @param
     * @return
     * @throws
     */
    @MsgHandler
    private MsgHandle localFileUrl(){
        MsgHandle<UserMessage> msgHandle = (UserMessage userMessage) ->{

        };
        return msgHandle;
    }
}
