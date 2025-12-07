# Use Ubuntu 24.04 (Noble Numbat)
FROM sapmachine:21-jdk-ubuntu-24.04

# Prevent interactive prompts
ENV DEBIAN_FRONTEND=noninteractive

# Update package list and install sqlite3 + sqlcipher
RUN apt-get update &&\
    apt-get install -y sqlite3 sqlcipher && \
    echo -e sqlcipher --version && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY target/*.jar app.jar
COPY unencryptDB.sql unencryptDB.sql
COPY unencryptDB-template.sql unencryptDB-template.sql
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]