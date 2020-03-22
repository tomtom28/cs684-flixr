-- Author: Thomas Thompson
-- Used to Create MovieStats Data for Admin Page

USE flixr;

-- Step 1: Create View for RatingCounts
CREATE VIEW RatingCounts AS
SELECT imdbId, COUNT(*) AS RatingCount FROM ratings GROUP BY imdbId;

-- Step 2: Create View for AverageRatings
CREATE VIEW AverageRatings AS
SELECT imdbId, AVG(Rating) AS AverageRating FROM ratings GROUP BY imdbId;

-- Step 3: Create View for MovieStats
CREATE VIEW MovieStats AS
SELECT m.MovieId, m.MovieName, r.RatingCount, a.AverageRating
FROM movies m, RatingCounts r, AverageRatings a
WHERE m.MovieId = r.ImdbId AND m.MovieId = a.ImdbId;


-- Query these views for DAO
SELECT * FROM MovieStats ORDER BY MovieName ASC;
SELECT * FROM MovieStats ORDER BY MovieName DESC;
SELECT * FROM MovieStats ORDER BY RatingCount DESC;
SELECT * FROM MovieStats ORDER BY AverageRating DESC;
