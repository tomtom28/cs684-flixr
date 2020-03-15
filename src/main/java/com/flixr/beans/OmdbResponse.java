package com.flixr.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Thompson
 *
 * Used to capture the OMDB API response that is retrieved from the REST API Service
 * This will automatically parse the JSON response and generate an object
 */
public class OmdbResponse {

    @JsonProperty("imdbID")
    private String imdbId;

    @JsonProperty("Title")
    private String movieTitle;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Rated")
    private String ageRating;

    @JsonProperty("Released")
    private String releaseDate;

    @JsonProperty("Poster")
    private String moviePosterURL;

    @JsonProperty("Writer")
    private String writer;

    @JsonProperty("Actors")
    private String actors;

    @JsonProperty("Runtime")
    private String runTime;

    @JsonProperty("Director")
    private String director;


    // Below this point are additional fields from the API:
    // -----------------------------------------------------------------------------------------------------------------

    @JsonProperty("Genre")
    private String genre;

    @JsonProperty("Plot")
    private String plot;

    @JsonProperty("Language")
    private String language;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("Awards")
    private String awards;

    @JsonProperty("Metascore")
    private String metascore;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("imdbVotes")
    private String imdbVotes;

    @JsonProperty("DVD")
    private String dvdReleaseDate;

    @JsonProperty("BoxOffice")
    private String boxOfficeEarnings;


    // Getter Methods:
    // -----------------------------------------------------------------------------------------------------------------


    public String getImdbId() {
        return imdbId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    /**
     * Some movies had a strange "-" in the year field from the API
     * This will convert the String to an int year (and remove the "-") if needed
     * @return  Year as an Integer (4 digits)
     */
    public int getYear() {
        int lengthOfYear = year.length();
        if (lengthOfYear > 4) {
            year = year.substring(0,5);
        }
        return Integer.parseInt(year);
    }

    public String getAgeRating() {
        return ageRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getMoviePosterURL() {
        return moviePosterURL;
    }

    public String getWriter() {
        return writer;
    }

    public String getActors() {
        return actors;
    }

    public String getRunTime() {
        return runTime;
    }

    public String getDirector() {
        return director;
    }

    public String getGenre() {
        return genre;
    }

    public String getPlot() {
        return plot;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getAwards() {
        return awards;
    }

    public String getMetascore() {
        return metascore;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    public String getImdbVotes() {
        return imdbVotes;
    }

    public String getDvdReleaseDate() {
        return dvdReleaseDate;
    }

    public String getBoxOfficeEarnings() {
        return boxOfficeEarnings;
    }
}
