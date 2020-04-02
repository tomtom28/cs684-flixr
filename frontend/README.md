//Created by Fangzhou Guo
//This is my first time to define an API manual, not sure if it useable
//please contact me, if anything wrong or need to improve.

API manual:

Signup:   
POST    "http://localhost:3001/signup"
Send JSON: {email:xxx, fullname:xxx, password:xxx, age:xxx, country:xxx}

Signin:
POST    "http://localhost:3001/signin"
Send JSON: {email:xxx, pass word:xxx}

Logout:
GET     "http://localhost:3001/logout/user_id"
Receive JSON:{email:xxx, status:xxx}

Check status:
GET     "http://localhost:3001/checkstatus/user_email"
Receive JSON:{email:xxx, status:xxx}

To get the data to rate:
GET     "http://localhost:3001/rating/user_id/#0~unlimited"
Receive JSON:{movie_id:xxx, title:xxx, poster_url:xxx}

Post the rating data to the server:
POST    "http://localhost:3001/rating"
Send JSON: {user_id:xxx, movie_id:xxx, grade:xxx}

Get recommended movie from sever:
GET     "http://localhost:3001/recommend/user_id/sort_type"
Receive JSON:[{movie_id:xxx, title:xxx, poster_url:xxx,},{},{}...]

Post a new movie to server:
POST    "http://localhost:3001/admin/newmovie"
Send JSON: {movie_id:xxx}

Re-train the dataset:
GET     "http://localhost:3001/admin/re_train"

Get the Analyze Ratings:
GET     "http://localhost:3001/admin/analyze/sort_type"
Receive JSON:[{movie_id:xxx, title:xxx, count:xxx, rating:xxx},{},{}...]

User searched a new movie to rate:
POST    "http://localhost:3001/nextorate"
Send JSON: {user_email:xxx, movie_rate_count:xxx, movie_name:xxx}
Explain:when user search a new movie, server should set the new movie as the next movie to rate.
For example, currently the user is rating his 4th movie, and he searches a new movie, then the sever should feed back the new movie as his 5th movie to rate.

Note: when user_email== admin@admin, the admin page will show up.
