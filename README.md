# Flixr: Movie Recommendation System

Flixr aims to help its users make better choices about the movies that they watch on popular streaming services, regardless of the streaming service to which the movie may belong.

The goal of this application is to improve movie recommendations to our users, while also gaining insights into customer behavior.

For more details please refer to the project's [design document](https://docs.google.com/document/d/1ZgZMWlzvBWPBJsKdvUY9sPhvFudqRSKGNfZaC5qrZB4/edit?usp=sharing).




### Authors

Fangzhou Guo

Thomas Thompson

Vraj Desai

Zion Whitehall



### Setting up the Project

#### API Service & Recommendation Engine Setup
Use any Java IDE, we suggest using IntelliJ:

  - Download Java JDK 8. You can use this [website](https://java.com/en/download/)
  
  - Download [IntelliJ](https://www.jetbrains.com/idea/) Community Edition

  - IntelliJ should automatically build the dependencies using Maven and the pom.xml file.

  - Then, open [MySQL workbench](https://www.mysql.com/products/workbench/) and run the queries included in the `database` folder.

  - Please note that the `com.flixr.configuration.ApplicationConstants` class will need to have its MySQL credentials updated to match your environment.


#### Frontend Setup
Ensure you have NodeJS installed:
  - Download [NodeJS](https://nodejs.org/en/) if needed.
  
Within the command line, perform the following actions:

  - Navigate to the frontend project folder `cs684-flixr/frontend`

  - Run `npm install` to download your dependencies


### Running the Application
You will need to run both webservices to use the web application.

#### Run the Java API:
Within your IDE:

  - Navigate to `com.flixr.Application` and click the `public static void main()` method.

  - Click the green play button in IntelliJ to run the project.
  
  - The API will be running GET and POST requests on [localhost:3001](http://localhost:3001/)


#### Run the NodeJS Frontend:

Within your commmand line:

  - Navigate to the frontend project folder `cs684-flixr/frontend`
  
  - Open up to [localhost:3000](http://localhost:3000/) in your browser to see the webapp in action.


### References

https://github.com/spring-guides/tut-react-and-spring-data-rest/tree/master/basic

http://girlincomputerscience.blogspot.com/search/label/Recommender%20Systems
