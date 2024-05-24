FROM openjdk:11
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn dependency:go-offline -B
RUN mvn clean package -DskipTests
EXPOSE 8081
COPY target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar
ENTRYPOINT ["java","-jar","MyTodoList.jar"]




