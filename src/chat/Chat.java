package chat;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/websocket/{nickName}")
public class Chat {

    /**
     * 连接对象集合
     */
    private static final Set<Chat> connections = new CopyOnWriteArraySet<Chat>();

    private String nickName;

    /**
     * WebSocket Session
     */
    private Session session;

    public Chat() {
    }

    /**
     * 打开连接
     *
     * @param session
     * @param nickName
     */
    @OnOpen
    public void onOpen(Session session,
                       @PathParam(value = "nickName") String nickName) {

        this.session = session;
        this.nickName = nickName;

        connections.add(this);
        String message = String.format("System> %s %s", this.nickName,
                " 已加入群聊.");
        Chat.broadCast(message);
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose() {
        connections.remove(this);
        String message = String.format("System> %s, %s", this.nickName,
                " 退出群聊.");
        Chat.broadCast(message);
    }

    /**
     * 接收信息
     *
     * @param message
     * @param nickName
     */
    @OnMessage
    public void onMessage(String message,
                          @PathParam(value = "nickName") String nickName) {
        Chat.broadCast(nickName + ">" + message);
        if(message.indexOf("@小方")!=-1){
            try {
                Chat.broadCast("机器人小方"+">"+Robot.machine(message.split("@小方")[0]));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 错误信息响应
     *
     * @param throwable
     */
    @OnError
    public void onError(Throwable throwable) {
    }

    /**
     * 发送或广播信息
     *
     * @param message
     */
    private static void broadCast(String message) {
        for (Chat chat : connections) {
            try {
                synchronized (chat) {
                    chat.session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                connections.remove(chat);
                try {
                    chat.session.close();
                } catch (IOException e1) {
                }
                Chat.broadCast(String.format("System> %s %s", chat.nickName,
                        " 退出群聊."));
            }
        }
    }
}