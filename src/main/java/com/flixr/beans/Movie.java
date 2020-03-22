package com.flixr.beans;

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

    public int getMovieID(){
        return this.movie_id;
    }
    public void setMovieID(int movieID){
        this.movie_id = movieID;
    }
    public String getMoviename(){
        return this.title;
    }
    public void setMoviename(String moviename) {
        this.title = moviename;
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
    public void setDirector(String director){
        this.director = director;
    }
    public String getWriter(){
        return this.writer;
    }
    public void setWriter(String writer) {
        this.writer = writer;
    }
//    public String getMoviePosterURL() {
//        return poster_url;
//    }
    public void setMoviePosterURL(String moviePosterURL) {
        this.poster_url = moviePosterURL;
    }


    // For Front End
    public int getmovie_id(){
        return this.movie_id;
    }
    public String getposter_url(){
        return this.poster_url;
    }
    public String gettitle(){
        return this.title;
    }

}
