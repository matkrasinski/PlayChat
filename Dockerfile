# Step 1: Use sbt to build the application
FROM hseeberger/scala-sbt:11.0.11_1.5.5_2.13.6 AS builder

# Copy the project definition files
COPY ./ /

# Download dependencies
RUN sbt update

# Compile the application
RUN sbt compile

# Package the application
RUN sbt dist

# Step 2: Create a lightweight image for running the application
FROM openjdk:11-jre-slim

# Copy the packaged application from the builder stage
COPY --from=builder /target/universal/playchat-2.1.zip /

# Unzip the application
RUN apt-get update && apt-get install -y unzip && \
    unzip playchat-2.1.zip && \
    mv playchat-2.1/* . && \
    rm -rf playchat-2.1.zip playchat-2.1

# Expose the port on which the Play application runs
EXPOSE 9000

# Define the entry point for the container
ENTRYPOINT ["bin/playchat"]
