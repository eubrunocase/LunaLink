# [US-02] Geração de Relatório Mensal de Reservas Tarifadas

## Descrição (Contexto de Negócio)

As reservas de áreas comuns específicas, como o Salão de Festas e a Churrasqueira, geram custos adicionais de manutenção e limpeza. Atualmente, o repasse desses custos para a empresa terceirizada de cobrança é um processo manual. O sistema deve fornecer uma extração automatizada para garantir que nenhum morador deixe de ser cobrado por reservas efetivamente consumadas.

## História de Usuário

**Como** administrador do condomínio (`ADMIN_ROLE`),
**Eu quero** gerar um relatório consolidado com todas as reservas consumadas em um determinado mês,
**Para que** eu possa enviar os dados exatos à empresa terceirizada responsável pelo faturamento das taxas.

## Critérios de Aceite

- **CA01: Filtragem por Período e Status**
  **Dado** que o administrador acesse a funcionalidade de relatórios,
  **Quando** ele solicitar o relatório de um mês específico (ex: Maio/2026),
  **Então** o sistema deve retornar apenas as reservas daquele período que possuam o status de confirmação válido (ex: `APPROVED` ou `COMPLETED`), ignorando reservas canceladas ou rejeitadas (`REJECTED`).

- **CA02: Filtragem por Tipo de Espaço**
  **Dado** que existem múltiplos espaços no condomínio,
  **Quando** o relatório for gerado,
  **Então** o sistema deve listar exclusivamente as reservas pertencentes aos espaços tarifados (Salão de Festas e Churrasqueira), filtrando pelo `SpaceType`.

- **CA03: Exibição dos Dados do Morador e Reserva**
  **Dado** que o relatório foi processado com sucesso,
  **Então** o documento ou carga de dados (Payload/DTO) retornado deve conter obrigatoriamente:
    - Nome do morador responsável (`Users.login` ou nome completo).
    - Identificação da residência/unidade (atrelada ao cadastro do usuário).
    - Data exata em que a reserva ocorreu.
    - Tipo do espaço utilizado.

## Notas Técnicas e de Arquitetura (Para a equipe de Desenvolvimento)

- **Camada de Infraestrutura/Persistência:** Criar uma *query* customizada no `ReservationRepository` utilizando Spring Data JPA. Exemplo de escopo:
  `findByDateBetweenAndStatusInAndSpaceTypeIn(LocalDate start, LocalDate end, List<ReservationStatus> status, List<SpaceType> types)`.

- **Camada de Aplicação (Ports):** Implementar o novo método na `ReservationServicePort` e a lógica de negócio na classe `ReservationService`.

- **Camada Web:** Criar um novo DTO (ex: `MonthlyReservationReportResponseDTO`) para devolver apenas os dados estritos definidos no CA03, evitando o vazamento de dados sensíveis da entidade `Users` ou de infraestrutura da entidade `Reservation`.

- **Endpoint sugerido:** `GET /lunaLink/reservation/report/monthly?month=5&year=2026` acessível apenas para usuários autenticados com a *role* de Admin.