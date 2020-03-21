     //jshint esversion:6
     //created by Fangzhou Guo
var express=require("express");
var bodyParser=require("body-parser");
var https=require("https");
var http=require("http");
var request = require('request');

var app=express();
app.set("view engine","ejs");
app.use(express.static("public"));
app.use(bodyParser.urlencoded({extended:true}));

var user_email="g@m.com";
var url="http://localhost:3001";
var status;
var movie_rate_count=0;

app.get("/",function(req,res){
  res.render("home");
});

app.get("/signin",function(req,res){
  res.render("signin");
});

app.post("/signin",function(req,res){
  //console.log(req.body);
  user_email=req.body.email;
  request.post(
    url+"/signin",
    {form:req.body},
    function (error, response, body) {
        if (!error && response.statusCode == 200) {
            console.log(body);
        }
    }
  );
  res.redirect("/");
});

app.get("/signup",function(req,res){
  res.render("signup");
});

app.post("/signup",function(req,res){
  user_email=req.body.email;
  console.log(req.body);
  request.post(
    url+"/signup",
    {form:req.body},
    function (error, response, body) {
        if (!error && response.statusCode == 200) {
            console.log(body);
        }
    }
  );
  res.redirect("/");
});

app.get("/logout",function(req,res){
  http.get(url+"/logout/"+user_email,function(getback){
    getback.on("data",function(data){
      var i=JSON.parse(data);
      console.log(i);
    });
  });
  res.redirect("/");
});

app.get("/checkstatus",function(req,res){
  http.get(url+"/checkstatus/"+user_email,function(getback){
    getback.on("data",function(data){
      var i=JSON.parse(data);
      console.log(i);
    });
  });
  res.redirect("/");
});

app.get("/rating",function(req,res){

  http.get(url+"/rating/"+user_email+"/"+movie_rate_count,function(getback){
    getback.on("data",function(data){
      var i=JSON.parse(data);
    //  console.log(i);
      res.render("rating",{movie:i});
    });
  });
});

app.get("/rating/prev",function(req,res){
    if(movie_rate_count-1<0){
      movie_rate_count=movie_rate_count+9;
      res.redirect("/rating");
    }else{
      movie_rate_count=(movie_rate_count-1)%10;
      res.redirect("/rating");
    }

});

app.get("/rating/next",function(req,res){
    movie_rate_count=(movie_rate_count+1)%10;
    res.redirect("/rating");
});

app.post("/rating/:id",function(req,res){
  // console.log(req.params.id);
  // console.log(req.body);
  var grade=0;
  if(req.body.star1=='on'){
    grade=1;
  }else if(req.body.star2=='on'){
    grade=2;
  }else if(req.body.star3=='on'){
    grade=3;
  }else if(req.body.star4=='on'){
    grade=4;
  }else if(req.body.star5=='on'){
    grade=5;
  }

  var object={
    user_email:user_email,
    movie_id:req.params.id,
    grade:grade
  };

  request.post(
    url+"/rating/"+user_email,
    {form:object},
    function (error, response, body) {
        if (!error && response.statusCode == 200) {
            console.log(body);
        }
    }
  );

  res.redirect("/rating/next");
});



app.get("/recommend/:sort_type",function(req,res){

  http.get(url+"/recommend/"+user_email+"/"+req.params.sort_type,function(getback){

    getback.on("data",function(data){
      var i=JSON.parse(data);
      //console.log(i);
      res.render("recommend",{items:i});
    });
  });



});

app.get("/admin/:sort_type",function(req,res){
  http.get(url+"/admin/analyze/"+req.params.sort_type,function(getback){

    getback.on("data",function(data){
      var i=JSON.parse(data);
      console.log(i);
      res.render("admin",{items:i});
    });
  });
});

app.post("/admin/newmovie",function(req,res){
  //console.log(req.body);
  request.post(
    url+"/admin/newmovie",
    {form:req.body},
    function (error, response, body) {
        if (!error && response.statusCode == 200) {
            console.log(body);
        }
        //res.redirect("/admin/rating");
    }
  );
  res.redirect("/admin/rating");
});

app.get("/re_train",function(req,res){
  http.get(url+"/admin/re_train");
  res.redirect("/admin/rating");
});

app.get("/about",function(req,res){
  res.render("about");
});

app.get("/contact",function(req,res){
  res.render("contact");
});

app.listen(process.env.PORT||3000, function() {
  console.log("Server started on port 3000~");
});
