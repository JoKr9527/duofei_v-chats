package com.duofei;

import com.duofei.message.MsgHandler;
import com.duofei.config.SpringApplicationContext;
import com.duofei.message.MsgHandle;
import com.duofei.message.dispatcher.MsgDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 启动类
 * @author duofei
 * @date 2019/8/16
 */
@SpringBootApplication
@EnableWebMvc
public class VChatsApp {

    private static Logger logger = LoggerFactory.getLogger(VChatsApp.class);

    public static void main(String[] args) {
        SpringApplication.run(VChatsApp.class, args);
        // 注册消息处理器
        Map<String, MsgDispatcher> beansOfType = SpringApplicationContext.getApplicationContext().getBeansOfType(MsgDispatcher.class);
        Set<Map.Entry<String, MsgDispatcher>> entries = beansOfType.entrySet();
        Iterator<Map.Entry<String, MsgDispatcher>> iterator =entries.iterator();
        while (iterator.hasNext()){
            Map.Entry<String, MsgDispatcher> next = iterator.next();
            MsgDispatcher msgDispatcher = next.getValue();
            ReflectionUtils.doWithMethods(msgDispatcher.getClass(), m->{
                MsgHandler msgHandler = m.getAnnotation(MsgHandler.class);
                String value = msgHandler.value().equals("")?m.getName():msgHandler.value();
                try {
                    m.setAccessible(true);
                    Object msgHandle = m.invoke(msgDispatcher);
                    msgDispatcher.msgHandles().put(value, ((MsgHandle) msgHandle));
                } catch (InvocationTargetException e) {
                    logger.error("注册消息处理器失败，msgDispatcherType={}",next.getKey(),e);
                }
            },m->{
                MsgHandler msgHandler = m.getAnnotation(MsgHandler.class);
                if(msgHandler == null){
                    return false;
                }
                return true;
            });
        }
    }
}
