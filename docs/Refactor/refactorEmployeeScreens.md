# Contexto: Telas de Funcionário (EMPLOYEE) para Vistorias e Listas de Convidados

## Objetivo

Implementar as telas necessárias para que usuários com perfil `EMPLOYEE_ROLE` consigam executar as responsabilidades definidas na **User Story 5 (Evolução do Fluxo de Reserva de Espaços Comuns)**: realizar as vistorias de reservas (pré-evento e pós-evento) e gerenciar a lista de convidados no dia do evento.

## Situação Atual

- O componente de reservas já existe no frontend (Angular PWA), mas hoje é acessado apenas por moradores (`RESIDENT_ROLE`) e administradores (`ADMIN_ROLE`).
- O funcionário (`EMPLOYEE_ROLE`) **não possui acesso** a nenhuma tela relacionada a reservas atualmente.
- Com a US-05, o funcionário passa a ter responsabilidades diretas no fluxo de reserva (vistoria pré-evento, vistoria pós-evento e controle de entrada de convidados), mas seu acesso deve ser **restrito a essas ações específicas** — ele não deve ter acesso à criação, aprovação/rejeição ou visualização geral de reservas como um todo, apenas às reservas que estão nas etapas relevantes ao seu papel.

## O que precisa ser decidido/avaliado

Não está definido ainda se o ideal é **reaproveitar o componente de reservas existente** (adicionando views/rotas condicionadas por role) ou **criar um componente específico** dedicado às telas de funcionário. Pontos a considerar na avaliação:

- O componente atual foi pensado para o fluxo de morador/admin (solicitação, aprovação/rejeição) — verificar o quanto de estrutura (rotas, serviços, models) seria realmente reaproveitável para as telas de vistoria e convidados, que têm propósito e dados diferentes.
- Reaproveitar pode economizar código de integração com a API de reservas já existente (ex: serviços Angular que já buscam dados da reserva), mas corre o risco de acoplar demais uma tela de propósito distinto (vistoria/convidados) a um componente pensado para outro fluxo (solicitação/aprovação).
- Criar um componente específico dá mais isolamento e clareza de responsabilidade (ex: `EmployeeInspectionModule` ou similar), facilitando o controle de acesso por role sem precisar esconder/mostrar partes de um componente compartilhado.
- Ao decidir, considerar o padrão arquitetural já usado no restante do frontend do projeto para separação de telas por role (se já existe uma convenção estabelecida).

## Telas Necessárias para o Funcionário (EMPLOYEE)

### 1. Lista de Reservas Pendentes de Vistoria

- Deve listar as reservas atualmente no status `AWAITING_INSPECTION` (vistoria pré-evento pendente) e as reservas cuja vistoria pós-evento está pendente (dia seguinte ao uso do espaço).
- Cada item da lista deve indicar claramente o espaço (Salão de Festas ou Churrasqueira — Campo de Futebol não entra nessa tela, pois não passa por vistoria), a data do evento e o tipo de vistoria pendente (pré ou pós-evento).

### 2. Formulário de Vistoria (Pré-evento e Pós-evento)

- Tela para o funcionário preencher a vistoria de uma reserva específica.
- Deve listar todos os equipamentos do espaço correspondente (conforme catálogo definido na US-05: Churrasqueira ou Salão de Festas), permitindo que o funcionário confirme o estado de cada item **individualmente**.
- Cada item deve ter seu próprio campo de upload de foto (uma foto por item, não uma foto genérica para todos).
- Deve conter um campo de observações gerais (texto livre, opcional).
- Deve bloquear o envio caso algum item não tenha sido confirmado ou esteja sem a foto obrigatória.
- A mesma estrutura de tela deve servir tanto para vistoria pré-evento quanto pós-evento (diferenciando pelo tipo, mas reaproveitando o layout/formulário).

### 3. Lista de Convidados (Consulta e Check-in)

- Tela para o funcionário consultar a lista de convidados de uma reserva confirmada.
- No dia do evento, cada convidado deve poder ser marcado como "entrou" (check-in) individualmente.
- Fora do dia do evento, a lista deve ser exibida apenas para consulta, sem a opção de marcação.
- Uma vez marcado como "entrou", o convidado não pode ter a marcação desfeita — a interface deve refletir esse estado como definitivo (ex: nome riscado/desabilitado, sem botão de reverter).

## Controle de Acesso

- Todas essas telas devem ser acessíveis exclusivamente por usuários com `EMPLOYEE` e `ADMIN_ROLE`.
- O funcionário não deve enxergar as telas/ações de solicitação de reserva (morador) nem de aprovação/rejeição (administrador).
- Avaliar se o guard de rota (Angular route guard) já existente no projeto para controle de acesso por role pode ser reaproveitado diretamente para essas novas rotas.

## Entregável Esperado do Agente

Uma proposta técnica de implementação cobrindo:
1. Recomendação (com justificativa) sobre reaproveitar o componente de reservas existente ou criar um componente novo dedicado ao funcionário.
2. Estrutura de rotas/telas propostas para as 3 telas descritas acima.
3. Pontos de integração necessários com a API backend (endpoints já definidos na US-05: submissão de vistoria por tipo, assinatura de termo — não aplicável ao funcionário —, consulta de convidados e check-in).
4. Qualquer ajuste necessário no controle de acesso por role do frontend para liberar essas rotas apenas a `EMPLOYEE`.

## Escopo
Essa refatoração diz respeito apenas para as reservas dos espaços SALAO_FESTAS, CAMPO_FUTEBOL, CHURRASQUEIRA.
Os demais espaços/equipamento devem ser mantidos da forma que está, sem novas mudanças.