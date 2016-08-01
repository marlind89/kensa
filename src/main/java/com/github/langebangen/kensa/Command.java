package com.github.langebangen.kensa;

/**
 * Command containing an {@link Action} with the specified argument (if any)
 *
 * @author langen
 */
public class Command
{
	private final Action action;
	private final String argument;

	/**
	 * Constructor.
	 *
	 * @param action
	 *      the {@link Action}
	 * @param argument
	 *      the argument
	 */
	public Command(Action action, String argument)
	{
		this.action = action;
		this.argument = argument;
	}

	/**
	 * Gets the {@link Action}
	 *
	 * @return action
	 *      the {@link Action}
	 */
	public Action getAction()
	{
		return action;
	}

	/**
	 * Gets the argument.
	 *
	 * @return argument
	 *      the argument
	 */
	public String getArgument()
	{
		return argument;
	}

	/**
	 * Parses the specified value into a {@link Command}
	 * and returns it.
	 *
	 * @param value
	 *      the value to parse.
	 *
	 * @return command
	 *      the {@link Command}, or null if it was not
	 *      possible to parse the specified value.
	 */
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