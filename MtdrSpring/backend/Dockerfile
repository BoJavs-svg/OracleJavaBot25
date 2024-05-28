FROM openjdk:11
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
EXPOSE 8080
COPY target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar
ENTRYPOINT ["java","-jar","MyTodoList.jar"]

# FROM node:14
# WORKDIR /app
# COPY package*.json ./
# RUN npm install
# COPY . .
# EXPOSE 8080
# CMD [ "npm", "start" ]
