package com.duofei.handler;

import com.duofei.message.BaseMessage;
import com.duofei.scope.Scope;
import com.duofei.user.BaseUser;
import com.duofei.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * 存转发消息处理器
 * @author duofei
 * @date 2019/8/20
 */
@Component("redirectMessageHandler")
public class RedirectMessageHandler implements MessageHandler {

    private static Logger logger = LoggerFactory.getLogger(RedirectMessageHandler.class);

    @Override
    public <T> void handle(Scope scope, List<BaseUser> baseUsers, BaseMessage<T> baseMessage) {
        try {
            Iterator<BaseUser> iterator = baseUsers.iterator();
            while(iterator.hasNext()){
                BaseUser baseUser = iterator.next();
                if(baseUser.getSession().isOpen()){
                    baseUser.getSession().sendMessage(new TextMessage(JsonUtils.toJSON(baseMessage)));
                }
            }
        } catch (IOException e) {
            logger.error("直接转发消息发生错误,users={},msg={}",baseUsers, baseMessage,e);
        }
    }
}
