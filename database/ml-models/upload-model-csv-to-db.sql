-- Author: Thomas Thompson
-- Code is used to insert the trained Model from the Recommendation Model (in CSV format) into the database

USE flixr;

DROP TABLE IF EXISTS RecEngineModel;

CREATE TABLE RecEngineModel(
	MovieIDi int NOT NULL,
	MovieIDj int NOT NULL,
	AvgDifference float(53),
	PRIMARY KEY (MovieIDi, MovieIDj)
);

SHOW VARIABLES LIKE 'secure_file_priv';
-- Use this path in the query below...
-- ex. mine was 'C:\ProgramData\MySQL\MySQL Server 5.7\Uploads'


LOAD DATA LOCAL INFILE 'C:\\ProgramData\\MySQL\\MySQL Server 5.7\\Uploads\\model-ml-small-ratings.csv' INTO TABLE recenginemodel
	FIELDS TERMINATED BY ','
    LINES TERMINATED BY '\r\n'
    IGNORE 1 LINES;
-- insert CSV to DB


-- Verify Results:
-- Use Bash: `wc -l model-ml-small-ratings.csv` to get # of lines in CSV file
-- # of DB records inserted should be in file should be 1 less than # of lines in CSV file