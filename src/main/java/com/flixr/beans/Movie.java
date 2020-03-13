package com.flixr.beans;

public class Movie {
    private int movieID;
    private String moviename;
    private String releasedate;
    private String agerating;
    private String actors;
    private int runtime;
    private String director;
    private String writer;
    private String moviePosterURL;

    public int getMovieID(){
        return this.movieID;
    }
    public void setMovieID(int movieID){
        this.movieID = movieID;
    }
    public String getMoviename(){
        return this.moviename;
    }
    public void setMoviename(String moviename) {
        this.moviename = moviename;
    }
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
    public String getActors(){
        return this.actors;
    }
    public void setActors(String actors){
        this.actors = actors;
    }
    public int getRuntime(){
        return this.runtime;
    }
    public void setRuntime(int runtime){
        this.runtime = runtime;
    }
    public String getDirector(){
        return this.director = director;
    }
    public void setDirector(){
        this.director = director;
    }
    public String getWriter(){
        return this.writer;
    }
    public void setWriter(String writer) {
        this.writer = writer;
    }
    public String getMoviePosterURL() {
        return moviePosterURL;
    }
    public void setDirector(String director) {
        this.director = director;
    }
    public void setMoviePosterURL(String moviePosterURL) {
        this.moviePosterURL = moviePosterURL;
    }
}
