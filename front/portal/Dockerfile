FROM dockerproxy.repos.tech.orange/node:16-alpine

#docker run -it -p 8080:8080 -e NODE_ENV='production' -e SERVER_PORT=8080 -e SCAAS_URL='http://compose:9000' -e JWT_SECRET='Pas!sific1' compose
#ENV NODE_ENV='production'
#ENV SERVER_PORT=8080
#ENV SCAAS_URL='http://compose:9000'
#ENV JWT_SECRET='Pas!sific1'

# définit le dossier 'app' comme dossier de travail
WORKDIR /app

# copie 'package.json' et 'package-lock.json' (si disponible)
COPY package*.json ./

# installe les dépendances du projet
RUN npm install

# copie les fichiers et dossiers du projet dans le dossier de travail (par exemple : le dossier 'app')
COPY . .

# construit l'app pour la production en la minifiant
RUN npm run build

EXPOSE 8080

# Define application entry point in shell form
ENTRYPOINT node .
