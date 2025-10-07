# ImageVault API 🛡️

> API RESTful segura para upload, gerenciamento e visualização de imagens na nuvem, construída com Spring Boot.

## ✨ Features

* **Autenticação e Segurança Completa:**
    * Sistema de Registro e Login com **JWT (Access + Refresh Tokens)**.
    * **Autenticação de Dois Fatores (2FA)** com TOTP (Google Authenticator, etc.).
    * Hashing de senhas com BCrypt.
    * Endpoints protegidos com base em autenticação.

* **Gerenciamento de Imagens:**
    * **Upload de imagens** para armazenamento de objetos compatível com S3 (MinIO).
    * **Geração assíncrona de Thumbnails** para otimização de performance.
    * **Visualização Segura** de imagens privadas através de **URLs Pré-Assinadas** com tempo de expiração.
    * Suporte para imagens públicas e privadas.
    * CRUD completo para metadados de imagens.

* **Arquitetura Robusta:**
    * Aplicação totalmente **containerizada** com Docker e Docker Compose.
    * Banco de dados relacional (PostgreSQL) com migrations gerenciadas por **Flyway**.
    * **Tratamento de Exceções Global** e padrão de resposta da API consistente.
    * Documentação da API interativa com **Swagger/OpenAPI**.

## 🛠️ Tecnologias Utilizadas

* **Back-end:** Java 17, Spring Boot, Spring Security, Spring Data JPA (Hibernate)
* **Banco de Dados:** PostgreSQL
* **Armazenamento de Objetos:** MinIO (S3-Compatible)
* **Autenticação:** JWT, TOTP
* **Build & Dependências:** Maven
* **Containerização:** Docker, Docker Compose
* **Outras Bibliotecas:** Lombok, Springdoc OpenAPI, JJWT, Imgscalr

## 🚀 Como Rodar o Projeto Localmente

**Pré-requisitos:**
* Java 17 (ou superior)
* Maven 3.x
* Docker e Docker Compose

**Passos:**

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/pedrohma07/ImageVault.git
    cd ImageVault
    ```

2.  **Crie o arquivo de ambiente `.env`:**
    Crie um arquivo chamado `.env` na raiz do projeto e preencha com suas credenciais. Você pode usar o exemplo abaixo como base:
    ```env
    # .env.example

    # Banco de Dados
    DB_USER=myuser
    DB_PASSWORD=secret

    # MinIO
    MINIO_ACCESS_KEY=minioadmin
    MINIO_SECRET_KEY=minioadmin

    # JWT (Use uma chave secreta forte gerada em Base64 para produção)
    JWT_SECRET_KEY=sua-secret-key

    # Chaves para criptografia do segredo 2FA (use valores longos e aleatórios)
    ENCRYPTION_PASSWORD=sua-senha-mestra-super-secreta-para-criptografia
    ENCRYPTION_SALT=5c0744944cfb4f34a1d8a342489a273b
    ```

3.  **Inicie a aplicação com Docker Compose:**
    Este comando irá construir a imagem da sua API e iniciar todos os serviços (API, Postgres, MinIO).
    ```bash
    docker-compose up --build
    ```

4.  **Acesse a aplicação:**
    * **API:** `http://localhost:8080`
    * **Documentação Swagger UI:** `http://localhost:8080/swagger-ui.html`
    * **Console do MinIO:** `http://localhost:9001` (login com as credenciais do `.env`)

## 📄 Endpoints da API

A documentação completa e interativa da API, com todos os endpoints e modelos de dados, está disponível via **Swagger UI** após iniciar a aplicação.

Acesse: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
