# Stage de construction (Build stage)
# Utilise une image JDK pour compiler l'application Java.
FROM eclipse-temurin:21-jdk AS build

# Définit le répertoire de travail dans le conteneur pour le stage de build.
WORKDIR /app

# Copie le fichier pom.xml et les fichiers du Maven Wrapper.
# Faire ceci en premier permet à Docker de mettre en cache cette étape.
# Si seul le code source change, Docker ne re-téléchargera pas les dépendances Maven.
COPY pom.xml .
COPY mvnw .             
COPY .mvn .mvn/        

# Rend le script Maven Wrapper exécutable.
RUN chmod +x mvnw

# Copie le code source de l'application.
COPY src ./src

# Exécute la compilation Maven.
# 'package' : compile le code, exécute les tests (sauf si ignorés), et crée le JAR/WAR.
# '-DskipTests' : ignore l'exécution des tests unitaires pour accélérer le build de l'image Docker.
RUN ./mvnw package -DskipTests


# Stage d'exécution (Runtime stage)
# Utilise une image JRE plus légère, car elle n'a pas besoin des outils de compilation.
FROM eclipse-temurin:21-jre

# Crée un utilisateur et un groupe système 'appuser'.
# C'est une bonne pratique de sécurité pour ne pas exécuter l'application en tant que 'root'.
RUN adduser --system --group appuser

# Définit le répertoire de travail pour l'application dans le stage de runtime.
WORKDIR /app

# Copie le fichier JAR compilé du stage 'build' vers le stage 'runtime'.
# --from=build : Spécifie que la source vient du stage nommé 'build'.
# /app/target/*.jar : Le chemin du JAR dans le stage 'build' (où Maven l'a placé).
# app.jar : Le nom sous lequel le JAR sera copié et renommé dans le stage 'runtime'.
COPY --from=build /app/target/*.jar app.jar

# Définit l'utilisateur sous lequel l'application s'exécutera.
USER appuser

# Expose le port sur lequel votre application Spring Boot écoute.
# Assurez-vous que ce port (ex: 8080) correspond à la configuration de votre application.
EXPOSE 8080

# Ajoute une étiquette à l'image pour l'information du mainteneur.
LABEL maintainer="Bassilekin jean 21T2352"

# Définit la commande par défaut qui sera exécutée lorsque le conteneur démarre.
# "java -jar app.jar" lance l'application Spring Boot.
ENTRYPOINT ["java", "-jar", "app.jar"]