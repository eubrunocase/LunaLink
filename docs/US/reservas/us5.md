# [US-05] Evolução do Fluxo de Reserva de Espaços Comuns: Exclusividade Diária, Vistoria e Termo de Responsabilidade

## Descrição (Contexto de Negócio)

O sistema já possui um fluxo funcional de reserva de espaços comuns (Salão de Festas, Churrasqueira e Campo de Futebol), com solicitação pelo morador e aprovação/rejeição pelo administrador. No entanto, esse fluxo precisa evoluir em dois pontos centrais:

1. **Regra de exclusividade diária entre espaços:** atualmente o sistema valida apenas a disponibilidade do espaço solicitado, mas não impede que espaços *diferentes* sejam reservados para a mesma data por moradores *diferentes*. Como Salão de Festas, Churrasqueira e Campo de Futebol normalmente atendem ao mesmo evento/celebração no condomínio, deve haver no máximo **uma reserva por dia, considerando os três espaços em conjunto** — exceto quando todas as reservas daquele dia pertencerem ao **mesmo morador**.

2. **Vistoria e formalização de responsabilidade:** para reduzir disputas sobre danos e uso indevido dos equipamentos do Salão de Festas e da Churrasqueira, o processo de aprovação deve passar a incluir uma etapa de vistoria pré-evento conduzida por um funcionário, seguida da assinatura digital de um termo de responsabilidade pelo morador antes da confirmação final da reserva. O Campo de Futebol, por não possuir equipamentos sob custódia, fica fora da etapa de vistoria.

Esta user story cobre exclusivamente os espaços **Salão de Festas**, **Churrasqueira** e **Campo de Futebol**.

## História de Usuário

**Como** morador do condomínio (`RESIDENT_ROLE`),
**Eu quero** solicitar a reserva de um espaço comum, ter a garantia de exclusividade do meu evento na data escolhida, e passar por um processo formal de vistoria e assinatura de responsabilidade,
**Para que** minha reserva seja confirmada com segurança tanto para mim quanto para o condomínio, sem conflito de uso com outros moradores no mesmo dia.

**Como** funcionário do condomínio (`EMPLOYEE_ROLE`),
**Eu quero** ser notificado para realizar a vistoria dos equipamentos de um espaço reservado, preenchendo um formulário com evidência fotográfica,
**Para que** o estado dos itens seja documentado antes do uso, resguardando o condomínio de disputas sobre danos.

**Como** administrador do condomínio (`ADMIN_ROLE`),
**Eu quero** continuar aprovando ou rejeitando as solicitações de reserva como já ocorre hoje,
**Para que** eu mantenha o controle sobre o uso dos espaços comuns, agora considerando também a nova regra de exclusividade diária.

## Regras de Negócio

- **RN01 — Exclusividade diária entre espaços:** Para uma mesma data, só pode existir uma reserva ativa (não rejeitada/cancelada) entre os espaços Salão de Festas, Churrasqueira e Campo de Futebol, **independentemente de qual desses três espaços seja escolhido**.
- **RN02 — Exceção por mesmo morador:** A única exceção à RN01 é quando **todas** as reservas conflitantes na mesma data pertencem ao **mesmo morador solicitante**. Nesse caso, esse morador (e somente ele) pode reservar mais de um desses três espaços na mesma data.
- **RN03 — Vistoria obrigatória apenas para Salão de Festas e Churrasqueira:** o Campo de Futebol não passa pela etapa de vistoria nem de termo de responsabilidade, seguindo direto de aprovação do administrador para confirmação.
- **RN04 — Lista de convidados visível à portaria/funcionário no dia do evento:** os nomes cadastrados na reserva devem estar acessíveis ao funcionário para consulta no dia da reserva.
- **RN05 — Foto individual por item na vistoria:** tanto na vistoria pré-evento quanto na pós-evento, cada equipamento deve ser fotografado separadamente, não sendo permitida uma única foto genérica cobrindo múltiplos itens.
- **RN06 — Vistoria pós-evento obrigatória:** para Salão de Festas e Churrasqueira, no dia seguinte ao uso do espaço, o funcionário deve preencher um segundo formulário de vistoria (pós-evento), repetindo a confirmação individual de estado e foto de cada item, para identificar eventuais avarias ocorridas durante o uso.
- **RN07 — Marcação de convidados restrita ao dia do evento:** o funcionário só pode marcar convidados como "entrou" na data em que a reserva efetivamente ocorre. Fora dessa data, a lista deve ficar somente para consulta, sem permitir a marcação.
- **RN08 — Marcação de convidado é irreversível:** uma vez que o funcionário marca um convidado como "entrou", essa ação não pode ser desfeita.

## Novo Fluxo de Reserva (Salão de Festas e Churrasqueira)

1. Morador solicita a reserva do espaço, informando data, observações e lista de convidados.
2. Sistema valida disponibilidade da data aplicando as regras RN01 e RN02.
3. Administrador recebe notificação e aprova ou rejeita a solicitação (fluxo já existente).
4. Se aprovada, funcionário (`EMPLOYEE_ROLE`) recebe notificação para iniciar a vistoria pré-evento.
5. Funcionário preenche o formulário de vistoria pré-evento, confirmando o estado de cada equipamento do espaço individualmente — com uma foto específica por item — e podendo incluir observações gerais.
6. Após a submissão da vistoria pré-evento, o sistema envia ao morador o Termo de Responsabilidade de Uso para assinatura/confirmação digital.
7. Morador assina/confirma o termo.
8. A reserva é então marcada como confirmada.
9. No dia do evento, o funcionário pode consultar a lista de convidados e marcar cada convidado como "entrou" conforme eles chegam.
10. No dia seguinte ao uso do espaço, o funcionário preenche um segundo formulário — a vistoria pós-evento — confirmando novamente o estado de cada item individualmente (com foto por item) e podendo incluir observações sobre eventuais avarias.

## Fluxo do Campo de Futebol (sem vistoria)

1. Morador solicita a reserva do espaço, informando data, observações e lista de convidados.
2. Sistema valida disponibilidade da data aplicando as regras RN01 e RN02.
3. Administrador aprova ou rejeita a solicitação.
4. Se aprovada, a reserva é confirmada diretamente (sem vistoria e sem termo).

## Critérios de Aceite

- **CA01: Bloqueio por reserva de outro espaço na mesma data (RN01)**
  **Dado** que já existe uma reserva ativa (`PENDING`, `AWAITING_INSPECTION`, `AWAITING_SIGNATURE` ou `APPROVED`/`CONFIRMED`) para o Salão de Festas, Churrasqueira **ou** Campo de Futebol em uma determinada data, feita por um morador X,
  **Quando** um morador Y (diferente de X) tentar solicitar reserva de qualquer um desses três espaços na mesma data,
  **Então** o sistema deve bloquear a solicitação e retornar um erro `409 Conflict` informando que já existe reserva para a data.

- **CA02: Exceção para o mesmo morador (RN02)**
  **Dado** que já existe uma reserva ativa de um dos três espaços em uma determinada data, feita pelo morador X,
  **Quando** o próprio morador X solicitar a reserva de um outro espaço dentre os três para a mesma data,
  **Então** o sistema deve permitir a criação da nova reserva normalmente, seguindo o restante do fluxo de aprovação.

- **CA03: Novos campos na solicitação de reserva**
  **Dado** que um morador está preenchendo o formulário de solicitação de reserva,
  **Quando** ele submeter o pedido,
  **Então** o sistema deve aceitar e persistir, além dos campos já existentes (data e espaço), um campo de observações (texto livre, opcional) e uma lista de convidados (nomes completos).

- **CA04: Encaminhamento para vistoria após aprovação (Salão e Churrasqueira)**
  **Dado** que uma reserva de Salão de Festas ou Churrasqueira foi aprovada pelo administrador,
  **Quando** o sistema processar essa aprovação,
  **Então** a reserva deve mudar para um status de aguardando vistoria, e um funcionário (`EMPLOYEE_ROLE`) deve ser notificado para realizar a vistoria.

- **CA05: Preenchimento do formulário de vistoria pré-evento**
  **Dado** que um funcionário acessa o formulário de vistoria pré-evento de uma reserva pendente de inspeção,
  **Quando** ele confirmar o estado de cada equipamento do espaço correspondente individualmente, anexando uma foto específica para cada item, e opcionalmente incluir uma observação geral,
  **Então** o sistema deve salvar o registro de vistoria vinculado à reserva, incluindo a foto de cada item separadamente, e disparar o envio do termo de responsabilidade ao morador.
    - Equipamentos da Churrasqueira: grelhas, aparatos de churrasco, cadeiras, tábuas, freezer.
    - Equipamentos do Salão de Festas: mesas, cadeiras, freezer 1, freezer 2, fogão, televisão.

- **CA06: Bloqueio de vistoria incompleta**
  **Dado** que o funcionário está preenchendo um formulário de vistoria (pré ou pós-evento),
  **Quando** ele tentar submeter sem confirmar o estado de todos os equipamentos do espaço ou sem anexar a foto obrigatória de cada item individualmente,
  **Então** o sistema deve bloquear a submissão e retornar um erro estruturado (`HTTP 400 Bad Request`).

- **CA12: Notificação e preenchimento da vistoria pós-evento**
  **Dado** que uma reserva de Salão de Festas ou Churrasqueira teve sua data de uso concluída,
  **Quando** chegar o dia seguinte ao evento,
  **Então** o sistema deve notificar um funcionário (`EMPLOYEE_ROLE`) para preencher o formulário de vistoria pós-evento, exigindo a mesma estrutura de confirmação individual e foto por item da vistoria pré-evento, além de um campo de observação para registrar eventuais avarias.

- **CA13: Marcação de entrada de convidados restrita ao dia do evento**
  **Dado** que um funcionário está consultando a lista de convidados de uma reserva,
  **Quando** a data atual for igual à data do evento,
  **Então** o sistema deve permitir que o funcionário marque cada convidado individualmente como "entrou"; **quando** a data atual for diferente da data do evento, o sistema deve exibir a lista apenas para consulta, sem permitir marcação.

- **CA14: Irreversibilidade da marcação de convidado**
  **Dado** que um convidado já foi marcado como "entrou",
  **Quando** o funcionário ou qualquer outro usuário tentar desfazer essa marcação,
  **Então** o sistema deve bloquear a ação e retornar um erro estruturado, mantendo o registro original da marcação.

- **CA07: Envio e assinatura do Termo de Responsabilidade**
  **Dado** que a vistoria de uma reserva foi concluída,
  **Quando** o sistema notificar o morador,
  **Então** o morador deve poder visualizar um termo de responsabilidade (com parágrafo genérico placeholder, a ser definido posteriormente) e confirmar sua assinatura/aceite dentro do aplicativo.

- **CA08: Confirmação final da reserva após assinatura**
  **Dado** que o morador assinou/confirmou o termo de responsabilidade,
  **Quando** o sistema registrar essa confirmação,
  **Então** a reserva deve mudar para o status final de confirmada, tornando-se visível como reserva ativa para fins da regra RN01.

- **CA09: Campo de Futebol não passa por vistoria (RN03)**
  **Dado** que uma reserva de Campo de Futebol foi aprovada pelo administrador,
  **Quando** o sistema processar essa aprovação,
  **Então** a reserva deve ser marcada como confirmada diretamente, sem passar pelas etapas de vistoria ou termo de responsabilidade.

- **CA10: Rejeição interrompe o fluxo a qualquer momento**
  **Dado** que uma reserva está em qualquer etapa do novo fluxo (aguardando aprovação, vistoria ou assinatura),
  **Quando** o administrador rejeitar a reserva (fluxo de rejeição já existente) ou o morador optar por cancelá-la,
  **Então** o sistema deve interromper o fluxo, marcar a reserva como rejeitada/cancelada e liberar a data para novas solicitações conforme RN01/RN02.

- **CA11: Visualização da lista de convidados pelo funcionário (RN04)**
  **Dado** que uma reserva possui lista de convidados cadastrada,
  **Quando** um funcionário consultar a reserva no dia do evento,
  **Então** o sistema deve exibir a lista completa de nomes cadastrados pelo morador.

## Notas Técnicas e de Arquitetura (Para a equipe de Desenvolvimento)

- **Evolução da Máquina de Estados:** os 4 status atuais (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`) não são suficientes para representar o novo fluxo. Sugestão de novos status a incluir no enum de reserva (ou avaliar se vale a pena um enum próprio de "etapa do fluxo" separado do status geral):
    - `PENDING` (aguardando aprovação do admin) — já existe
    - `AWAITING_INSPECTION` (aprovada pelo admin, aguardando vistoria do funcionário) — novo
    - `AWAITING_SIGNATURE` (vistoria concluída, aguardando assinatura do termo pelo morador) — novo
    - `CONFIRMED` (reserva confirmada — resultado final para Campo de Futebol após aprovação, ou para Salão/Churrasqueira após assinatura) — novo, ou reaproveitar `APPROVED` como estado final para Campo de Futebol e criar `CONFIRMED` só para os que passam por vistoria (a equipe de arquitetura deve avaliar qual abordagem gera menos ambiguidade)
    - `REJECTED` — já existe
    - `CANCELLED` — já existe

- **Validação de Exclusividade Diária (RN01/RN02):** a validação de disponibilidade de data precisa deixar de ser feita apenas dentro do escopo do próprio espaço e passar a considerar os três espaços (Salão de Festas, Churrasqueira, Campo de Futebol) como um conjunto, ao consultar conflitos de data — comparando o `resident`/morador solicitante da nova reserva contra os moradores das reservas ativas já existentes na mesma data entre esses três espaços.

- **Nova Entidade de Domínio — Vistoria (Inspection):** sugestão de entidade `SpaceInspection`, contendo:
    - `id` (UUID)
    - `reservation` (referência à reserva)
    - `employee` (referência ao funcionário que realizou a vistoria)
    - `type` (Enum: `PRE_EVENT`, `POST_EVENT` — para diferenciar a vistoria pré e pós-evento, ambas usando a mesma estrutura de dados)
    - `notes`/`observations` (String, opcional — observações gerais do funcionário sobre a vistoria)
    - `items` (lista/coleção de itens vistoriados, cada um com identificação do equipamento, confirmação de estado e sua própria foto individual — ex: `SpaceInspectionItem` com `equipmentName`, `okConfirmed` e `photoUrl`)
    - `inspectedAt` (`LocalDateTime`)
    - Observação: como cada item exige sua própria foto, a foto deve ficar associada ao item (`SpaceInspectionItem.photoUrl`) e não à vistoria como um todo.

- **Agendamento da Vistoria Pós-evento:** avaliar se o disparo da notificação para o funcionário no dia seguinte ao evento será feito via job agendado (scheduler) que varre reservas confirmadas cuja data de uso já passou, ou outro mecanismo equivalente já usado no projeto para tarefas futuras.

- **Controle de Presença de Convidados:** ao incluir o campo `guestList` na reserva, cada convidado deve ser modelado como uma entidade própria (ex: `Guest`) — não apenas uma string solta — para suportar o estado de presença:
    - `name` (nome completo)
    - `checkedIn` (boolean, default `false`)
    - `checkedInAt` (`LocalDateTime`, nullable)
    - A operação de marcação deve validar no backend que a data atual corresponde à data do evento, e deve ser idempotente/bloqueada uma vez que `checkedIn` já esteja `true` (não permitir reversão).

- **Catálogo de Equipamentos por Espaço:** os equipamentos por espaço (listados na CA05) podem ser mantidos de forma estática no backend inicialmente (ex: configuração/enum por `SpaceType`), sem necessidade de CRUD de equipamentos nesta fase.

- **Nova Entidade de Domínio — Termo de Responsabilidade (LiabilityTerm/UsageAgreement):** sugestão de entidade contendo:
    - `id` (UUID)
    - `reservation` (referência à reserva)
    - `content` (texto do termo — parágrafo genérico fixo por ora)
    - `signedByResident` (boolean/confirmação)
    - `signedAt` (`LocalDateTime`)

- **Alteração na Entidade de Reserva Existente:** incluir os novos campos:
    - `notes`/`observations` (String, opcional)
    - `guestList` (coleção de nomes completos — ex: lista de strings ou entidade `Guest` simples vinculada à reserva)

- **Notificações:** reaproveitar o mecanismo de notificação já existente no sistema, estendendo-o para os novos destinatários/eventos:
    - Notificação ao funcionário (`EMPLOYEE_ROLE`) quando a reserva entrar em `AWAITING_INSPECTION`.
    - Notificação ao morador quando a reserva entrar em `AWAITING_SIGNATURE` (termo disponível para assinatura).
    - Notificação ao morador na confirmação final (já existe para aprovação/rejeição — estender para o novo status final `CONFIRMED`).

- **Camada Web (Endpoints Sugeridos):**
    - Ajustar `POST` de solicitação de reserva existente para aceitar `notes` e `guestList`.
    - `POST /lunaLink/reservations/{id}/inspection?type=PRE_EVENT` (Acesso: `EMPLOYEE_ROLE`) — submissão do formulário de vistoria pré-evento, com upload de uma foto por item.
    - `POST /lunaLink/reservations/{id}/inspection?type=POST_EVENT` (Acesso: `EMPLOYEE_ROLE`) — submissão do formulário de vistoria pós-evento, mesma estrutura da pré-evento.
    - `POST /lunaLink/reservations/{id}/liability-term/sign` (Acesso: `RESIDENT_ROLE`, restrito ao morador dono da reserva) — assinatura/confirmação do termo.
    - `GET /lunaLink/reservations/{id}/guests` (Acesso: `EMPLOYEE_ROLE`/`ADMIN_ROLE`) — consulta da lista de convidados.
    - `PATCH /lunaLink/reservations/{id}/guests/{guestId}/check-in` (Acesso: `EMPLOYEE_ROLE`) — marca o convidado como "entrou"; deve validar que a data atual é a data do evento e que o convidado ainda não foi marcado.

- **Escopo em aberto:** o conteúdo definitivo do Termo de Responsabilidade ainda será fornecido pelo cliente; para esta implementação, usar um parágrafo genérico placeholder configurável.