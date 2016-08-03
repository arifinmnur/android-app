package com.kelsos.mbrc.commands.model;

import javax.inject.Inject;
import com.kelsos.mbrc.interfaces.ICommand;
import com.kelsos.mbrc.interfaces.IEvent;
import com.kelsos.mbrc.model.LyricsModel;

public class UpdateLyrics implements ICommand {
  private LyricsModel model;

  @Inject
  public UpdateLyrics(LyricsModel model) {
    this.model = model;
  }

  @Override
  public void execute(IEvent e) {
    model.setLyrics(e.getDataString());
  }
}
