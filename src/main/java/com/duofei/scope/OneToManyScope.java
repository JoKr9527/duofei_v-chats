package com.duofei.scope;

import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 一对多域
 * @author duofei
 * @date 2019/8/16
 */
public class OneToManyScope extends BaseScope {

    /**
     * 主持人
     */
    private WebRtcEndpoint presenter;
    /**
     * 参观者
     */
    private Map<String,WebRtcEndpoint> viewer;

    public OneToManyScope(String id, MediaPipeline mediaPipeline){
        super(id, mediaPipeline);
        this.viewer = new HashMap<>();
    }

    public OneToManyScope(String id){
        super(id);
        this.viewer = new HashMap<>();
    }

    public OneToManyScope(String id, String name){
        super(id,name);
        this.viewer = new HashMap<>();
    }

    public WebRtcEndpoint getPresenter() {
        return presenter;
    }

    public void setPresenter(WebRtcEndpoint presenter) {
        this.presenter = presenter;
    }
    
    /**
     * 新增参观者
     * @author duofei
     * @date 2019/8/16
     * @param name
     * @param webRtcEndpoint
     */
    public void addViewer(String name,WebRtcEndpoint webRtcEndpoint){
        presenter.connect(webRtcEndpoint);
        viewer.put(name, webRtcEndpoint);
    }

    /**
     * 移除参观者
     * @author duofei
     * @date 2019/8/16
     * @param name 用户名字
     */
    public void removeViewer(String name){
        final WebRtcEndpoint webRtcEndpoint = this.viewer.get(name);
        if(webRtcEndpoint != null){
            this.viewer.remove(name);
            this.presenter.disconnect(webRtcEndpoint);
            webRtcEndpoint.release();
        }
    }

    /**
     * 返回参观者名称
     * @author duofei
     * @date 2019/8/28
     * @return Set<String> 参观者名字
     */
    public Set<String> getViewers(){
        return viewer.keySet();
    }
}
