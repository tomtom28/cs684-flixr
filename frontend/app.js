//jshint esversion:6
var express=require("express");
var bodyParser=require("body-parser");

var app=express();
app.set("view engine","ejs");
app.use(express.static("public"));
app.use(bodyParser.urlencoded({extended:true}));

var movie_list=[{title:"dog1",image:"http://all4desktop.com/data_images/original/4243865-dog.jpg",description:"gold hair"},{title:"dog2",image:"http://all4desktop.com/data_images/original/4243865-dog.jpg",description:"gold hair"},{title:"dog3",image:"http://all4desktop.com/data_images/original/4243865-dog.jpg",description:"gold hair"}];

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
  res.render("rating",{list:movie_list});
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
  console.log("Server started on port 3003~");
});
