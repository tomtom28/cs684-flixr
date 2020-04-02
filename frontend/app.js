     //jshint esversion:6
     //created by Fangzhou Guo
var express=require("express");
var bodyParser=require("body-parser");
var https=require("https");
var http=require("http");
var request = require('request');
var session = require('express-session');
var passport = require("passport");
var LocalStrategy = require("passport-local").Strategy;

var app=express();
app.set("view engine","ejs");
app.use(express.static("public"));
app.use(bodyParser.urlencoded({extended:true}));


app.use(session({
  secret: "hello.",
  resave: false,
  saveUninitialized: false
}));
app.use(passport.initialize());
app.use(passport.session());
passport.serializeUser(function(user, done) {
  done(null, user);
});
passport.deserializeUser(function(user, done) {
  done(null, user);
  // User.findById(id, function(err, user) {
  //   done(err, user);
  // });
});

app.use(function(req,res,next){
  //res.locals is the objects to pass in, req.user return current user.
  res.locals.currentUser=req.user;
  next();
});

passport.use(new LocalStrategy({
    usernameField: 'email',
    passwordField: 'password'
  },
  function(username, password, done) {
    //post signin info to backend
    //user_email=username;

    request.post(
      url+"/signin",
      {form:{email:username,password:password}},
      function (error, response, body) {
          if (!error && response.statusCode == 200) {
              console.log(body);
          }
      }
    );
    var user
    ={username:username,password:password,movie_rate_count:0};
    http.get(url+"/checkstatus/"+username,function(getback){
      getback.on("data",function(data){
        var i=JSON.parse(data);
        console.log(i);
        user_id=i.user_id;
        console.log(user_id);

        if(i.status=="off"){
          return done(null,false);
        }
        else if(i.status=="on"){
          return done(null,user);
        }
      });
    });

  }
));

var url="http://localhost:3001";

//home page
app.get("/",function(req,res){
  if (req.isAuthenticated()){
    res.render("home",{message:"on"});
  } else {
    res.render("home",{message:"off"});
  }
});

//get the signin page
app.get("/signin",function(req,res){
  res.render("signin");
});

app.post('/signin',
  passport.authenticate('local',
  {    successRedirect: '/',
  failureRedirect: '/signin' })
);

//get signup page
app.get("/signup",function(req,res){
  res.render("signup");
});

//post signup info to server
app.post("/signup",function(req,res){

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
  //res.redirect("/");
  var user
  ={username:req.body.email,password:req.body.password,movie_rate_count:0};
  req.login(user, function(err) {
  if (err) { return next(err); }
  return res.redirect('/' );
  });
});

//logout
app.get("/logout",function(req,res){
  if (req.isAuthenticated()){
    http.get(url+"/checkstatus/"+req.user.username,function(getback){
      getback.on("data",function(data){
        var i=JSON.parse(data);

        http.get(url+"/logout/"+i.user_id,function(getback){
          getback.on("data",function(data){
            var i2=JSON.parse(data);
            console.log(i2);
          });
        });

      });
    });
    req.logout();
    res.redirect("/");
  } else {
    res.render("home",{message:"off"});
  }

});

//check statuse
app.get("/checkstatus",function(req,res){
  http.get(url+"/checkstatus/"+req.user.username,function(getback){
    getback.on("data",function(data){
      var i=JSON.parse(data);
      console.log(i);
      user_id=i.user_id;
      console.log(user_id);
    });
  });
  res.redirect("/");
});

//get the rating data
app.get("/rating",function(req,res){
  if (req.isAuthenticated()){
    http.get(url+"/checkstatus/"+req.user.username,function(getback){
      getback.on("data",function(data){
        var i=JSON.parse(data);
        console.log(req.user.movie_rate_count);

        http.get(url+"/rating/"+i.user_id+"/"+req.user.movie_rate_count,function(getback){
          getback.on("data",function(data){
            var i1=JSON.parse(data);
          //  console.log(i);
            res.render("rating",{movie:i1});
          });

        });
      });
    });
  } else {
    res.render("signin");
  }

});

//rating data go previous
app.get("/rating/prev",function(req,res){
    if(req.user.movie_rate_count-1<0){
      req.user.movie_rate_count=0;
      res.redirect("/rating");
    }else{
      req.user.movie_rate_count=req.user.movie_rate_count-1;
      res.redirect("/rating");
    }

});

//rating data go next
app.get("/rating/next",function(req,res){
    req.user.movie_rate_count=req.user.movie_rate_count+1;
    res.redirect("/rating");
});

//post rating data to server
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

  http.get(url+"/checkstatus/"+req.user.username,function(getback){
    getback.on("data",function(data){
      var i=JSON.parse(data);

      var object={
        user_id:i.user_id,
        movie_id:req.params.id,
        grade:grade
      };

      request.post(
        url+"/rating",
        {form:object},
        function (error, response, body) {
            if (!error && response.statusCode == 200) {
                console.log(body);
            }
        }
      );

    });
  });



  res.redirect("/rating/next");
});

//get the recommended movie from server
app.get("/recommend/:sort_type",function(req,res){

  if (req.isAuthenticated()){
    http.get(url+"/checkstatus/"+req.user.username,function(getback){
      getback.on("data",function(data){
        var i1=JSON.parse(data);

        http.get(url+"/recommend/"+i1.user_id+"/"+req.params.sort_type,function(getback){

          getback.on("data",function(data){
            var i=JSON.parse(data);
            //console.log(i);
            console.log(i.length);

            var perPage=12;
            var pageQuery=parseInt(req.query.page);
            var pageNumber=pageQuery?pageQuery:1;

            pageQuery--;

            var items=
            i.slice(perPage*pageQuery,perPage*pageQuery+perPage);

            res.render("recommend",{
              items:items,
              current:pageNumber,
              pages:Math.ceil(i.length/perPage),
              type:req.params.sort_type
            });

          });
        });

      });
    });
  } else {
    res.render("signin");
  }
});

//get the admin page
app.get("/admin/:sort_type",function(req,res){
  http.get(url+"/admin/analyze/"+req.params.sort_type,function(getback){

    getback.on("data",function(data){
      var i=JSON.parse(data);

      console.log(i.length);

      var perPage=20;
      var pageQuery=parseInt(req.query.page);
      var pageNumber=pageQuery?pageQuery:1;

      pageQuery--;

      var items=
      i.slice(perPage*pageQuery,perPage*pageQuery+perPage);

      res.render("admin",{
        items:items,
        current:pageNumber,
        pages:Math.ceil(i.length/perPage),
        type:req.params.sort_type
      });

      // res.render("admin",{items:items,current:pageNumber,
      //       pages:Math.ceil(count/perPage)});


    });
  });
});

//add new moives
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

//re_train the dataset
app.get("/re_train",function(req,res){
  http.get(url+"/admin/re_train");
  res.redirect("/admin/rating");
});

//about page
app.get("/about",function(req,res){
  res.render("about");
});

//contact page
app.get("/contact",function(req,res){
  res.render("contact");
});

app.post("/rating/search/movie",function(req,res){
 var obj={user_email:req.user.username , movie_rate_count:req.user.movie_rate_count+1, movie_name:req.body.search};
  request.post(
    url+"/nextorate",
    {form:obj},
    function (error, response, body) {
        if (!error && response.statusCode == 200) {
            console.log(body);
        }
    }
  );
  console.log(obj);
  res.redirect("/rating/next");
});

app.listen(process.env.PORT||3000, function() {
  console.log("Server started on port 3000~");
});
