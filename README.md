# Simulação de Co-Evolução e Busca em Grafos

Projeto desenvolvido em **Java** (com interface gráfica baseada em **Processing**) que simula a co-evolução entre entregadores (agentes que utilizam algoritmos de busca como A* e Dijkstra) e obstáculos dinâmicos em um labirinto modelado como grafo, contando com interface gráfica e visualização em tempo real.

---

## 📋 Pré-requisitos

Certifique-se de ter instalado em sua máquina:

- **Java Development Kit (JDK)** (versão 11 ou superior recomendada)
- **Apache Maven** *(necessário para baixar a dependência gráfica `processing-core` via `pom.xml`, caso ainda não a possua em `~/.m2`)*

---

## 📁 Estrutura do Projeto

- `src/`: Código-fonte em Java (`Main.java` é a classe principal).
- `data/`: Arquivos de configuração do sistema (`config.properties`).
- `runWindows.bat` / `coreWindows.bat`: Scripts de compilação e execução para Windows.
- `runLinux.sh` / `coreLinux.sh`: Scripts de compilação e execução para Linux.
- `pom.xml`: Arquivo de configuração do Maven.

---

## 🚀 Como Executar

> **Primeira execução:** Caso seja a primeira vez rodando o projeto na máquina, execute `mvn dependency:resolve` na pasta raiz para garantir que a biblioteca do Processing seja baixada para o repositório local do Maven (`.m2`).

### 🪟 No Windows

O projeto já possui scripts prontos para facilitar a execução no Windows.

1. Abra o **Prompt de Comando (CMD)** ou o **PowerShell** na pasta raiz do projeto.
2. Execute o script principal:

   - **No CMD:**

     ```cmd
     runWindows.bat
     ```

   - **No PowerShell:**

     ```powershell
     .\runWindows.bat
     ```

   *(Alternativamente, você pode dar um duplo clique no arquivo `runWindows.bat` pelo Explorador de Arquivos.)*

---

### 🐧 No Linux

Para executar o projeto no Linux utilizando os scripts fornecidos:

1. Abra o **Terminal** na pasta raiz do projeto.
2. Conceda permissão de execução aos arquivos `.sh`:

   ```bash
   chmod +x runLinux.sh coreLinux.sh
   ```

3. Execute o script principal:

   ```bash
   ./runLinux.sh
   ```

---

## 🛠️ Execução Manual (Alternativa)

Caso queira compilar e rodar o projeto manualmente, sem utilizar os scripts `.bat` ou `.sh`, você pode usar uma das opções abaixo.

### Utilizando o Maven (`pom.xml`) — Recomendado

```bash
mvn clean compile exec:java "-Dexec.mainClass=Main"
```

### Utilizando apenas `javac` e `java`

> Requer que o arquivo `processing-core-4-4.5.0.jar` já esteja presente no repositório local `.m2`.

**No Linux:**

```bash
mkdir -p bin
javac -cp "$HOME/.m2/repository/com/github/micycle1/processing-core-4/4.5.0/processing-core-4-4.5.0.jar" -d bin $(find src -name "*.java")
java -cp "bin:$HOME/.m2/repository/com/github/micycle1/processing-core-4/4.5.0/processing-core-4-4.5.0.jar" -ea Main
```

**No Windows (PowerShell):**

```powershell
mkdir -Force bin
javac -cp "$env:USERPROFILE\.m2\repository\com\github\micycle1\processing-core-4\4.5.0\processing-core-4-4.5.0.jar" -d bin (Get-ChildItem -Path src -Filter *.java -Recurse).FullName
java -cp "bin;$env:USERPROFILE\.m2\repository\com\github\micycle1\processing-core-4\4.5.0\processing-core-4-4.5.0.jar" -ea Main
```

---

## ⚙️ Configurações

Os parâmetros iniciais da simulação podem ser ajustados diretamente na interface gráfica do sistema (`TelaConfig`) ou editando o arquivo `data/config.properties`.