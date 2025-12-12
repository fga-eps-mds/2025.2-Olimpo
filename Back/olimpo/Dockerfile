# --- Estágio 1: Build (Compilação) ---
# Altere a versão aqui para uma imagem Maven que use OpenJDK 21
FROM maven:3.9-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o pom.xml primeiro para aproveitar o cache de dependências do Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o resto do código-fonte e compila a aplicação
COPY src ./src
RUN mvn package -DskipTests -Dmaven.wagon.http.retryHandler.count=5

# --- Estágio 2: Run (Execução) ---
# Altere a versão aqui para uma imagem JRE (Java Runtime) 21
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia apenas o .jar compilado do estágio de build para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que sua aplicação Spring roda (padrão 8080)
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]