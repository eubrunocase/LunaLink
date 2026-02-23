# LunaLink Application (API)
## Visão geral
O é uma API backend voltada para o gerenciamento de rotinas e serviços em um contexto de **condomínios/residenciais**, com foco em **usuários** e **reservas de espaços** (ex.: salão, churrasqueira, quadras). O projeto expõe endpoints REST para criação e administração de reservas, consulta de disponibilidade e gerenciamento de usuários. **LunaLink**
A aplicação foi estruturada com uma separação clara de responsabilidades (camadas **Web/Controller**, **Application/Facades/Ports** e **Domain**), facilitando a manutenção, testes e evolução do sistema.
## Principais funcionalidades
### Usuários
- Criar usuário
- Listar usuários
- Buscar usuário por ID
- Atualizar usuário
- Remover usuário

### Reservas
- Criar reserva para o usuário autenticado
- Listar reservas
- Buscar reserva por ID
- Atualizar reserva
- Remover reserva
- Verificar **disponibilidade** de um espaço por data
- Aprovar reserva
- Rejeitar reserva

## Arquitetura (alto nível)
O projeto segue uma abordagem em camadas, com ênfase em desacoplamento via e uso de na camada de aplicação: **ports****facades**
- **Web (`web/controller`, `web/dto`)**
Responsável por receber requisições HTTP, validar payloads e retornar respostas padronizadas (DTOs).
- **Application (`application/facades`, `application/ports`)**
Orquestra casos de uso por meio de _facades_ e contratos (ports), reduzindo acoplamento entre controller e regras de negócio.
- **Domain (`domain/model`)**
Contém os modelos e regras centrais do negócio.
- **Infrastructure (`infrastructure/mapper`)**
Componentes de suporte como mapeadores entre entidades e DTOs.

## Stack e tecnologias
- **Java 23**
- **Spring MVC** (API REST)
- **Spring Security** (uso de para identificar o usuário autenticado) `Authentication`
- **Jakarta Validation** () `jakarta.validation.Valid`
- **Lombok** (quando aplicável no projeto)
- Estilo de DTOs para entrada/saída (`Request/Response/Create DTO`)

## Endpoints (resumo)
### Base paths
- `/lunaLink/users`
- `/lunaLink/reservation`

### Usuários () `/lunaLink/users`
- `GET /{id}` — busca usuário por ID
- `GET /` — lista todos os usuários
- `POST /create` — cria usuário
- `PUT /update/{id}` — atualiza usuário
- `DELETE /delete/{id}` — remove usuário

### Reservas () `/lunaLink/reservation`
- `POST /` — cria uma nova reserva (para o usuário autenticado)
- `GET /` — lista reservas
- `GET /{id}` — busca reserva por ID
- `PUT /{id}` — atualiza reserva
- `DELETE /{id}` — remove reserva
- `GET /checkAvaliability/{date}/{spaceId}` — verifica disponibilidade de um espaço numa data
- `PUT /{id}/approve` — aprova reserva
- `PUT /{id}/reject` — rejeita reserva


> Observação: a criação e checagem de disponibilidade utilizam o usuário autenticado via . A identificação do usuário é obtida pelo login retornado em . `Authentication``authentication.getName()`
> 

## Padrões de DTOs
O projeto utiliza DTOs específicos por caso de uso, por exemplo:
- `CreateDTO` para criação
- `RequestDTO` para atualizações/entrada genérica
- `ResponseDTO` para respostas da API

Isso ajuda a manter contratos estáveis e evita expor modelos internos diretamente.
## Como executar 
1. **Clonar o repositório**
2. **Configurar variáveis de ambiente** e/ou /`application.yml` (ex.: banco, autenticação, etc.) `application.properties`
3. **Subir a aplicação**
    - via IDE (IntelliJ) executando a classe principal Spring Boot (se aplicável)
    - ou via build tool (Maven/Gradle), conforme configuração do projeto


## Autenticação e permissões
Algumas rotas dependem do contexto de autenticação (por exemplo, criação de reserva e checagem de disponibilidade). Garanta que o ambiente esteja configurado com um provedor de autenticação compatível e que o cliente HTTP esteja enviando as credenciais/tokens conforme a configuração do projeto.
