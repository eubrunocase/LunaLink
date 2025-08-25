# LunaLink – API de Gerenciamento de Espaços, Residentes e Reservas
Este repositório implementa um backend moderno para gerenciamento de espaços, residentes e reservas, contemplando autenticação segura, autorização baseada em roles e uma arquitetura modular com camadas bem definidas.
## Visão Geral
- Plataforma: API REST baseada em Spring Boot com autenticação JWT.
- Foco do domínio: gestão de residentes, administradores, espaços e reservas.
- Principais modelos de domínio:
    - Residentes
    - Administradores
    - Espaços
    - Reservas
    - Reservas Mensais (MonthlyReservations)

- Segurança: configuração de SecurityFilterChain, UserDetailsService que busca usuários em dois repositórios (Administrador e Resident), e autenticação baseada em tokens.
- Tecnologias: Java 24, Spring MVC, Spring Security, Lombok (quando utilizado), Jakarta EE (jakarta.*), JPA/Hibernate, BCrypt para password encoding.
- Persistência: repositórios de domínio ( ResidentRepository, AdministratorRepository, SpaceRepository, ReservationRepository, MonthlyReservationRepository, etc.).
- API documentation: endpoints de swagger acessíveis publicamente para facilitar testes e documentação (`/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html`).

## Arquitetura e Organização
- Camadas principais:
    - Web: controllers (ex.: ResidentController) expõem os endpoints REST.
    - Application/Business: services (ex.: ReservationService, AuthorizationResidentService) contêm a lógica de negócio.
    - Infrastructure: repositórios e componentes de infraestrutura (ex.: SecurityConfiguration, TokenService, SecurityFilter).
    - Core/Domain: entidades como Resident, Reservation, Space, MonthlyReservations (domínios centrais do sistema).

- Segurança:
    - SecurityConfiguration define as regras de autorização por rota e método HTTP.
    - Autenticação baseada em UserDetailsService que consulta AdministratorRepository e ResidentRepository.
    - PasswordEncoder BCryptPasswordEncoder.
    - Stateless sessions via JWT (TokenService e SecurityFilter).

- DTOs e mapeamento:
    - Data Transfer Objects (DTOs) para requests/responses do domínio de segurança e de residentes.
    - Exemplos de DTOs visíveis no código: AuthenticationDTO, LoginResponseDTO, etc.

## Fluxos Principais
- Autenticação
    - Endpoint de login público: POST /lunaLink/auth/login
    - Retorna um token JWT (quando credenciais válidas)
    - Sessões são stateless (sem sessão no servidor)

- Autorização por role
    - ADMINISTRADOR tem acesso a endpoints de administração (ex.: /lunaLink/adm/**)
    - Residentes podem ter acesso específico conforme configuração (ex.: /lunaLink/resident, /lunaLink/reservation, etc.)
    - Endpoints de swagger continuam públicos para facilitar documentação

- Operações de Residentes
    - POST /lunaLink/resident: criar um novo residente (permitido sem autenticação conforme configuração)
    - GET /lunaLink/resident: listar residentes (requer ADMINISTRATOR)
    - DELETE /lunaLink/resident/{id}: excluir residente (requer autenticação, conforme configuração)
    - PUT /lunaLink/resident/{id}: atualizar residente

- Operações de Reservas
    - POST /lunaLink/reservation: criar reserva (permitido publicamente)
    - GET /lunaLink/reservation: listar reservas (permitido publicamente)
    - DELETE /lunaLink/reservation: excluir reserva (requer ADMINISTRATOR)
    - Outras operações podem existir conforme implementação de Service/Controller

- Operações de Espaços
    - POST /lunaLink/space/**: criação de espaço (permitido publicamente)
    - GET /lunaLink/space/**: consulta de espaços (permitido publicamente)

- Perfis de usuário via Token
    - GET /lunaLink/resident/profile: retorna o perfil do usuário logado com base no token JWT fornecido em Authorization


> Observação: as regras de autorização estão definidas em SecurityConfiguration, incluindo permissões para endpoints de docs, login e recursos sensíveis.
> 

## Estrutura de Dados (Domínio)
- Resident
    - Usuário com login e senha criptografada
    - Perfil utilizado para autenticação via Token JWT

- Administrator
    - Usuário com privilégios administrativos
    - Autenticação via login/senha

- Space
    - Tipo de espaço (ex.: sala, ambiente, etc.)
    - Identificação e disponibilidade

- Reservation
    - Data da reserva
    - Espaço reservado
    - Residente que realizou a reserva

- MonthlyReservations
    - Registro de reservas mensais para um residente específico

## Endpoints (Resumo)
- Autenticação e docs
    - POST /lunaLink/auth/login — autenticação (public)
    - /swagger-ui/**, /v3/api-docs/**, /swagger-ui.html — documentação (public)

- Residentes
    - POST /lunaLink/resident — criar residente (public)
    - GET /lunaLink/resident — listar residentes (ADMINISTRATOR)
    - DELETE /lunaLink/resident/{id} — excluir residente (autenticado)
    - PUT /lunaLink/resident/{id} — atualizar residente (autenticado)
    - GET /lunaLink/resident/profile — perfil do usuário via token (autenticado)

- Reservas
    - POST /lunaLink/reservation — criar reserva (public)
    - GET /lunaLink/reservation — listar reservas (public)
    - DELETE /lunaLink/reservation — excluir reserva (ADMINISTRATOR)

- Espaços
    - POST /lunaLink/space/** — criar espaço (public)
    - GET /lunaLink/space/** — listar espaço (public)

- Outras
    - GET /lunaLink/monthlyReservation/** — operações relacionadas a monthly reservations (public)

Observação: a configuração de permissões pode exigir autenticação para alguns endpoints não explicitamente protegidos no código de rota. Consulte SecurityConfiguration para entender as regras completas.
## Modelos de Segurança
- Autenticação
    - UserDetailsService que consulta:
        - AdministratorRepository.findByLogin(username)
        - ResidentRepository.findByLogin(username)

    - PasswordEncoder: BCryptPasswordEncoder
    - AuthenticationManager com DaoAuthenticationProvider

- Autorização
    - Roles utilizadas: ADMINISTRATOR (e possivelmente outros papéis no domínio)
    - Contexto de segurança baseado em JWT (token-based, stateless)

- Proteção de Endpoints
    - CSRF desativado (CSRF disabled)
    - Sessão: STATELESS
    - Filtros de segurança adicionados antes do UsernamePasswordAuthenticationFilter

## Configurações e Dependências
- Linguagem e plataforma:
    - Java SDK 24
    - Spring Boot (com Spring MVC e Spring Security)
    - Jakarta EE (jakarta.* imports)
    - Lombok (quando utilizado)

- Persistência:
    - JPA/Hibernate (via repositórios)
    - Banco de dados relacional (ex.: PostgreSQL, MySQL) — configurações via application.properties/application.yaml

- Build:
    - Maven ou Gradle (recomendado conforme o projeto)

- Segurança:
    - BCryptPasswordEncoder
    - JWT para autenticação/autorizações

Sugestão de configuração típica (exemplos genéricos; adapte conforme seu ambiente):
- spring.datasource.url, spring.datasource.username, spring.datasource.password
- spring.jpa.hibernate.ddl-auto (update/create)
- jwt.secret (ou configuração equivalente para o TokenService)

## Build e Execução
- Pré-requisitos:
    - JDK 17+ (ajuste se necessário para Java 24)
    - Maven ou Gradle
    - Banco de dados configurado e acessível

- Com Maven:
    - mvn clean package
    - java -jar target/nome-do-aplicativo.jar
    - Ou: mvn spring-boot:run

- Com Gradle:
    - ./gradlew bootRun
    - Ou: ./gradlew clean build

- Testes
    - mvn test ou ./gradlew test

Notas:
- Ajuste as propriedades de conexão do banco de dados e a configuração de JWT conforme o ambiente (dev, test, prod).
- As URLs de documentação do Swagger ficam públicas para facilitar testes.

## Desenvolvimento e Contribuição
- Estrutura recomendada:
    - Camada Web: controllers (ex.: ResidentController)
    - Camada Application/Business: services (ex.: ReservationService)
    - Camada Infra: repositórios, SecurityConfiguration, TokenService
    - Domínio: entidades como Resident, Reservation, Space, MonthlyReservations

- Padronização:
    - Use DTOs para requests/responses quando necessário
    - Adote tratamento de exceções claro e mensagens descritivas
    - Documente endpoints com comentários ou ferramentas de documentação (Swagger)

- Contribuição:
    - Fork do repositório
    - Criar branch de feature/bugfix
    - Submeter pull request com descrição detalhada

## Observações Técnicas Relevantes
- O código demonstra uma configuração de segurança com autenticação baseada em dois repositórios de usuário (administradores e residentes), o que facilita um fluxo de login centralizado com roles distintas.
- O serviço de autenticação gerencia a validação de credenciais via DaoAuthenticationProvider e user details de diferentes origens.
- O fluxo de validação de reservas implementa checagens de conflito antes de salvar, incluindo disponibilidade de espaço e unicidade de reservas por usuário.
- O sistema utiliza um modelo de reservas mensais para manter um histórico adicional por residente.

## Perguntas Frequentes (FAQ)
- Como funciona a autenticação?
    - O endpoint de login recebe credenciais, valida via AuthenticationManager e retorna um token JWT utilizado para autorizar chamadas subsequentes.

- Quais endpoints são públicos?
    - Swagger (docs) e o endpoint de login são públicos. Vários endpoints de POST/GET para recursos como espaços e reservas podem ser públicos conforme configuração, mas muitas rotas requerem autenticação conforme SecurityConfiguration.

- Onde posso encontrar a documentação de API?
    - Swagger UI em /swagger-ui/** e /v3/api-docs/**
