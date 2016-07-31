package com.github.langebangen.kensa;

/**
 * @author langen
 */
public class Command
{
	private final Action action;
	private final String argument;

	public Command(Action action, String argument)
	{
		this.action = action;
		this.argument = argument;
	}

	public Action getAction()
	{
		return action;
	}

	public String getArgument()
	{
		return argument;
	}

	public static Command parseCommand(String value)
	{
		if(!value.isEmpty())
		{
			String[] commands = value.split(" ");
			String actionString = commands[0];
			Action action = Action.getAction(actionString);
			if(action != null)
			{
				return new Command(action, commands.length > 1
						? value.substring(1 + actionString.length() + value.indexOf(commands[0]))
						: null);
			}
		}

		return null;
	}
}