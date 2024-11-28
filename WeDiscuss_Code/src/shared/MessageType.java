package shared;

public enum MessageType {
	LOGIN, // Login request
	LOGOUT, // Logout request
	ADDUSER, // Administrator add user account request
	DELUSER, // Administrator delete user account request
	GETDEL, // Message Informing user of account deletion 
	CPWD, // Change password request
	GUL, // Get user log
	GCL, // Get Chat room log
	CC, // Create Chat room
	IUC, // Invite User to Chat room
	JC, // Join Chat room
	LC, // Leave Chat room
	UTU, // User to User
	UTC, // User to Chat room
	UPDATEUM, // Any data updates from server->client about Usermap
	UPDATECM, // Any data updates from server->client aboud Chatroommap
}
