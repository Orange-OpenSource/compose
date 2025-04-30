FROM dockerproxy.repos.tech.orange/eclipse-temurin:17-jre-focal

RUN apt-get update
RUN apt-get install git ca-certificates curl gnupg -y
RUN mkdir -p /etc/apt/keyrings
RUN curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg
RUN echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_20.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list
RUN apt-get update
RUN apt-get upgrade -y
RUN apt-get install nodejs -y
RUN npm install -g npm

COPY scaas-3.1.0/ ./scaas/
RUN chmod +x scaas/bin/scaas
