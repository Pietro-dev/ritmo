# Defini construção da aplicação com maven e java 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# copia o código-fonte e gera o .jar
COPY src ./src
RUN mvn clean package -DskipTests -Djacoco.skip=true

# Imagem final leve para execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o artefato gerado (AplicacaoRitmo-0.0.1-SNAPSHOT.jar)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Limita o uso de memória ram para evitar estouro no plano grátis do render 512 mb
ENTRYPOINT ["java", "-Xmx384m", "-Xms128m", "-jar", "app.jar"]