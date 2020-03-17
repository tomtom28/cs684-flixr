//created by Fangzhou Guo

API manual:

Signup:   
POST    "http://localhost:3001/signup"
Send JSON: {email:xxx, fullname:xxx, password:xxx, age:xxx, country:xxx}

Signin:    
POST    "http://localhost:3001/signin"
Send JSON: {email:xxx, password:xxx}

Logout:    
GET        "http://localhost:3001/logout/user_email"
Receive JSON:{email:xxx, status:xxx}

Check status:
GET        "http://localhost:3001/checkstatus/user_email"
Receive JSON:{email:xxx, status:xxx}

To get the data to rate:
GET          "http://localhost:3001/rating/user_email/#0~9"
Receive JSON:{movie_id:xxx, title:xxx, poster_url:xxx}

Post the rating data to the server:
POST        "http://localhost:3001/rating/user_email"
Send JSON: {user_email:xxx, movie_id:xxx, grade:xxx}

Get recommended movie from sever:
GET          "http://localhost:3001/recommend/user_email/sort_type"
Receive JSON:[{movie_id:xxx, title:xxx, poster_url:xxx,},{},{}...]

Post a new movie to server:
POST          "http://localhost:3001/admin/newmovie"
Send JSON: {movie_id:xxx}

Re-train the dataset:
GET              "http://localhost:3001/admin/re_train"

Get the Analyze Ratings:
GET              "http://localhost:3001/admin/analyze/sort_type"
Receive JSON:[{movie_id:xxx, title:xxx, count:xxx, rating:xxx},{},{}...]
