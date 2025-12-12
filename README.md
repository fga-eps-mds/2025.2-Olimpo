<div align="center">
  <img src="Front/public/logo.png" alt="Lume Logo" width="150">
  <h1>Lume</h1>
  <p>
    Conectando estudantes empreendedores a investidores visionários.
  </p>

  <p>
    <img src="https://img.shields.io/github/stars/fga-eps-mds/2025.2-Olimpo?style=for-the-badge&color=yellow" alt="Stars"/>
    <img src="https://img.shields.io/github/commit-activity/m/fga-eps-mds/2025.2-Olimpo?style=for-the-badge&color=blue" alt="Commits"/>
    <img src="https://img.shields.io/github/last-commit/fga-eps-mds/2025.2-Olimpo?style=for-the-badge&color=orange" alt="Last Commit"/>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
    <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
    <img src="https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB" />
    <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
  </p>
</div>

<br />

## 📖 Sobre

**Lume** é um sistema web desenvolvido para a disciplina de **Métodos de Desenvolvimento de Software (2025.2)** da Universidade de Brasília (UnB). O objetivo da plataforma é conectar estudantes universitários que desejam empreender com investidores que buscam apoiar ideias inovadoras, criando um ecossistema de colaboração e crescimento.

## 🚀 Tecnologias

-   **Frontend:** React, Vite, CSS Modules
-   **Backend:** Java 17+, Spring Boot 3+
-   **Banco de Dados:** PostgreSQL
-   **Infraestrutura:** Docker, Docker Compose

## ⚙️ Configuração

### Variáveis de Ambiente (.env)

Para executar o projeto corretamente, é necessário configurar as variáveis de ambiente. Crie um arquivo `.env` na pasta `Back` com o seguinte conteúdo:

```env
# Banco de Dados
POSTGRES_USER=postgres
POSTGRES_PASSWORD=sua_senha
POSTGRES_DB=olimpo_db

# Cloudinary (Upload de Imagens)
CLOUDINARY_CLOUD_NAME=seu_cloud_name
CLOUDINARY_API_KEY=sua_api_key
CLOUDINARY_API_SECRET=seu_api_secret

# Email (Envio de notificações)
MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=sua_senha_de_app

# Frontend URL (CORS)
APP_FRONTEND_URL=http://localhost:5173
```

### Portas

| Serviço | Porta Interna | Porta Exposta |
| :--- | :---: | :---: |
| **Frontend** | 5173 | 5173 |
| **Backend** | 8080 | 8080 |
| **Banco de Dados** | 5432 | 5432 |

## 🛠️ Como Executar

### Usando Docker (Recomendado)

Certifique-se de ter o [Docker](https://www.docker.com/) e o [Docker Compose](https://docs.docker.com/compose/) instalados.

1.  Navegue até a pasta `Back`:
    ```bash
    cd Back
    ```
2.  Suba os containers (Banco de Dados e Backend):
    ```bash
    docker-compose up --build
    ```
3.  Em outro terminal, navegue até a pasta `Front` e inicie o frontend:
    ```bash
    cd Front
    npm install
    npm run dev
    ```

### Execução Manual

#### Backend
1.  Certifique-se de ter um banco PostgreSQL rodando e configurado conforme o `.env`.
2.  Navegue até a pasta do backend:
    ```bash
    cd Back/olimpo
    ```
3.  Execute a aplicação:
    ```bash
    ./mvnw spring-boot:run
    ```

#### Frontend
1.  Navegue até a pasta do frontend:
    ```bash
    cd Front
    ```
2.  Instale as dependências:
    ```bash
    npm install
    ```
3.  Inicie o servidor de desenvolvimento:
    ```bash
    npm run dev
    ```

## 👥 Equipe Olimpo

| Nome | Github | Discord | Email | Matrícula |
|:----:|:------:|:-------:|:-----:|:---------:|
| Arthur Martins | [@thetrulyvoid](https://github.com/thetrulyvoid) | thevoidcruz | 241038174@aluno.unb.br | 241038174 |
| Eduardo Lobo | [@EduLoboM](https://github.com/EduLoboM) | eduzao__ | 241011027@aluno.unb.br | 241011027 |
| Enzo Costa | [@enzocostaj](https://github.com/enzocostaj) | tyfoon77 | 232001999@aluno.unb.br | 232001999 |
| Gustavo Lima | [@gustavolima973](https://github.com/gustavolima973) | thegusttt | 211062938@aluno.unb.br | 211062938 |
| Nicole Jovita  | [@nicolejovita](https://github.com/nicolejovita) | nicolejovita_32722 | nicolejovitafernandes@gmail.com | 241012347 |
| Pedro Augusto Ribeiro | [@pedrorfb](https://github.com/pedrorfb) | .pedro2 | pedrufb@gmail.com | 241040323 |
| Thaíza Weert | [@ThaizaWeert](https://github.com/ThaizaWeert) | thaizaweert | 211062508@aluno.unb.br | 211062508 |
| Vinícius Araújo   | [@Vini-Araujoo](https://github.com/Vini-Araujoos) | vini_cz | viniciustrabalhosunb@gmail.com | 241025425 |
| Vitor Lacerda | [@vitorldslds](https://github.com/vitorldslds) | vitorldslds | 232014217@aluno.unb.br  | 232014217 |
| Yasmim de Souza | [@eii-yahs](https://github.com/eii-yahs) | eii.yahs | yasmimdesouzasantos200612@gmail.com | 241040860 |

## 📄 Licença

Este projeto está sob a licença [MIT](LICENSE).