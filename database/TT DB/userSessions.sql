-- author: Thomas Thompson
-- Tracks whether a given UserID has a log in session
-- An entry in this table means that they are logged in

USE flixr;

CREATE TABLE userSessions (
	userID INT(11),
    PRIMARY KEY (userID),
    FOREIGN KEY (userID) REFERENCES users(userID)
);