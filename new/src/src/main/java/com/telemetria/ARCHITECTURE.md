# Documento de Arquitetura — JHC Telemetria

**Projeto:** JHC Telemetria — Plataforma de Gestão de Frotas com Monitoramento em Tempo Real  
**Autor:** Vitor Augusto (Arquiteto de Software)  
**Versão:** 1.3 
**Data:** Junho/2026

---

## 1. Visão Geral da Arquitetura

O sistema JHC Telemetria adota uma **arquitetura em camadas** (Layered Architecture), com separação clara de responsabilidades entre interface gráfica, lógica de domínio e acesso a dados. Essa escolha facilita a manutenção, o teste isolado de cada camada e a evolução independente de partes do sistema.

```
┌──────────────────────────────────────────┐
│           VIEW (FXML / JavaFX)           │  ← Telas da interface gráfica
├──────────────────────────────────────────┤
│        CONTROLLER (JavaFX Controllers)   │  ← Recebe eventos da UI, chama Model/DAO
├──────────────────────────────────────────┤
│         MODEL (Entidades de Domínio)     │  ← Regras de negócio e lógica OOP
├──────────────────────────────────────────┤
│    REPOSITORY (DAOs) + DB (Conexão)      │  ← Acesso ao banco de dados SQLite
└──────────────────────────────────────────┘
```

A camada de **View** não foi incluída no repositório como arquivos `.java` separados — ela é definida por arquivos `.fxml` carregados dinamicamente pelos Controllers. Isso respeita o padrão **MVC** (Model-View-Controller) aplicado com JavaFX.

---

## 2. Organização de Pacotes

```
src/main/java/com/telemetria/
│
├── application/       # Ponto de entrada da aplicação (Main.java)
│
├── controller/        # Controllers JavaFX — ponte entre UI e lógica
│
├── model/             # Entidades, interfaces, enums e lógica de domínio
│
├── repository/        # DAOs de acesso ao banco de dados
│
└── db/                # Conexão com o banco e inicialização do schema SQLite
```

A separação entre `db/` e `repository/` é intencional: `db/` cuida exclusivamente da conexão e da criação das tabelas (`ConexaoBanco`, `InicializadorBanco`), enquanto `repository/` contém os DAOs que operam sobre os dados. Nenhuma classe de `model/` acessa o banco diretamente (a dívida técnica do `LocalizacaoDAO` embutido em `Localizacao.java` permanece pendente — ver seção 6).

---

## 3. Decisões Arquiteturais e Justificativas

Esta seção registra o **porquê** das principais escolhas técnicas do projeto.

---

### 3.1 `Usuario` como Classe Abstrata

**Decisão:** `Usuario` é uma classe abstrata com o método `acessarSistema()` declarado como abstrato.

**Justificativa:** O sistema possui subclasses concretas de usuário (`Gestor`, `Operador`, `Equipe`, `Cliente`, `Instalador`, `Motorista`), cada uma com um painel e comportamento distintos ao acessar o sistema. Tornar `Usuario` abstrata impede que uma instância genérica seja criada acidentalmente, e força cada subclasse a definir seu próprio comportamento de acesso. Isso aplica o princípio **Open/Closed**: o sistema está aberto para adicionar novos tipos de usuário sem modificar a classe base.

```java
// Usuario.java
public abstract void acessarSistema(); // cada subclasse define seu painel
```

---

### 3.2 `Autenticavel` como Interface com Métodos Default

**Decisão:** `Autenticavel` é uma interface com três sobrecargas de `validarFator()` como métodos `default` e um contrato `getQuantidadeFatores()`.

**Justificativa:** Nem todos os usuários autenticam com o mesmo número de fatores. A interface define o contrato e fornece implementações padrão, permitindo que cada subclasse sobrescreva apenas o método relevante ao seu nível de acesso. Isso evita duplicação de código e mantém a flexibilidade para adicionar novos métodos de autenticação no futuro.

```
Motorista/Cliente → validarFator(senha)               [1 fator]
Operador          → validarFator(senha, token)         [2 fatores]
Equipe/Admin      → validarFator(senha, token, bio)    [3 fatores]
```

---

### 3.3 `PerfilAcesso` como Enum com Comportamento (RBAC)

**Decisão:** As regras de autorização são centralizadas no enum `PerfilAcesso`, no método `temPermissao(String acao)`.

**Justificativa:** O controle de acesso baseado em papéis (RBAC) poderia ter sido implementado com verificações espalhadas por cada controller ou classe de usuário. Centralizar essa lógica no enum garante que a regra de "quem pode fazer o quê" esteja em um único lugar, reduzindo o risco de inconsistências.

Os quatro perfis ativos são:

| Enum | Nível | Descrição |
|------|-------|-----------|
| `MOTORISTA` | 0 | Motorista — acesso ao app de bordo |
| `FROTISTA` | 1 | Gestor de Frota — gerencia veículos e mensagens |
| `OPERADOR` | 2 | Operador de Telemetria — gerencia sensores e usuários |
| `ADMIN` | 3 | Administrador do Sistema — acesso total, incluindo logs |

```java
// PerfilAcesso.java
public boolean temPermissao(String acao) {
    switch (acao) {
        case "VER_LOGS":    return this == ADMIN;
        case "VER_FROTA":   return this.nivel >= 0;
        // ...
    }
}
```

O `Usuario` delega a verificação ao enum via `podeExecutar()`, mantendo a lógica de autorização fora das subclasses.

---

### 3.4 `Localizacao` como Java Record

**Decisão:** `Localizacao` foi implementada como um `record` Java (disponível a partir do Java 16).

**Justificativa:** Dados de GPS são inerentemente **imutáveis** — uma leitura de localização representa um momento específico no tempo e não deve ser alterada após a captura. O `record` garante essa imutabilidade por design, gera automaticamente `equals()`, `hashCode()` e `toString()`, e comunica a intenção semântica de que `Localizacao` é um **tipo de valor**, não uma entidade com identidade mutável.

```java
public record Localizacao(
    double latitude,
    double longitude,
    double velocidadeKmh,
    Instant timestamp
) {
    public String toGoogleMapsUrl() { ... }
}
```

---

### 3.5 SQLite como Banco de Dados Embarcado

**Decisão:** O banco de dados foi migrado de PostgreSQL para **SQLite**, com o arquivo `jhctelemetria.db` armazenado localmente no diretório do projeto.

**Justificativa:** O SQLite elimina a necessidade de um servidor de banco de dados externo, simplificando a distribuição e execução do sistema em ambiente desktop. A conexão é feita via JDBC com o driver SQLite sem nenhuma configuração de rede, o que também resolve a dívida técnica de credenciais expostas no código (item 4 da versão anterior). O schema completo é criado pelo `InicializadorBanco` na primeira execução.

```java
// ConexaoBanco.java
private static final String URL = "jdbc:sqlite:jhctelemetria.db";
```

As tabelas `usuario`, `veiculos`, `sensores`, `logs`, `localizacao` e `mensagens` (nova) são criadas via `InicializadorBanco.java`.

---

### 3.6 Pacote `db/` para Infraestrutura de Banco

**Decisão:** A lógica de conexão e inicialização do banco foi isolada em um pacote próprio (`db/`), separado dos DAOs em `repository/`.

**Justificativa:** Na versão anterior, `ConexaoBanco` estava dentro de `repository/`. Com a migração para SQLite e a adição do `InicializadorBanco` (responsável por criar as tabelas na primeira execução), fez sentido criar um pacote dedicado à infraestrutura de banco, deixando `repository/` exclusivo para operações de dados.

```
db/
├── ConexaoBanco.java       # Fábrica de conexões JDBC (SQLite)
└── InicializadorBanco.java # Cria todas as tabelas na primeira execução
```

---

### 3.7 Padrão DAO (Data Access Object) na Camada Repository

**Decisão:** Todo acesso ao banco de dados é isolado em classes DAO dentro do pacote `repository/`.

**Justificativa:** Sem essa separação, chamadas JDBC estariam espalhadas pelos controllers ou pelas entidades de domínio, tornando o código difícil de testar e manter. Com o padrão DAO, a camada `model/` não conhece a existência do banco de dados. Os DAOs ativos são:

| Classe DAO | Responsabilidade |
|------------|-----------------|
| `UsuarioDAO` | CRUD de usuários, autenticação, log embutido |
| `VeiculoDAO` | Cadastro e consulta de veículos |
| `SensorDAO` | Cadastro e atualização de sensores por veículo |
| `LogDAO` | Gravação e leitura de logs de auditoria |
| `GeralDAO` | Consultas compostas (frota completa, histórico de veículo, localização) |

---

### 3.8 Roteamento de Telas por Cargo no `MenuController`

**Decisão:** O `MenuController` usa um `switch` sobre o cargo do usuário logado (obtido via `LoginController.cargoLogado`) para determinar qual arquivo FXML carregar.

**Justificativa:** Centralizar o roteamento em um único ponto evita que cada tela precise conhecer as outras. O `MenuController` funciona como um **Front Controller** simplificado, validando também se o arquivo FXML existe antes de tentar carregá-lo, o que previne erros silenciosos.

```
Login → MenuController → TelaX.fxml (Administrador)
                       → TelaY.fxml (Cliente)
                       → TelaZ.fxml (Operador)
```

---

## 4. Diagrama de Dependências entre Pacotes

```
application/
    └── depende de → model/

controller/
    └── depende de → model/
    └── depende de → repository/
    └── depende de → db/ (indiretamente via DAOs)

model/
    └── não depende de nenhum outro pacote do projeto (isolado)*

repository/
    └── depende de → model/ (usa as entidades para montar objetos)
    └── depende de → db/    (usa ConexaoBanco para obter conexões)

db/
    └── não depende de nenhum outro pacote do projeto
```

\* A exceção é `Localizacao.java`, que ainda contém `LocalizacaoDAO` embutido com dependência em `db/` — dívida técnica registrada na seção 6.

---

## 5. Modelo Relacional e Correspondência com o Domínio

| Tabela SQL | Classe Java | Observação |
|---|---|---|
| `usuario` | `Usuario` (abstract) + subclasses | `nivel_acesso` mapeia para `PerfilAcesso` via `porNivel()` |
| `veiculos` | `Veiculo` | Relacionamento 1:N com `usuario`; campo `motorista_id` para vínculo com Motorista |
| `sensores` | `Sensor` | Relacionamento 1:N com `veiculos` |
| `localizacao` | `Localizacao` (record) | Persistida via `GeralDAO.salvarLocalizacao()` ou `LocalizacaoDAO` (embutido) |
| `logs` | — | Sem entidade Java correspondente; acessado via `LogDAO` e `UsuarioDAO.salvarLog()` |
| `mensagens` | — | Nova tabela; caixa de mensagens entre usuários e a Central; sem entidade Java ainda |

---

## 6. Dívidas Técnicas e Pontos de Melhoria

| # | Problema | Localização | Impacto | Sugestão |
|---|----------|-------------|---------|----------|
| 1 | `LocalizacaoDAO` está definida dentro de `Localizacao.java` | `model/Localizacao.java` | Viola a separação entre model e db/repository | Mover para `repository/LocalizacaoDAO.java` |
| 2 | Conflito de merge não resolvido em `GeralDAO.java` | `repository/GeralDAO.java` | Marcadores `<<<<<<< HEAD` e `>>>>>>>` presentes no código | Resolver o merge e decidir qual versão de `salvarLocalizacao` manter |
| 3 | Duplicação de `salvarLocalizacao` | `GeralDAO.java` e `Localizacao.java` (LocalizacaoDAO) | Dois caminhos para persistir GPS; risco de inconsistência | Consolidar em `repository/LocalizacaoDAO.java` |
| 4 | Operador `&` usado em vez de `&&` em `Autenticavel` | `model/Autenticavel.java` | `validarFator()` usa operador bitwise em vez de lógico; não faz validação real | Substituir `&` por `&&` e implementar validação real |
| 5 | Senhas armazenadas em texto puro | `repository/UsuarioDAO.java` | Risco de segurança mesmo em protótipo | Aplicar hashing com bcrypt ou similar |
| 6 | Token fixo `"000000"` na autenticação multifator | Subclasses de `Usuario` | Autenticação multifator sem efeito real | Implementar geração de token dinâmico |
| 7 | Tabela `mensagens` sem entidade Java correspondente | `db/InicializadorBanco.java` | Tabela criada no schema mas não mapeada no modelo | Criar classe `Mensagem` e `MensagemDAO` |
| 8 | `salvarLog` duplicado em `UsuarioDAO` e `LogDAO` | `repository/` | Lógica de log em dois lugares; risco de divergência | Centralizar toda gravação de log no `LogDAO` |

---

## 7. Histórico de Evolução Arquitetural

| Fase | Decisão |
|---|---|
| Início (maio/2026) | Modelagem inicial com hierarquia `Usuario`, interface `Autenticavel`, `Localizacao` como record |
| Meio (maio/2026) | Adição de `PerfilAcesso` como enum com comportamento; criação de `Equipe`, `Instalador` e `Motorista` |
| Versão anterior (junho/2026) | Migração de `dao/` para `repository/`; controllers JavaFX; integração com PostgreSQL |
| Atual (junho/2026) | Migração de PostgreSQL para **SQLite** (`jhctelemetria.db`); criação do pacote `db/` (`ConexaoBanco`, `InicializadorBanco`); adição da tabela `mensagens`; atualização dos perfis (`MOTORISTA`, `FROTISTA`, `OPERADOR`, `ADMIN`) |

---

*Documento mantido pelo Arquiteto de Software. Deve ser atualizado a cada mudança estrutural relevante no projeto.*
