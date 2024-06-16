Simple chat application, which uses Scala Play framework

Application is integrated with MongoDB to store user data and chat messages

App uses JWT for user authorization

## Configuration
You need to have at least Java 11 installed on your local machine

Additionally, mongoDB needs to run for the app to properly run

In `conf/application.conf` provide URI to your mongoDB,

example: `mongodb://localhost:27017/mongodb`

Make sure that your `.env` file contains proper `JWT_SECRET` variable set.

## Start

To run the app - `sbt run`

To run tests - `sbt test`
