package com.github.langebangen.kensa.role;

public enum KensaRole
{
	ADMIN("KensaAdmin");

	private final String roleName;
	KensaRole(String roleName)
	{
		this.roleName = roleName;
	}

	public String GetRoleName()
	{
		return roleName;
	}
}