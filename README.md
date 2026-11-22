# Simulação de Co-Evolução e Busca em Grafos

Projeto desenvolvido em **Java** que simula a co-evolução entre entregadores (agentes que utilizam algoritmos de busca como A* e Dijkstra) e obstáculos dinâmicos em um labirinto modelado como grafo, contando com interface gráfica e visualização em tempo real.

---

## 📋 Pré-requisitos

Certifique-se de ter instalado em sua máquina:
* **Java Development Kit (JDK)** (versão 11 ou superior recomendada)
* **Apache Maven** *(opcional, caso prefira compilar e gerenciar o projeto via `pom.xml`)*

---

## 📁 Estrutura do Projeto

* `src/`: Código-fonte em Java (`Main.java` é a classe principal).
* `data/`: Arquivos de configuração do sistema (`config.properties`).
* `runWindows.bat` / `coreWindows.bat`: Scripts de compilação e execução para Windows.
* `runLinux.sh` / `coreLinux.sh`: Scripts de compilação e execução para Linux.
* `pom.xml`: Arquivo de configuração do Maven.

---

## 🚀 Como Executar

### 🪟 No Windows

O projeto já possui scripts prontos para facilitar a execução no Windows.

1. Abra o **Prompt de Comando (CMD)** ou o **PowerShell** na pasta raiz do projeto.
2. Execute o script principal:
   ```cmd
   runWindows.bat
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

Caso queira compilar e rodar o projeto manualmente sem utilizar os scripts `.bat` ou `.sh` (via linha de comando ou Maven):

### Utilizando o Maven (`pom.xml`)
```bash
mvn clean compile exec:java -Dexec.mainClass="Main"
```

### Utilizando apenas o `javac` e `java`
* **No Linux:**
  ```bash
  mkdir -p bin
  javac -d bin $(find src -name "*.java")
  java -cp bin Main
  ```
* **No Windows (PowerShell):**
  ```powershell
  mkdir -Force bin
  javac -d bin (Get-ChildItem -Path src -Filter *.java -Recurse).FullName
  java -cp bin Main
  ```

---

## ⚙️ Configurações

Os parâmetros iniciais da simulação podem ser ajustados diretamente na interface gráfica do sistema (`TelaConfig`) ou editando o arquivo `data/config.properties`.
