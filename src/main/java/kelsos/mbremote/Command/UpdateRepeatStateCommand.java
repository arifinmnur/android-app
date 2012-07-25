package kelsos.mbremote.Command;

import com.google.inject.Inject;
import kelsos.mbremote.Interfaces.ICommand;
import kelsos.mbremote.Interfaces.IEvent;
import kelsos.mbremote.Models.MainDataModel;

public class UpdateRepeatStateCommand implements ICommand
{
	@Inject private MainDataModel model;
	public void execute(IEvent e)
	{
		model.setRepeatState(e.getData());
	}
}