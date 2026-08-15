# [US-03] Registro Digital de Ocorrências e Notificação em Tempo Real

## Descrição (Contexto de Negócio)

Atualmente, o processo de registro de queixas, sugestões ou incidentes (o "livro negro" ou livro de ocorrências) é físico, burocrático e passivo, exigindo que o morador solicite o caderno e que o administrador vá verificá-lo manualmente. O sistema deve digitalizar esse processo, permitindo a abertura de ocorrências pelo aplicativo e garantindo que a administração seja alertada instantaneamente para uma rápida tomada de decisão.

## História de Usuário

**Como** residente do condomínio (`RESIDENT_ROLE`),
**Eu quero** registrar uma nova ocorrência através do aplicativo, detalhando a situação e o momento exato do acontecimento,
**Para que** minha solicitação seja formalizada sem a necessidade do livro físico e o administrador seja alertado imediatamente.

## Critérios de Aceite

- **CA01: Abertura da Ocorrência**
  **Dado** que um morador acessa a tela de ocorrências no aplicativo,
  **Quando** ele preencher os dados obrigatórios (texto descritivo e a data/hora do ocorrido) e enviar o formulário,
  **Então** o sistema deve salvar o registro atrelado ao perfil daquele morador e retornar uma mensagem de sucesso (`HTTP 201 Created`).

- **CA02: Evento e Notificação em Tempo Real (WebSocket)**
  **Dado** que uma nova ocorrência acabou de ser salva com sucesso no banco de dados,
  **Quando** o sistema finalizar a transação,
  **Então** um evento interno deve ser disparado para enviar, de forma assíncrona, uma notificação via WebSocket (STOMP) diretamente para o canal do Administrador (`ADMIN_ROLE`), acendendo um alerta no aplicativo dele.

- **CA03: Rastreabilidade (Auditoria de Criação)**
  **Dado** a criação de um registro,
  **Quando** o sistema persistir a entidade,
  **Então** deve existir a diferenciação clara entre a "data/hora em que a situação aconteceu" (fornecida pelo usuário) e a "data/hora em que o registro foi feito no sistema" (gerada automaticamente pelo backend).

- **CA04: Validação de Dados (Prevenção de Erros)**
  **Dado** a tentativa de registrar uma ocorrência,
  **Quando** o morador enviar a requisição sem o texto descritivo ou sem a data do ocorrido,
  **Então** o sistema deve bloquear a ação e retornar um erro estruturado (`HTTP 400 Bad Request`) via `GlobalExceptionHandler`.

## Notas Técnicas e de Arquitetura (Para a equipe de Desenvolvimento)

- **Nova Entidade de Domínio:** Criar a entidade `Occurrence` (ou `Incident`).
    - Atributos principais:
        - `id` (UUID)
        - `user` (Referência a `Users`)
        - `description` (String, texto livre longo, ex: `@Column(columnDefinition="TEXT")`)
        - `occurrenceDate` (`LocalDateTime` — input do usuário)
        - `createdAt` (`LocalDateTime` — gerado pelo `@PrePersist`)

- **Fluxo Orientado a Eventos (EDA):**
    - Criar a classe `OccurrenceRegisteredEvent` no pacote `domain/events`.
    - Adicionar o método `publishOccurrenceRegisteredEvent` no seu `EventPublisher`.
    - No `OccurrenceService`, após o `save()`, publicar este evento.

- **Integração com WebSocket:**
    - Criar ou aproveitar uma classe de Listener (ex: `OccurrenceEventListener`).
    - Capturar o evento assíncrono (`@Async` `@EventListener`).
    - Buscar o usuário com `ADMIN_ROLE` e usar o `SimpMessagingTemplate` para injetar o DTO de notificação na fila do administrador (ex: `/topic/notifications/{adminId}`).

- **Camada Web:**
    - Criar `OccurrenceRequestDTO` e `OccurrenceResponseDTO`.
    - Expor o endpoint `POST /lunaLink/occurrences` restrito a `RESIDENT_ROLE`.