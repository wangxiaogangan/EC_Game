package io.element.communication;

import io.element.communication.Session.BroadCast;
import io.element.communication.Session.Deliver;
import io.element.communication.impl.ServerSession;
import io.element.gtserver.App;
import io.element.protobuf.GlobalProto;
import io.element.protobuf.LoginProto;
import io.element.reflect.Handler;
import io.element.room.impl.SessionRoom;
import io.element.util.HandlerUtil;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

// room  单纯的保存房间的数据  我们可以简单的将room当作doc
// steering 会根据  房间数据以及type 向game server请求  绝定到底该如何处理数据
public class ServerSteering implements BroadCast<Integer>, Deliver{

	protected SessionRoom 	m_owner 		= null;
	
	protected int 			m_maxCapacity 	= 0;
	
	protected ServerSession m_session 		= null;
	
	protected static HashMap<LoginProto.G2S_MSGTYPE , Handler> m_logHandlers = new HashMap<LoginProto.G2S_MSGTYPE, Handler>();
	
	public ServerSteering( SessionRoom owner)
	{
		m_owner = owner;
	}
	
	public final ServerSession session(){ 
		return m_session; 
	}
	
	public boolean checkVaild()
	{
		return m_session != null ? m_session.isActive() : false;
	}
	
	public boolean playersCountVaild(){ 
		return false; 
	}
	
	public int maxCapacity(){
		return 0;
	}
	
	public void broadcast() {
		// TODO Auto-generated method stub
		
	}

	public void broadcast(int location) {
		// TODO Auto-generated method stub
		
	}

	public void broadcast(List<Integer> lists) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean connect() throws InterruptedException {
		if(m_session == null)
			return false;
		
		return m_session.connect();
	}

	public boolean connect(Handler completeCallback) {
		if(m_session == null)
			return false;
		
		return m_session.connect(completeCallback);
	}

	public boolean connect(int port, Handler completeCallback) {
		if(m_session == null)
			return false;
		
		return m_session.connect(port, completeCallback);
	}

	public void close(Handler completeCallBack)
	{
		if( m_session != null )
			m_session.close(completeCallBack);
	}
	
	public void close()
	{
		if( m_session != null )
		{
			try {
				m_session.close();
			} catch (InterruptedException e) {
				
			}			
		}
	}
	
	
	//*******************************************************
	// configure for msg type & call back method	begin
	//*******************************************************
	static {
		RegisterProtoMsgCmd();
	}
	
	protected static 	void RegisterProtoMsgCmd() 
	{
		REFLECT_REGISTER_TYPE_CMD(LoginProto.G2S_MSGTYPE.G2S_REQUEST_BEGIN_GAME, 		"G2S_req_beginGame");
		REFLECT_REGISTER_TYPE_CMD(LoginProto.G2S_MSGTYPE.G2S_REQUEST_CREATE_NEWROOM, 	"G2S_req_newRoom");
	}
	
	protected static 	void REFLECT_REGISTER_TYPE_CMD(LoginProto.G2S_MSGTYPE type, String str_method) {
		Method method = null;
		try {
			method = HandlerUtil.getMethod(ServerSteering.class.getName() , str_method);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(method == null)
			return;
		
		Handler handler = new Handler(method);
		m_logHandlers.put(type, handler);
	}

	// get function handler
	public  Handler ReflectHandler(LoginProto.G2S_MSGTYPE type) {
		Handler handler = ServerSteering.m_logHandlers.get(type);
		if(handler == null)
			return null;
		
		Handler tempHandler = null;
		try {
			 tempHandler = (Handler) handler.clone();			
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tempHandler.SetObject(this);		
		return tempHandler;
	}

	// call function positive
	public boolean HandlerMsg(LoginProto.G2S_MSGTYPE type, Object param) {
		Handler handler = ServerSteering.m_logHandlers.get(type);
		if(handler == null)
			return false;
		
		Handler tempHandler = null;
		try {
			 tempHandler = (Handler) handler.clone();			
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return HandlerMsg(tempHandler, param);
	}
	
	protected boolean HandlerMsg(Handler handler, Object param)
	{
		// 下面这句话实现了房间事件的 override
		handler.SetObject(this);
		handler.SetParam(param);
		handler.invoke();
		return true;
	}
	
	//*******************************************************
	// configure for msg type & call back method	end
	//*******************************************************
	
	// G2S_REQUEST_BEGIN_GAME
	public void G2S_req_beginGame(Object obj)
	{
		// 目前保持一致
		G2S_req_newRoom(obj);		
	}
	
	// G2S_REQUEST_CREATE_NEWROOM -- connect 完成以后 直接发送create room 请求
	public void G2S_req_newRoom(Object obj)
	{
		App.LOGGER.info("gate room-- 发送逻辑服务器创建请求 --logic server， id = {}", this.m_owner.getID());
		
		SessionRoom room = (SessionRoom) m_owner;
		
		LoginProto.Room logRoom = new HandlerUtil().getLoginRoom(room);
		LoginProto.G2S_CreateNewRoom.Builder builder = LoginProto.G2S_CreateNewRoom.newBuilder(); 
		LoginProto.G2S_CreateNewRoom content = builder.setRoom(logRoom).build();
		
		GlobalProto.MessageStream messageStream = (GlobalProto.MessageStream) HandlerUtil.CREATE_G2S_GLOBALMESSAGE(
				LoginProto.G2S_MSGTYPE.G2S_REQUEST_CREATE_NEWROOM, content.toByteString());
	
		this.session().sendMessage(messageStream);
	}
}