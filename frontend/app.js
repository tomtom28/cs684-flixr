     //jshint esversion:6
var express=require("express");
var bodyParser=require("body-parser");
var https=require("https");
var http=require("http");

var app=express();
app.set("view engine","ejs");
app.use(express.static("public"));
app.use(bodyParser.urlencoded({extended:true}));

var userid;

app.get("/",function(req,res){
  res.render("home");
});

app.get("/signin",function(req,res){
  res.render("signin");
});

app.get("/signup",function(req,res){
  res.render("signup");
});


app.get("/rating",function(req,res){
  http.get("http://localhost:3001/api",function(getback){

    getback.on("data",function(data){
      var i=JSON.parse(data);
      console.log(i);
    });

  });
  res.render("rating");
});

app.get("/recommend",function(req,res){
  res.render("recommend");
});

app.get("/admin",function(req,res){
  res.render("admin");
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
