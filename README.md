# LogisticaAdaptativa

🚀 Como Rodar a Aplicação via Docker

1. Prepare os arquivos XML:
Crie uma pasta chamada dados_xml na raiz do projeto no seu computador e coloque os arquivos XML que a aplicação precisa ler/gravar dentro dela.
2. Se você usa Linux (X11 ou Wayland):
Antes de iniciar o Docker, você precisa exportar as permissões do seu usuário para o container. Rode no terminal:bash
export CURRENT_UID=$(id -u)
export CURRENT_GID=$(id -g)
xhost +local:docker 2>/dev/null || true
Use o código com cuidado.
Em seguida, suba a aplicação com:bash
docker compose up --build
Use o código com cuidado.
3. Se você usa Windows ou macOS:
	• Certifique-se de ter um servidor X instalado e rodando em segundo plano (como o VcXsrv no Windows ou XQuartz no Mac).
	• Nas configurações do servidor X, marque a opção "Allow connections from clients" (Permitir conexões de clientes externos).
	• Rode no terminal antes do Docker (ajustando para o IP da sua máquina local):bash
# No Windows (PowerShell)
$env:DISPLAY="seu_ip_local:0.0"
# No macOS
export DISPLAY=localhost:0
Use o código com cuidado.
	• Execute: docker compose up --build
