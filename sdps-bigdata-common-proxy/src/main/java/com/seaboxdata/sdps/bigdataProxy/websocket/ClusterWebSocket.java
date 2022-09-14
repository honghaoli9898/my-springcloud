package com.seaboxdata.sdps.bigdataProxy.websocket;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 集群Socket
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/16
 */
@ServerEndpoint("/websocket/cluster/{username}")
@Slf4j
public class ClusterWebSocket {
    private static int onlineCount = 0;
    private static Map<String, ClusterWebSocket> clients = new ConcurrentHashMap<>();
    private Session session;
    private String username;

    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) throws IOException {

        this.username = username;
        this.session = session;

        addOnlineCount();
        clients.put(username, this);
        log.info("已连接");
    }

    @OnClose
    public void onClose() throws IOException {
        clients.remove(username);
        subOnlineCount();
    }

    @OnMessage
    public void onMessage(String message) throws IOException {

        /*JSONObject jsonTo = JSONObject.parseObject(message);
        String mes = (String) jsonTo.get("message");*/

        /*if (!"All".equals(jsonTo.get("To"))) {
            sendMessageTo(mes, jsonTo.get("To").toString());
        } else {
            sendMessageAll("给所有人");
        }*/
        sendMessageAll(message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("集群Socket连接失败 ", error);
        // error.printStackTrace();
    }

    public void sendMessageTo(String message, String to) throws IOException {
        for (ClusterWebSocket item : clients.values()) {
            if (item.username.equals(to)) {
                item.session.getAsyncRemote().sendText(message);
            }
        }
    }

    public void sendMessageAll(String message) throws IOException {
        for (ClusterWebSocket item : clients.values()) {
            item.session.getAsyncRemote().sendText(message);
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        ClusterWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        ClusterWebSocket.onlineCount--;
    }

    public static synchronized Map<String, ClusterWebSocket> getClients() {
        return clients;
    }
}
