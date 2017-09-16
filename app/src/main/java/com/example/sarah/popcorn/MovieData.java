package com.example.sarah.popcorn;

import java.io.Serializable;

/**
 * Created by Sarah on 8/13/2016.
 */
public class MovieData implements Serializable {

    String title,
            releaseDate,
            poster,
            voteAverage,
            plot,
            id;
    String[] trailersName, trailersKey, reviewsContent, reviewsAuthor;
    MovieData(){
        //this.poster="http://image.tmdb.org/t/p/w185";
    }

    public String getTitle(){
        return this.title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getReleaseDate(){
        return this.releaseDate;
    }
    public void setReleaseDate(String releaseDate){
        this.releaseDate = releaseDate;
    }
    public String getPoster(){
        return this.poster;
    }
    public void setPoster(String poster){
        if(this.poster!=null)
            this.poster += poster;
        else
            this.poster = poster;
    }
    public String getVoteAverage(){
        return this.voteAverage;
    }
    public void setVoteAverage(String voteAverage){
        this.voteAverage = voteAverage;
    }
    public String getPlot(){
        return this.plot;
    }
    public void setPlot(String plot){
        this.plot = plot;
    }
    public String[] getTrailersName(){
        return this.trailersName;
    }
    public void setTrailersName(String[] trailersName){
        this.trailersName = trailersName;
    }
    public String[] getTrailersKey(){
        return this.trailersKey;
    }
    public void setTrailersKey(String[] trailersKey){
        this.trailersKey = trailersKey;
    }
    public String[] getReviewsContent(){
        return this.reviewsContent;
    }
    public void setReviewsContent(String[] reviewsContent){
        this.reviewsContent = reviewsContent;
    }
    public String[] getReviewsAuthor(){
        return this.reviewsAuthor;
    }
    public void setReviewsAuthor(String[] reviewsAuthor){
        this.reviewsAuthor = reviewsAuthor;
    }
    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id= id;
    }
    public void makePosterURL(){this.poster = "http://image.tmdb.org/t/p/w185";}


}

