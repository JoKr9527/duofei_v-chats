package com.duofei.message.dispatcher;

import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.MsgHandle;
import com.duofei.message.MsgHandler;
import com.duofei.message.model.SystemMessage;
import com.duofei.message.model.UserMessage;
import com.duofei.scope.*;
import com.duofei.user.BaseUser;
import org.kurento.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 录制消息处理
 * @author duofei
 * @date 2019/9/21
 */
@Component
public class RecordMsgDispatcher implements MsgDispatcher {

    private static Map<String,MsgHandle> msgHandles = new HashMap<>();

    private static Map<String, RecorderEndpoint> recorders = new HashMap<>();

    private static String uri = "file:///var/log/kurento-media-server/";

    @Resource
    private ScopeContext scopeContext;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UserContext userContext;
    @Autowired
    private WebRtcEndpointContext webRtcEndpointContext;

    @Override
    public Map<String, MsgHandle> msgHandles() {
        return msgHandles;
    }

    /**
     * 接收开始录制消息
     * @author duofei
     * @date 2019/9/21
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle startRecord(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) -> {
            String key = userMessage.getFrom() + userMessage.getTo();
            Scope scope = scopeContext.getE(userMessage.getTo());

            if(scope instanceof BaseScope){
                BaseScope baseScope = (BaseScope)scope;
                // 构建recorder
                RecorderEndpoint recorder = new RecorderEndpoint.Builder(baseScope.getMediaPipeline(),uri+key+".webm")
                        .withMediaProfile(MediaProfileSpecType.MP4).build();
                // 一对一聊天室，录制对方
                if(baseScope instanceof OneToOneScope){
                    OneToOneScope oneToOneScope = (OneToOneScope) baseScope;
                    WebRtcEndpoint source = null;
                    if(oneToOneScope.getCallingFromUserName().equals(userMessage.getFrom())){
                        source = oneToOneScope.getCallingTo();
                    }else{
                        source = oneToOneScope.getCallingFrom();
                    }
                    recorder.addMediaFlowInStateChangeListener(flowIn ->{
                        if(flowIn.getState() == MediaFlowState.NOT_FLOWING){
                            recorder.stopAndWait();
                        } });
                    source.connect(recorder);
                    recorder.record();
                }
                // 一对多时，录制主持人
                if(baseScope instanceof OneToManyScope){
                    OneToManyScope oneToManyScope = (OneToManyScope) baseScope;
                    WebRtcEndpoint source = oneToManyScope.getPresenter();
                    recorder.addMediaFlowInStateChangeListener(flowIn ->{
                        if(flowIn.getState() == MediaFlowState.NOT_FLOWING){
                            recorder.stopAndWait();
                        } });
                    source.connect(recorder);
                    recorder.record();
                }
                // 多对多时，混流录制
                if(baseScope instanceof GroupScope){
                    GroupScope groupScope = (GroupScope) baseScope;
                    Composite composite = new Composite.Builder(groupScope.getMediaPipeline()).build();
                    groupScope.getAllGroupUsers().forEach(groupUser -> {
                        HubPort sink = new HubPort.Builder(composite).build();
                        groupUser.getOutgoingMedia().connect(sink);
                    });
                    HubPort source = new HubPort.Builder(composite).build();
                    source.addMediaFlowOutStateChangeListener(flowOut ->{
                        if(flowOut.getState() == MediaFlowState.FLOWING){
                            recorder.record();
                        } });
                    recorder.addMediaFlowInStateChangeListener(flowIn ->{
                        if(flowIn.getState() == MediaFlowState.NOT_FLOWING){
                            recorder.stopAndWait();
                        } });
                    source.connect(recorder);
                }
                // 多人聊天时，开始录制
                if(baseScope instanceof PeopleRoomScope){
                    PeopleRoomScope peopleRoomScope = (PeopleRoomScope) baseScope;
                    peopleRoomScope.getHubPort().connect(recorder);
                    recorder.addMediaFlowInStateChangeListener(flowIn ->{
                        if(flowIn.getState() == MediaFlowState.NOT_FLOWING){
                            recorder.stopAndWait();
                        } });
                    recorder.record();
                }
                recorders.put(key, recorder);
            }
        };
        return result;
    }

    /**
     * 接收停止录制消息消息
     * @author duofei
     * @date 2019/9/21
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle stopRecord(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) -> {
            String key = userMessage.getFrom() + userMessage.getTo();
            RecorderEndpoint recorder = recorders.get(key);
            if(recorder != null){
                recorder.stopAndWait();
                recorder.release();
                recorders.put(key, null);
            }
        };
        return result;
    }

    /**
     * 接收获取可播放文件
     * @author duofei
     * @date 2019/9/21
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle reqPlayFiles(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) -> {
            String userName = userMessage.getFrom();
            // 获取该用户存储的播放列表
            List<String> keys = recorders.keySet().stream().filter(key -> key.contains(userName)).collect(Collectors.toList());
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setId("playFiles");
            systemMessage.setContent(keys);
            BaseUser baseUser = userContext.getE(userMessage.getFrom());
            if(baseUser != null){
                applicationContext.publishEvent(new MsgSendEvent(baseUser.getSession(),systemMessage));
            }
        };
        return result;
    }

    /**
     * 播放文件
     * @author duofei
     * @date 2019/9/21
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle playFile(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) -> {
            // 获取创建的webRtc 名称
            String name = userMessage.getFrom() + userMessage.getContent();
            WebRtcEndpoint webRtcEndpoint = webRtcEndpointContext.getE(name);
            if(webRtcEndpoint != null){
                PlayerEndpoint playEndpoint = new PlayerEndpoint.Builder(webRtcEndpoint.getMediaPipeline(), uri + userMessage.getContent() + ".webm").build();
                playEndpoint.connect(webRtcEndpoint);
                playEndpoint.play();
            }
        };
        return result;
    }

}
