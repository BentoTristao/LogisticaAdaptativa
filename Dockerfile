# --- ESTÁGIO 1: Compilação ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# --- ESTÁGIO 2: Execução ---
FROM eclipse-temurin:17-jre

# Instala suporte a fontes, GTK, OpenGL e X11 necessários para a janela do Processing
RUN apt-get update && apt-get install -y \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-dri \
    libgl1-mesa-glx \
    libgles2-mesa \
    libegl1-mesa \
    libgtk-3-0 \
    x11-apps \
    fonts-dejavu \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Pasta onde os arquivos XML de entrada/saída serão mapeados
RUN mkdir -p /app/data && chmod 777 /app/data

# Busca o arquivo jar gerado com as dependências embutidas
COPY --from=build /app/target/*-with-dependencies.jar /app/app.jar

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
