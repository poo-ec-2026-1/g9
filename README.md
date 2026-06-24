<div align="center">

# 🚗 JHC Telemetria

### Plataforma de Gestão de Frotas com Monitoramento em Tempo Real

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue?style=for-the-badge)](https://openjfx.io/)
[![SQLite](https://img.shields.io/badge/SQLite-Database-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://www.sqlite.org/)


*Projeto acadêmico — Disciplina de Programação Orientada a Objetos · UFG · 2026*

</div>

---

## 📖 Sobre o Projeto

O **JHC Telemetria** é uma plataforma de IoT veicular desenvolvida para monitoramento de frotas em tempo real. O sistema integra sensores embarcados, rastreamento geográfico via GPS e uma central de alertas, cobrindo desde o diagnóstico preventivo de veículos até o controle de conformidade operacional.

A motivação surgiu de um evento real de março de 2026, em que uma paciente de 91 anos faleceu após uma ambulância do SAMU ter seu percurso obstruído. O sistema busca endereçar falhas de comunicação e rastreamento em situações críticas de mobilidade urbana.

### ✨ Funcionalidades

| Módulo | Descrição |
|---|---|
| 🔐 **Autenticação** | Login com controle de acesso por perfil (RBAC) e autenticação multifator |
| 📍 **Rastreamento GPS** | Leitura de latitude, longitude e velocidade em tempo real com link para Google Maps |
| 📡 **Sensores** | Cadastro, vinculação a veículos e disparo de alertas ao exceder limites configurados |
| 🚨 **Central de Alertas** | Recepção de emergências, localização do veículo e listagem de ocorrências ativas |
| 🖥️ **Interface Gráfica** | Painéis JavaFX distintos por cargo (Admin, Operador/Instalador, Cliente) |
| 👥 **Gestão de Usuários** | CRUD completo de usuários com controle de permissões por nível |
| 🚗 **Gestão de Frota** | Cadastro de veículos com identificador (placa/chassi), tipo e associação a sensores |
| 📋 **Logs do Sistema** | Registro de ações com timestamp, limpeza e auditoria reservada ao Administrador |
| 🗄️ **Persistência** | Banco de dados PostgreSQL via JDBC com transações e batch operations |

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas com separação clara de responsabilidades:

```
src/main/java/com/telemetria/
│
├── application/          # Ponto de entrada da aplicação
│   └── Main.java
│
├── controller/           # Controllers JavaFX — ligam a UI à lógica
│   ├── LoginController.java
│   ├── MenuController.java
│   ├── TelaXController.java       ← Painel do Administrador
│   ├── TelaYController.java       ← Painel do Cliente
│   ├── TelaZController.java       ← Painel do Instalador/Operador
│   ├── CadastroVeiculoController.java
│   ├── NovoUsuarioFormController.java
│   ├── RegistroLoginController.java
│   ├── NomeAlterarController.java
│   └── SenhaAlterarController.java
│
├── model/                # Entidades e lógica de domínio (OOP puro)
│   ├── Usuario.java               ← Classe abstrata base
│   ├── Autenticavel.java          ← Interface de autenticação multifator
│   ├── PerfilAcesso.java          ← Enum com níveis e permissões (RBAC)
│   ├── Gestor.java                ← extends Usuario, implements Autenticavel
│   ├── Operador.java              ← extends Usuario, implements Autenticavel
│   ├── Equipe.java                ← extends Usuario, implements Autenticavel (Admin)
│   ├── Cliente.java               ← extends Usuario, implements Autenticavel
│   ├── Instalador.java            ← extends Usuario, implements Autenticavel
│   ├── Veiculo.java
│   ├── Sensor.java
│   ├── Localizacao.java           ← Record imutável (lat, long, velocidade, timestamp)
│   ├── Central.java
│   ├── Monitoramento.java
│   └── Login.java
│
└── repository/           # DAOs — acesso ao banco de dados
    ├── ConexaoBanco.java
    ├── UsuarioDAO.java
    ├── VeiculoDAO.java
    ├── SensorDAO.java
    ├── GeralDAO.java
    └── LogDAO.java
```

---

## 🧩 Modelagem OOP

### Hierarquia de Classes

```
            ┌──────────────────────┐
            │    <<interface>>     │
            │     Autenticavel     │
            │  + getQuantFatores() │
            │  + validarFator(...) │
            └──────────┬───────────┘
                       │ implements
          ┌────────────┼───────────────┐──────────────┐
          │            │               │              │
    ┌─────┴────┐ ┌─────┴────┐ ┌───────┴──┐ ┌────────┴───┐ ┌───────────┐
    │  Gestor  │ │ Operador │ │  Equipe  │ │  Cliente   │ │ Instalador│
    └──────────┘ └──────────┘ └──────────┘ └────────────┘ └───────────┘
          │            │               │              │             │
          └────────────┴───────────────┴──────────────┴─────────────┘
                                       │ extends
                               ┌───────┴────────┐
                               │    Usuario      │  <<abstract>>
                               │  # login        │
                               │  # senha        │
                               │  # nome         │
                               │  # perfil       │
                               │  + autenticar() │
                               │  + podeExecutar()│
                               │  + acessarSistema() <<abstract>>
                               └─────────────────┘


  ┌─────────────┐  composição   ┌──────────────────┐
  │   Veiculo   ├───────────────► List<Sensor>      │
  │             │               └──────────────────┘
  │             │  composição   ┌──────────────────┐
  │             ├───────────────►   Localizacao     │  <<record>>
  └──────┬──────┘               │  lat, long, kmh  │
         │ referência           │  timestamp       │
         ▼                      └──────────────────┘
  ┌─────────────┐
  │ Monitoramento│ ──────────────► Central
  │ + temErro() │  notifica       │ + receberAlerta()
  └─────────────┘                 └──────────────────
```

### Padrões OOP Aplicados

| Conceito | Implementação |
|---|---|
| **Herança** | `Gestor`, `Operador`, `Equipe`, `Cliente`, `Instalador` estendem `Usuario` |
| **Interface** | `Autenticavel` define contrato para autenticação com 1, 2 ou 3 fatores |
| **Polimorfismo** | `acessarSistema()` com comportamento distinto em cada subclasse |
| **Classe Abstrata** | `Usuario` define o template comum; impede instanciação direta |
| **Encapsulamento** | Atributos `private`/`protected` com getters e setters controlados |
| **Record (Java 16+)** | `Localizacao` como tipo de valor imutável com método auxiliar `toGoogleMapsUrl()` |
| **Enum com comportamento** | `PerfilAcesso` encapsula nível numérico, descrição e lógica de permissões via `temPermissao()` |
| **Composição** | `Veiculo` delega GPS a `Localizacao` e mantém `List<Sensor>` |
| **DAO Pattern** | Camada `repository` isola todo o acesso ao PostgreSQL da lógica de domínio |
| **Método Template** | `podeExecutar()` em `Usuario` delega para `PerfilAcesso.temPermissao()` |
| **RBAC** | Enum `PerfilAcesso` centraliza regras de autorização por ação e nível |

---

## 👥 Controle de Acesso (RBAC)

```
Nível 0 — CLIENTE
  ✔ Visualizar seus próprios sensores vinculados
  ✔ Autenticação por 1 fator (senha)

Nível 1 — FROTISTA (Gestor)
  ✔ Tudo do nível anterior
  ✔ Visualizar frota completa com filtros
  ✔ Cadastrar e manter veículos/sensores

Nível 2 — OPERADOR
  ✔ Tudo do nível anterior
  ✔ Gerenciar usuários (criar, editar, excluir)
  ✔ Instalar e vincular sensores a usuários
  ✔ Verificar telemetria ativa da frota
  ✔ Autenticação por 2 fatores (senha + token)

Nível 3 — ADMIN (Equipe)
  ✔ Acesso total ao sistema
  ✔ Visualizar e limpar logs do sistema
  ✔ Autenticação por 3 fatores (senha + token + biometria)
```

---

## 🛠️ Tecnologias

- **Java 17+** com features modernas (`record`, `switch` expressions, `instanceof` pattern)
- **JavaFX** — interface gráfica com FXML, `@FXML`, controllers e navegação entre cenas
- **Spring Boot** — estrutura base da aplicação
- **PostgreSQL** — banco de dados relacional com JDBC puro
- **Git / GitHub** — controle de versão com histórico desde maio de 2026

---

## ⚙️ Pré-requisitos

- Java 17 ou superior (`java --version`)
- PostgreSQL 13 ou superior
- JavaFX SDK (caso não fornecido via Maven/Gradle)
- Maven ou Gradle

---

## 🚀 Instalação e Execução

### 1. Clone o repositório

```bash
git clone https://github.com/VictorEduardo-coder/Gestao-de-Frotas.git
cd Gestao-de-Frotas
```

### 2. Configure o banco de dados PostgreSQL

```sql
-- Execute no psql ou em seu cliente SQL preferido
CREATE DATABASE jhctelemetria;

\c jhctelemetria

CREATE TABLE usuario (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(100)        NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    login         VARCHAR(50) UNIQUE  NOT NULL,
    senha         VARCHAR(255)        NOT NULL,
    nivel_acesso  INTEGER             NOT NULL DEFAULT 0
);

CREATE TABLE veiculos (
    id                SERIAL PRIMARY KEY,
    usuario_id        INTEGER REFERENCES usuario(id),
    identificador     VARCHAR(50)  NOT NULL,
    tipo_identificador VARCHAR(30),
    tipo_veiculo      VARCHAR(50),
    ativo             BOOLEAN      DEFAULT true
);

CREATE TABLE sensores (
    id             SERIAL PRIMARY KEY,
    veiculo_id     INTEGER REFERENCES veiculos(id),
    categoria      VARCHAR(50),
    nome           VARCHAR(100) NOT NULL,
    und_medida     VARCHAR(20),
    tipo_dado      VARCHAR(20),
    valor_atual    DOUBLE PRECISION DEFAULT 0,
    limite_maximo  DOUBLE PRECISION DEFAULT 0,
    atualizado_em  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE localizacao (
    id              SERIAL PRIMARY KEY,
    dispositivo_id  BIGINT      NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    velocidade      DOUBLE PRECISION,
    data_hora       TIMESTAMP   NOT NULL
);

CREATE TABLE logs (
    id             SERIAL PRIMARY KEY,
    usuario_email  VARCHAR(100),
    acao           TEXT,
    data_hora      TIMESTAMP DEFAULT NOW()
);
```

### 3. Configure a conexão com o banco

Edite o arquivo `src/src/main/java/com/telemetria/repository/ConexaoBanco.java`:

```java
private static final String URL     = "jdbc:postgresql://localhost:5432/jhctelemetria";
private static final String USUARIO = "seu_usuario_postgres";
private static final String SENHA   = "sua_senha_postgres";
```

> ⚠️ **Segurança:** nunca suba credenciais reais para o repositório. Prefira variáveis de ambiente:
> ```bash
> export DB_URL=jdbc:postgresql://localhost:5432/jhctelemetria
> export DB_USER=postgres
> export DB_PASS=sua_senha
> ```

### 4. Execute a aplicação

```bash
# Via Maven
mvn spring-boot:run

# Ou compile e execute diretamente
javac -cp . GestaofrotasApplication.java
java com.telemetria.model.GestaofrotasApplication
```

---

## 🖥️ Como Usar

Ao iniciar, a tela de **Login** é exibida. O sistema redireciona automaticamente para o painel correto conforme o cargo do usuário autenticado.

### Usuários de desenvolvimento (pré-cadastrados)

| Login | Senha | Cargo | Painel |
|---|---|---|---|
| `adm` | `123` | Administrador | TelaX — acesso total |
| `adm1` | `1234` | Operador | TelaZ — telemetria e instalação |

> Novos usuários podem ser criados pela tela de registro ou pelo painel do Administrador.

### Fluxo de navegação

```
Tela de Login
    │
    ├── Administrador ──► TelaX  [vincular/ver sensores, editar/excluir usuários, logs]
    │
    ├── Operador/Instal. ─► TelaZ  [instalar sensores, verificar telemetria]
    │
    └── Cliente ──────────► TelaY  [ver meus sensores vinculados]
```

---

## 🗄️ Modelo de Dados

```
usuario ──────────────────────────────────────┐
│ id (PK)                                     │
│ nome, email, login, senha, nivel_acesso     │
└─────────────────────────────────────────────┘
         │ 1
         │ N
      veiculos ───────────────────────────────┐
      │ id (PK)                               │
      │ usuario_id (FK → usuario)             │
      │ identificador, tipo_identificador     │
      │ tipo_veiculo, ativo                   │
      └───────────────────────────────────────┘
               │ 1
               │ N
            sensores
            │ id (PK)
            │ veiculo_id (FK → veiculos)
            │ categoria, nome, und_medida
            │ tipo_dado, valor_atual, limite_maximo

      localizacao                    logs
      │ dispositivo_id               │ usuario_email
      │ latitude, longitude          │ acao
      │ velocidade, data_hora        │ data_hora
```

---

## 👨‍💻 Equipe

| Nome | Matrícula | Função |
|---|---|---|
| **Humberto Nogueira** | 202506862 | Líder de Projeto & Líder Técnico |
| **Victor Eduardo** | 202506944 | Desenvolvedor Backend |
| **João Pedro** | 202403019 | Desenvolvedor Frontend |
| **Raphael Henrique** | 202506943 | Engenheiro de QA / Testes |
| **Vitor Augusto** | 202503278 | Arquiteto de Software & Documentação |

---

## 📅 Histórico de Versões

| Data | Descrição |
|---|---|
| Maio/2026 | Modelagem inicial — interfaces, hierarquia de usuários, `Localizacao` como record |
| Maio/2026 | Implementação dos perfis de acesso (`PerfilAcesso` enum), `Equipe`, `Instalador` |
| Junho/2026 | Controllers JavaFX, integração com PostgreSQL, logs, gestão de frota e sensores |

---

## ⚠️ Observações Importantes

- A pasta `old/` contém versões anteriores das classes, mantidas apenas como histórico de evolução do projeto.
- O sistema possui duas camadas DAO (`dao/` e `repository/`) — a camada `repository/` é a versão atual e mais completa, enquanto `dao/` é uma versão mais antiga em processo de substituição.
- Senhas são armazenadas em texto puro no protótipo atual. Em ambiente de produção, aplique hashing com **bcrypt** ou similar.
- Os tokens de autenticação multifator estão fixos (`"000000"`) para fins de demonstração acadêmica.

---

<div align="center">
