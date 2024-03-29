package com.websockets;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.json.*;



@ServerEndpoint(value="/VitalCheckEndPoint",configurator=VitalCheckconfigurator.class)
public class VitalCheckEndPoint
{
static Set<Session> subscribers=Collections.synchronizedSet(new HashSet<Session>());
	

@OnOpen
public void handleOpen(EndpointConfig endpointconfig,Session userSession)
{
userSession.getUserProperties().put("username",endpointconfig.getUserProperties().get("username"));
subscribers.add(userSession);
}


@OnMessage
public void handleMessage(String message,Session userSession)
{
   String username=(String)userSession.getUserProperties().get("username");
  
   if(username!=null && !username.equals("doctor"))
    {
       subscribers.stream().forEach(x->{
            try {
	 if(x.getUserProperties().get("username").equals("doctor"))
	 {
	      // if(Integer.parseInt(message)< 90)
		 x.getBasicRemote().sendText(buildJSON(username,message));
	  }
	}
	catch(Exception e)
	{
	   e.printStackTrace();
	 }
	 });
	 }
   else if(username!=null && username.equals("doctor"))
    {
     String[] messages=message.split(",");
     String patient=messages[0];
     String subject=messages[1];
     subscribers.stream().forEach(x->{
     try 
    {
        if(subject.equals("ambulance"))
        {
           if(x.getUserProperties().get("username").equals(patient))
	 {
	 x.getBasicRemote().sendText(buildJSON("doctor","has summoned an ambulance"));
	 }
           else if(x.getUserProperties().get("username").equals("ambulance"))
	{
	 x.getBasicRemote().sendText(buildJSON(patient,"Requires an ambulance"+messages[2]));
	 }
          }
       else if(subject.equals("medication"))
         {
             if(x.getUserProperties().get("username").equals(patient))
	{
	 x.getBasicRemote().sendText(buildJSON("doctor",messages[2]+","+messages[3]));
	 }
	
          }
 }
 catch(Exception e)
 {
     e.printStackTrace();
 }
 });
 }
}

@OnClose
public void handleClose(Session userSession)
{
  subscribers.remove(userSession);
 }

 @OnError
  public void handleError(Throwable t)
  {	}
	
  
 private String buildJSON(String username,String message)
{
   JsonObject jsonObject=Json.createObjectBuilder().add("message",username+","+message).build();
   StringWriter stringWriter=new StringWriter();
   try(JsonWriter jsonWriter=Json.createWriter(stringWriter))
     {
         jsonWriter.write(jsonObject);
     }
   return stringWriter.toString();
 }

}