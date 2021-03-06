package io.element.communication.impl;

import io.element.communication.ProtoMsgHandler;
import io.element.gtserver.App;
import io.element.gtserver.GT_Managers;
import io.element.gtserver.GT_RoomManager;
import io.element.protobuf.LoginProto;

import io.element.protobuf.GlobalProto.MESSAGE;
import io.element.protobuf.LoginProto.S2GByteStream;
import io.element.reflect.Handler;
import io.element.room.impl.SessionRoom;
import io.element.room.impl.SessionRoom.ROOM_STATUS;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

public class S2GProto_LoginHandler extends ProtoMsgHandler<LoginProto.S2G_MSGTYPE,LoginProto.S2GByteStream> {

	private static final S2GProto_LoginHandler single = new S2GProto_LoginHandler();  
	 
	private S2GProto_LoginHandler() { m_globalType = MESSAGE.MESSAGE_LOGIN_S2G; }  
	    
	public static S2GProto_LoginHandler getInstance() {  
        return single;  
	}  
	
	{
		RegisterProtoMsgCmd();
	}
	
	@Override
	public void RegisterProtoMsgCmd() {
		super.RegisterProtoMsgCmd();
		
		PROTOMSG_REGISTER_HANDLEREX(LoginProto.S2G_MSGTYPE.S2G_RESP_BEGIN_GAME, 	LoginProto.S2G_CreateNewRoom.class, "CallBack_S2G_Resp_BeginGame");
		PROTOMSG_REGISTER_HANDLEREX(LoginProto.S2G_MSGTYPE.S2G_RESP_CREATE_NEWROOM,	LoginProto.S2G_CreateNewRoom.class, "CallBack_S2G_Resp_CreateNewRoom");
	}
	
	@Override
	public boolean HandlerMsg(ChannelHandlerContext channel, LoginProto.S2GByteStream param) {
				
		LoginProto.S2G_MSGTYPE type = param.getType();
		Handler handler = m_handlers.get(type);
		if( handler == null || !handler.isVaild())
			return false;
		
		// logic call -- 逻辑处理
		Handler tempHandler = null;
		Object re = null;
		try {
			 tempHandler = (Handler) handler.clone();
			 tempHandler.SetObject(this);
			 tempHandler.SetParam(param, channel.channel());
			 re = tempHandler.invoke();
		} catch (CloneNotSupportedException e) {
			App.LOGGER.error( e.toString() );
		}
						
		// logger call -- 日志处理
		handler = m_log_handlers.get(type);
		if(handler == null || !handler.isVaild())
			return true;
			
		try {
			tempHandler = (Handler) handler; 
			tempHandler.SetObject(this);
			tempHandler.SetParam(re, channel.channel());
			tempHandler.invoke();		
		} catch (Exception e) {
			
		}

		return true;
	}
	
	//***********************************************
	// 
	// 协议调用函数，逻辑处理函数
	// 
	//***********************************************
	
	// call back -- S2G_RESP_BEGIN_GAME
	public Object CallBack_S2G_Resp_BeginGame(S2GByteStream msg, SocketChannel channel)
	{
		return null;
	}
	
	// call back -- S2G_RESP_CREATE_NEWROOM
	public Object CallBack_S2G_Resp_CreateNewRoom(S2GByteStream msg, SocketChannel channel) throws CloneNotSupportedException, Exception
	{		
		// 反序列化
		LoginProto.S2G_CreateNewRoom cmd = VAR_CHECK_CMD_FIX(msg.getRequestData(), msg.getType());
					
		// 获取session room， broadcast
		LoginProto.Room room = cmd.getRoom();
		GT_RoomManager mgr = GT_Managers.getRoomManager();
		SessionRoom sRoom  = mgr.getRoomByID(room.getId());
	
		// 激活房间
		sRoom.setStatus(ROOM_STATUS.ACTIVE);
		
		// 广播给玩家
		sRoom.broadcast();
		
		return cmd;
	}
	
	//***********************************************
	// 
	// 协议调用函数，logger处理函数
	// 
	//***********************************************
	
	public void CallBack_S2G_Resp_CreateNewRoom_Logger(Object msg, SocketChannel channel) throws CloneNotSupportedException, Exception
	{		
		LoginProto.S2G_CreateNewRoom cmd = (LoginProto.S2G_CreateNewRoom) msg;
		
		App.LOGGER.info(String.format("logic server-- 逻辑服务器创建房间成功,游戏开始 --gate server，房间id = %d", cmd.getRoom().getId()));
	}
	
	
	//***********************************************
	// the message function called end for handler
	//***********************************************

}
