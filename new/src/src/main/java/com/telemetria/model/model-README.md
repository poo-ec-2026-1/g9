# Pacote `model/` — Camada de Domínio

Este pacote contém todas as entidades, interfaces, enums e classes de lógica de domínio do sistema **JHC Telemetria**. É o núcleo do projeto: não depende de banco de dados, nem de interface gráfica — representa o "o quê" do sistema, independente de como ele é exibido ou persistido.

> **Regra de ouro deste pacote:** nenhuma classe aqui deveria importar `java.sql.*` ou acessar `ConexaoBanco` diretamente. Acesso a banco é responsabilidade do pacote `repository/`. ⚠️ Exceções a essa regra existem na versão atual e estão documentadas na seção de [Dívidas Técnicas](#-dívidas-técnicas).

---

## Índice

- [Visão Geral](#visão-geral)
- [Hierarquia de Usuários](#hierarquia-de-usuários)
- [Telemetria e Sensores](#telemetria-e-sensores)
- [Monitoramento e Alertas](#monitoramento-e-alertas)
- [Autenticação e Autorização](#autenticação-e-autorização)
- [Classes Auxiliares](#classes-auxiliares)
- [Mapa de Dependências](#mapa-de-dependências)
- [Dívidas Técnicas](#-dívidas-técnicas)

---

## Visão Geral

O pacote possui **21 arquivos**, organizados em cinco grupos funcionais:

| Grupo | Classes | Responsabilidade |
|---|---|---|
| **Hierarquia de Usuários** | `Usuario`, `Gestor`, `Operador`, `Equipe`, `Cliente`, `Motorista`, `Instalador` | Modelar os atores do sistema com herança e polimorfismo |
| **Autenticação e Autorização** | `Autenticavel`, `PerfilAcesso`, `Login` | Controlar acesso por nível e por fator de autenticação |
| **Telemetria e Sensores** | `Sensor`, `SensorGeografico`, `Veiculo`, `Localizacao`, `SimuladorSensor`, `ProcessadorTelemetria` | Representar dispositivos físicos e seus dados em tempo real |
| **Monitoramento e Alertas** | `Monitoramento`, `GatilhoSensor`, `Central`, `RegistroAlerta` | Avaliar leituras e disparar alertas quando limites são violados |
| **Auxiliares** | `TelaDeInicio` | Bootstrapping da aplicação no modo console |

---

## Hierarquia de Usuários

### `Usuario` *(classe abstrata)*

Classe base de todos os atores do sistema. Define os atributos comuns (`login`, `senha`, `nome`, `email`, `perfil`) e o contrato que toda subclasse deve cumprir.

**Método abstrato:**
```java
public abstract void acessarSistema();
```
Cada subclasse implementa seu próprio painel de navegação. Isso garante que `Usuario` nunca seja instanciado diretamente — o sistema sempre trabalha com um tipo concreto de usuário.

**Método de delegação:**
```java
public boolean podeExecutar(String tipoAcao) {
    return perfil.temPermissao(tipoAcao);
}
```
`Usuario` delega a lógica de autorização para `PerfilAcesso`, mantendo-se livre de regras de negócio de acesso.

⚠️ **Bug conhecido:** `setSenha(String Senha)` não atualiza o atributo — o parâmetro usa `S` maiúsculo mas o corpo do método atribui `this.senha = senha` (referenciando o atributo, não o parâmetro). O setter nunca funciona.

---

### `Gestor` *(extends Usuario, implements Autenticavel)*

Usuário de nível **FROTISTA (1)**. Gerencia a frota e os usuários da empresa com autenticação de **2 fatores** (senha + token).

Implementa `acessarSistema()` com um menu interativo que exibe opções dinamicamente com base nas permissões retornadas por `podeExecutar()`. Isso significa que o mesmo menu se adapta ao nível do usuário sem precisar de `if (perfil == X)` explícito em cada item.

---

### `Operador` *(extends Usuario, implements Autenticavel)*

Usuário de nível **OPERADOR (2)**. Responsável pela coleta de telemetria em campo e gestão de usuários e sensores. Autenticação de **2 fatores**.

Mantém uma `List<Veiculo> frotaLocal` em memória durante a sessão de coleta, que é descartada ao encerrar — os dados persistidos ficam no banco via `SensorDAO` e `VeiculoDAO`.

---

### `Equipe` *(extends Usuario, implements Autenticavel)*

Usuário de nível **ADMIN (3)**. Acesso total ao sistema, incluindo logs e exclusão de dados críticos. Autenticação de **3 fatores** (senha + token + biometria).

É a única classe que pode invocar `LogDAO.lerLogsBanco()` e a única com acesso à limpeza de logs, controlada via `PerfilAcesso.temPermissao("LIMPAR_LOGS")`.

⚠️ `Equipe` possui acesso direto ao banco (`ConexaoBanco`, `PreparedStatement`) no método `detalharUsuario()`. Essa lógica deveria estar em um DAO.

---

### `Cliente` *(extends Usuario, implements Autenticavel)*

Usuário de nível **FROTISTA (1)** na nomenclatura do enum, mas representa o cliente final da frota. Autenticação de **1 fator** (senha).

⚠️ `Cliente` também acessa o banco diretamente para visualizar seus sensores, violando a separação de camadas.

---

### `Motorista` *(extends Usuario, implements Autenticavel)*

Usuário de nível **MOTORISTA (0)**. Responsável por iniciar viagens, calcular rotas e transmitir dados de GPS. Autenticação de **1 fator**.

É a única classe que instancia `LocalizacaoDAO` diretamente (classe aninhada em `Localizacao.java`) e gerencia `Thread`s para os `SimuladorSensor` durante uma viagem ativa.

---

### `Instalador` *(extends Usuario, implements Autenticavel)*

Usuário técnico com autenticação de **1 fator**. Implementação mínima — `acessarSistema()` está pendente de desenvolvimento.

---

## Telemetria e Sensores

### `Sensor`

Representa um sensor físico instalado em um veículo. Suporta dois tipos de dado:

- **`"1/0"`** — sensor binário (porta aberta/fechada, luz ligada/desligada). Aceita apenas `0` ou `1`.
- **Decimal** — sensor numérico (temperatura, velocidade, pressão). Dispara aviso no console se `valorAtual > limiteMaximo`.

O método `simularLeituraAleatoria()` gera valores aleatórios para testes: para sensores binários, sorteia `0` ou `1`; para decimais, sorteia até `limiteMaximo * 1.2` para cobrir cenários acima do limite.

⚠️ `idSensor` é um contador estático (`static int`), o que significa que IDs são sequenciais por sessão de JVM, não por banco de dados. Em ambiente multi-thread pode gerar IDs duplicados.

---

### `SensorGeografico` *(extends Sensor)*

Especialização de `Sensor` para dados de GPS. Mantém `latitude` e `longitude` como atributos próprios e implementa `simularDeslocamentoAleatorio()`, que calcula um deslocamento aleatório de até 200 metros em direção e distância aleatórias usando trigonometria básica.

Coordenadas padrão iniciais: **Goiânia, GO** (-16.677, -49.242).

---

### `Veiculo`

Agrega `Localizacao` (GPS) e `List<Sensor>` (sensores instalados). Delega as consultas de coordenadas ao record `Localizacao`:

```java
public double getLatitude()  { return localizacao.latitude();  }
public double getLongitude() { return localizacao.longitude(); }
```

⚠️ `setTipoVeiculo()` está quebrado — o método atribui `this.tipoVeiculo = tipoVeiculo` sem parâmetro, ou seja, nunca altera o valor.

---

### `Localizacao` *(record Java 16+)*

Tipo de valor imutável que representa uma leitura de GPS em um instante específico.

```java
public record Localizacao(
    double latitude,
    double longitude,
    double velocidadeKmh,
    Instant timestamp
) { ... }
```

Por ser um `record`, garante imutabilidade por design: uma leitura de GPS não deve ser alterada após a captura. Inclui o método auxiliar `toGoogleMapsUrl()` que converte as coordenadas em link direto para o Google Maps.

⚠️ **Dívida técnica grave:** a classe `LocalizacaoDAO` está definida no **mesmo arquivo** que `Localizacao`, dentro do pacote `model/`. Essa classe deveria estar em `repository/LocalizacaoDAO.java`.

---

### `SimuladorSensor` *(implements Runnable)*

Executa em uma `Thread` separada para simular transmissão contínua de dados de um sensor. A cada 3 segundos:

- 15% de chance de injetar um **pico** (150% do limite máximo)
- 15% de chance de injetar uma **queda brusca** (-5.0)
- 70% de chance de gerar uma **leitura normal aleatória**

Após cada leitura, chama `monitor.processarNovaLeitura()` para que o `Monitoramento` avalie se um alerta deve ser disparado.

---

### `ProcessadorTelemetria`

Orquestra um ciclo completo de telemetria geográfica: lê o `SensorGeografico`, empacota os dados em um `Localizacao` com o `Instant.now()` exato, e envia para o banco via `GeralDAO.salvarLocalizacao()`.

---

## Monitoramento e Alertas

### `Monitoramento`

Central de regras de um veículo. Mantém uma lista de `GatilhoSensor` (regras ativas) e avalia cada nova leitura recebida:

```
Nova leitura → encontra o sensor na lista de gatilhos
             → avalia se valorRecebido > limiteMaximo
             → se verdadeiro: dispara alarme → notifica Central + Veiculo
```

O método `avaliarGatilho()` é `public` para permitir testes unitários isolados da lógica de avaliação.

---

### `GatilhoSensor`

Representa uma regra de segurança: "se o sensor X superar o valor Y, disparar alerta". Associa um `Sensor` a um `limiteMaximo` de referência. Usado por `Monitoramento` para avaliar leituras.

---

### `Central`

Representa a central de monitoramento que recebe alertas de múltiplos veículos. Mantém uma lista de `veiculosEmEmergencia` e exibe a localização do veículo no momento do alerta.

⚠️ **Dívida técnica grave:** `Central` acessa o banco de dados diretamente via `ConexaoBanco` no método `lerMensagensFrotistas()`. Toda a lógica SQL deveria estar em um `MensagemDAO` dentro do pacote `repository/`.

---

### `RegistroAlerta`

Registra o ciclo de vida de um alerta: horário de início, horário de fim e se ainda está ativo. O método `getDuracaoFormatada()` calcula e formata o tempo decorrido desde o início do alerta até seu encerramento (ou até agora, se ainda ativo).

---

## Autenticação e Autorização

### `Autenticavel` *(interface)*

Define o contrato de autenticação multifator com três sobrecargas de `validarFator()`:

| Sobrecarga | Fatores | Usuários |
|---|---|---|
| `validarFator(String senha)` | 1 fator | `Cliente`, `Instalador`, `Motorista` |
| `validarFator(String senha, String token)` | 2 fatores | `Gestor`, `Operador` |
| `validarFator(String senha, String token, String bio)` | 3 fatores | `Equipe` (Admin) |

`getQuantidadeFatores()` permite que o sistema saiba quantos fatores exigir antes de tentar a validação.

⚠️ **Bug:** os métodos `default` retornam `true & true` usando o operador **bitwise** `&` em vez do operador **lógico** `&&`. Embora o resultado seja o mesmo para booleanos neste caso, a intenção correta é `&&`. Além disso, os métodos `default` retornam `true` sem validar nada — as subclasses sobrescrevem corretamente, mas qualquer classe que use o `default` passa automaticamente na autenticação.

---

### `PerfilAcesso` *(enum com comportamento)*

Centraliza todo o controle de acesso baseado em papéis (RBAC). Cada constante carrega um `nivel` numérico e uma `descricao`, além de implementar `temPermissao(String acao)`.

```
MOTORISTA (0) → INICIAR_VIAGEM, CALCULAR_ROTA, VER_FROTA
FROTISTA  (1) → + CRIAR_USUARIO, MANTER_VEICULO, MENSAGEM_CENTRAL
OPERADOR  (2) → + VER_USUARIO, EDITAR_USUARIO, EXCLUIR_USUARIO, VER_SENSOR, ADICIONAR_SENSOR
ADMIN     (3) → + VER_LOGS, LIMPAR_LOGS (acesso total)
```

O método estático `porNivel(int nivel)` converte um inteiro do banco de dados na constante correta, lançando `IllegalArgumentException` para valores inválidos.

---

### `Login`

Ponto de entrada do sistema no **modo console** (anterior à interface JavaFX). Gerencia o fluxo de cadastro, login e navegação pós-autenticação. Instancia `UsuarioDAO` para autenticar e, após o login, chama `usuarioLogado.acessarSistema()` — o polimorfismo redireciona para o painel correto de cada subclasse.

---

## Classes Auxiliares

### `TelaDeInicio`

Bootstrapping do sistema em modo console. Provavelmente substituída pela classe `Main.java` da camada `application/` na versão com JavaFX.

---

## Mapa de Dependências

```
model/ (núcleo — deveria ser isolado)
│
├── depende de → repository/   (GeralDAO, UsuarioDAO, SensorDAO, VeiculoDAO, LogDAO)
│                               ← violação em: Gestor, Operador, Equipe, Cliente, Motorista
│
├── depende de → db/            (ConexaoBanco)
│                               ← violação em: Central, Equipe, Cliente, Motorista, Localizacao (LocalizacaoDAO)
│
└── não deveria depender de nada externo ao próprio pacote model/
```

---

## ⚠️ Dívidas Técnicas

Problemas identificados neste pacote que devem ser corrigidos nas próximas etapas:

| # | Arquivo | Problema | Correção Sugerida |
|---|---|---|---|
| 1 | `Localizacao.java` | `LocalizacaoDAO` definida dentro do arquivo de um record de domínio | Mover para `repository/LocalizacaoDAO.java` |
| 2 | `Central.java` | Acessa `ConexaoBanco` e `PreparedStatement` diretamente | Criar `repository/MensagemDAO.java` |
| 3 | `Equipe.java` | Método `detalharUsuario()` com SQL direto | Mover lógica SQL para `GeralDAO` ou novo DAO |
| 4 | `Cliente.java` | Acessa banco diretamente para ver sensores | Delegar para `SensorDAO` |
| 5 | `Motorista.java` | Instancia `LocalizacaoDAO` (classe de `model/`) diretamente | Usar `repository/LocalizacaoDAO` após correção do item 1 |
| 6 | `Autenticavel.java` | Métodos `default` usam `&` (bitwise) em vez de `&&` (lógico) e retornam `true` sem validar | Substituir `&` por `&&`; implementar validação real |
| 7 | `Usuario.java` | `setSenha()` não atualiza o atributo (bug de parâmetro com `S` maiúsculo) | Corrigir para `this.senha = senha` com `s` minúsculo |
| 8 | `Veiculo.java` | `setTipoVeiculo()` não recebe parâmetro e não atualiza nada | Adicionar parâmetro `String tipoVeiculo` ao método |
| 9 | `Sensor.java` | `idSensor` estático pode gerar conflitos em ambiente multi-thread | Usar ID gerado pelo banco de dados |

---

*Documentação mantida pelo Arquiteto de Software. Atualizar sempre que novas classes forem adicionadas ou comportamentos existentes forem alterados.*
