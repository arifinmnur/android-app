package com.kelsos.mbrc.commands.visual;

import com.google.inject.Inject;
import com.kelsos.mbrc.data.SocketMessage;
import com.kelsos.mbrc.interfaces.ICommand;
import com.kelsos.mbrc.interfaces.IEvent;
import com.kelsos.mbrc.model.MainDataModel;
import com.kelsos.mbrc.others.Protocol;
import com.kelsos.mbrc.services.SocketService;
import com.kelsos.mbrc.utilities.MainThreadBusWrapper;

public class VisualUpdateHandshakeComplete implements ICommand
{
	@Inject MainThreadBusWrapper bus;
    @Inject SocketService service;
    @Inject MainDataModel model;

	public void execute(IEvent e)
	{

        boolean isComplete = (Boolean)e.getData();
        model.setHandShakeDone(isComplete);

        if(!isComplete) return;
        service.sendData(new SocketMessage(Protocol.PlayerStatus,Protocol.Request, ""));
        service.sendData(new SocketMessage(Protocol.NowPlayingTrack, Protocol.Request, ""));
        service.sendData(new SocketMessage(Protocol.NowPlayingCover, Protocol.Request, ""));
        service.sendData(new SocketMessage(Protocol.NowPlayingLyrics, Protocol.Request, ""));
        service.sendData(new SocketMessage(Protocol.NowPlayingPosition, Protocol.Request, ""));
	}
}


