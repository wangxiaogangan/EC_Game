package io.element.cmd;

import io.element.protobuf.LoginProto;
import io.element.protobuf.SimpleProto;
import io.element.protobuf.GlobalProto.MESSAGE;
import io.element.room.impl.BaseDispatcher;
import io.element.room.impl.GM_BaseRoom;
import io.element.room.impl.GM_SimpleRoom;
import io.element.util.HandlerUtil;

public class S2G {

	public static void Cmd_Resp_CreateNewRoom( GM_BaseRoom room, LoginProto.CREATEROOM_STATUS status )
	{
		LoginProto.Room bRoom = new HandlerUtil().getLoginRoom(room);
		
		LoginProto.S2G_CreateNewRoom.Builder mBuilder = LoginProto.S2G_CreateNewRoom.newBuilder();
		LoginProto.S2G_CreateNewRoom s2g = mBuilder.setRoom(bRoom).setFlag(status).build();
		
		LoginProto.S2GByteStream buffer = HandlerUtil.CREATE_S2G_LOGINMESSAGE(LoginProto.S2G_MSGTYPE.S2G_RESP_CREATE_NEWROOM, s2g.toByteString());	
		Object msg = HandlerUtil.CREATE_S2G_GLOBALMESSAGE(MESSAGE.MESSAGE_LOGIN_S2G, buffer.toByteString());
		
		room.session().sendMessage(msg);
	}
	
	public static void Cmd_Notify_GlobalState_Simple(GM_SimpleRoom room, SimpleProto.PHASE_TYPE phaseType, SimpleProto.S2G_MSGTYPE msgType)
	{
		SimpleProto.S2G_NotifyPhaseBegin.Builder builder = SimpleProto.S2G_NotifyPhaseBegin.newBuilder();
		BaseDispatcher dispatcher = room.getMessageDispatcher();
		
		SimpleProto.Room sRoom = new HandlerUtil().getSimpleRoom(room);
		SimpleProto.S2G_NotifyPhaseBegin msg = builder.setPhase(phaseType)
					  								  .setLast( (int) dispatcher.DelayedTime())
					  								  .setRoom(sRoom)
					  								  .build();

		SimpleProto.S2GByteStream buffer = HandlerUtil.CREATE_S2G_SIMPLEMESSAGE( msgType , msg.toByteString());
		room.session().sendMessage(HandlerUtil.CREATE_S2G_GLOBALMESSAGE( MESSAGE.MESSAGE_SIMPLE_S2G , buffer.toByteString()));
	}
	
}
