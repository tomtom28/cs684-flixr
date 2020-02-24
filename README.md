# Flixr: Movie Recommendation System

Flixr aims to help its users make better choices about the movies that they watch on popular streaming services, regardless of the streaming service to which the movie may belong.

The goal of this application is to improve movie recommendations to our users, while also gaining insights into customer behavior.

For more details please refer to the project's [design document](https://docs.google.com/document/d/1ZgZMWlzvBWPBJsKdvUY9sPhvFudqRSKGNfZaC5qrZB4/edit?usp=sharing).




### Authors

Fangzhou Gou

Thomas Thompson

Vraj Desai

Zion Whitehall



### Cloning Down the Repo

Use any Java IDE, we suggest using IntelliJ:

  - Download [IntelliJ](https://www.jetbrains.com/idea/) Community Edition

  - Using the Import option, select the pom.xml file.

  - IntelliJ will automatically build the dependencies using Maven (built into IntelliJ).

  - Then, open [MySQL workbench](https://www.mysql.com/products/workbench/) and run the queries included in the `database` folder.

  - Please note that the `com.flixr.configuration.ApplicationConstants` class will need to have its MySQL credentials updated to match your environment.


Within the command line, perform the following actions:

  - Navigate to the root folder `cs684-flixr`

  - Run `npm install` to download your dependencies

  - Run `webpack` to compile the ReactJS front-end


Now, going back to your IDE:

  - Navigate to `com.flixr.Application` and click the `public static void main()` method.

  - Click the green play button in IntelliJ to run the project.

  - Finally, open up to [localhost:8080](http://localhost:8080/) in your browser to see the webapp in action.



### References

https://github.com/spring-guides/tut-react-and-spring-data-rest/tree/master/basic

http://girlincomputerscience.blogspot.com/search/label/Recommender%20Systems