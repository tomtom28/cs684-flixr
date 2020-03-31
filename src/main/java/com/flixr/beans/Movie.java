package com.flixr.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Vraj Desai
 */
public class Movie {
    private int movie_id;
    private String title;
    private String releasedate;
    private String agerating;
    private String actors;
    private int runtime;
    private String director;
    private String writer;
    private String poster_url;

    @JsonIgnore
    public int getMovieID(){
        return this.movie_id;
    }
    public void setMovieID(int movieID){
        this.movie_id = movieID;
    }

    @JsonIgnore
    public String getMoviename(){
        return this.title;
    }
    public void setMoviename(String moviename) {
        this.title = moviename;
    }

    @JsonIgnore
    public String getReleasedate() {
        return releasedate;
    }
    public void setReleasedate(String releasedate){
        this.releasedate = releasedate;
    }

    public String getAgerating(){
        return this.agerating;
    }
    public void setAgerating(String agerating){
        this.agerating = agerating;
    }

    @JsonIgnore
    public String getActors(){
        return this.actors;
    }
    public void setActors(String actors){
        this.actors = actors;
    }

    @JsonIgnore
    public int getRuntime(){
        return this.runtime;
    }
    public void setRuntime(int runtime){
        this.runtime = runtime;
    }

    @JsonIgnore
    public String getDirector(){
        return this.director = director;
    }
    public void setDirector(String director){
        this.director = director;
    }

    @JsonIgnore
    public String getWriter(){
        return this.writer;
    }
    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getposter_url(){
        return this.poster_url;
    }
    public void setMoviePosterURL(String moviePosterURL) {
        this.poster_url = moviePosterURL;
    }


    // For Front End
    public int getmovie_id(){
        return this.movie_id;
    }
    public String gettitle(){
        return this.title;
    }


    /**
     * Added by Thomas Thompson
     * Checks if MovieWithPrediction equals another MovieWithPrediction object
     * @param o Object
     * @return  Returns true if type is Movie and all fields are matched
     */
    @Override
    public boolean equals(Object o) {

        if (o instanceof Movie) {

            // Convert to Movie object
            Movie m = (Movie) o;

            // Get all fields
            int movie_id = m.getmovie_id();
            String title = m.gettitle();
            String releasedate = m.getReleasedate();
            String agerating = m.getAgerating();
            String actors = m.getActors();
            int runtime = m.getRuntime();
            String director = m.getDirector();
            String writer = m.getWriter();
            String poster_url = m.getposter_url();

            // Check all fields
            if (this.movie_id != movie_id) return false;
            if (!this.title.equalsIgnoreCase(title)) return false;
            if (!this.releasedate.equalsIgnoreCase(releasedate)) return false;
            if (!this.agerating.equalsIgnoreCase(agerating)) return false;
            if (!this.actors.equalsIgnoreCase(actors)) return false;
            if (this.runtime != runtime) return false;
            if (!this.director.equalsIgnoreCase(director)) return false;
            if (!this.writer.equalsIgnoreCase(writer)) return false;
            if (!this.poster_url.equalsIgnoreCase(poster_url)) return false;

            return true;

        }

        return false;

    }

}
