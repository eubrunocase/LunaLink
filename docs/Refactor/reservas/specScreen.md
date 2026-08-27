# Especificação: Reformulação da Tela de Reservas (Espaços Comuns + Equipamentos)

## Contexto

O aplicativo (Ionic/Angular) possui hoje um fluxo de reservas para dois tipos de recurso:

- **Espaços comuns:** Salão de Festas, Churrasqueira e Campo de Futebol.
- **Equipamento:** Televisão comunitária.

Cada tipo segue um workflow diferente, já definido nas User Stories do projeto:

- **Salão de Festas e Churrasqueira (US-05):** solicitação → aprovação do administrador → vistoria pré-evento pelo funcionário (com foto individual por item + observações) → assinatura do termo de responsabilidade pelo morador → confirmação → uso do espaço → vistoria pós-evento pelo funcionário (dia seguinte).
- **Campo de Futebol (US-05):** solicitação → aprovação do administrador → confirmação direta (sem vistoria, sem termo).
- **Televisão Comunitária (US-04):** solicitação self-service pelo morador → confirmação automática (`CONFIRMED`) → retirada do controle pelo funcionário (`IN_USE`) → devolução (`RETURNED`), sem aprovação do admin.

Hoje, a tela de reservas apresenta dois problemas centrais:

1. **UX/UI insatisfatória:** a interface atual não comunica bem em qual etapa do workflow cada reserva está, nem separa claramente as ações disponíveis por tipo de usuário.
2. **Acoplamento entre espaços comuns e equipamento:** a reserva da TV comunitária está misturada na mesma tela/fluxo visual das reservas de espaço, apesar de ter um workflow e um propósito completamente diferentes.

Esta especificação define o novo desenho de telas, separado por ator e por tipo de recurso, para orientar o desenvolvimento.

## Objetivo desta Entrega

1. Redesenhar a tela de reservas com um layout profissional, claro quanto ao status/etapa de cada reserva.
2. Separar visualmente e estruturalmente a gestão de **reservas de espaço** da gestão de **reserva de equipamento (TV)**, mantendo o mesmo padrão visual/design system entre as duas.
3. Definir exatamente quais telas cada ator (Morador, Funcionário, Administrador) precisa, considerando seu papel em cada workflow.
4. Servir de base para, na sequência, revisar as queries dos repositórios JPA/Hibernate que hoje alimentam essas telas e estão causando erros no cliente Angular (ver seção final).

## Referências de Layout/UX

Para o padrão visual, recomenda-se se inspirar em interfaces de apps de reserva/agendamento com forte hierarquia visual de status, como:

- **Calendly / Cal.com** — pela forma clara de exibir horários disponíveis e status de agendamento.
- **Booking.com (app mobile)** — pelo padrão de cards de reserva com status colorido, timeline de etapas e detalhes expansíveis.
- **Google Calendar (mobile)** — pela visualização de agenda/calendário combinada com lista, útil para o morador visualizar disponibilidade antes de solicitar.
- **Apps de delivery (iFood/Uber Eats) na tela de "acompanhar pedido"** — como referência para o **componente de "timeline de status"** (ex: Solicitado → Aprovado → Vistoria → Assinatura → Confirmado), aplicável diretamente ao workflow de Salão/Churrasqueira.

Princípios de UX a seguir:

- **Status sempre visível:** cada reserva deve exibir um chip/badge de status com cor semântica (ex: amarelo = pendente/aguardando ação, azul = em andamento, verde = confirmado, vermelho = rejeitado/cancelado).
- **Timeline de etapas para Salão/Churrasqueira:** como o fluxo tem múltiplas etapas sequenciais, uma reserva desse tipo deve exibir visualmente em qual etapa está (não apenas um status isolado), para que morador, funcionário e admin entendam rapidamente o que falta.
- **Ações contextuais:** botões de ação devem aparecer apenas quando fazem sentido para o ator logado e para o status atual da reserva (ex: funcionário só vê botão de "Iniciar vistoria" quando a reserva está `AWAITING_INSPECTION`).
- **Consistência entre Espaços e Equipamento:** mesmo sendo telas/rotas separadas, ambas devem usar os mesmos componentes base (cards, chips de status, botões) para manter unidade visual no app.

## Estrutura Geral de Navegação

Separar em dois módulos/rotas de alto nível:

```
/reservas/espacos        → Reservas de Salão de Festas, Churrasqueira e Campo de Futebol
/reservas/equipamentos   → Reserva da Televisão Comunitária
```

Cada módulo tem sua própria listagem, criação e detalhamento — reaproveitando componentes visuais compartilhados, mas com models/serviços próprios (evitando o acoplamento atual).

---

## Telas por Ator

### 🧑‍🤝‍🧑 Morador (`RESIDENT_ROLE`)

#### Tela 1 — Listagem de Reservas de Espaços (`/reservas/espacos`)
- Lista as reservas do próprio morador (passadas e futuras).
- **Filtros:** por data (período) e por espaço (Salão, Churrasqueira, Campo de Futebol).
- Cada item em formato de card, exibindo: espaço, data, status/etapa atual (badge), e um resumo (ex: "Aguardando vistoria").
- Botão de ação flutuante (FAB) ou botão fixo no topo: **"Nova Reserva"**.
- Toque no card abre a **Tela 3 (Detalhe da Reserva)**.

#### Tela 2 — Nova Solicitação de Reserva de Espaço
- Formulário com: seleção do espaço, data desejada (com indicação visual de disponibilidade, respeitando a regra de exclusividade diária da US-05), campo de observações (texto livre) e lista de convidados (input dinâmico para adicionar nomes completos).
- Validação em tempo real de conflito de data (feedback claro caso a data já esteja ocupada por outro morador).
- Botão de confirmação de envio, com tela de sucesso/confirmação de solicitação enviada.

#### Tela 3 — Detalhe da Reserva de Espaço (modal ou tela menor, conforme pedido)
- Aberta a partir do card na listagem.
- Exibe: espaço, data, observações, lista de convidados cadastrada, e a **timeline de status** completa (Solicitado → Aprovação Admin → Vistoria Pré-evento → Assinatura do Termo → Confirmado → Vistoria Pós-evento, quando aplicável; para Campo de Futebol, a timeline é mais curta: Solicitado → Aprovação → Confirmado).
- Quando a reserva estiver na etapa `AWAITING_SIGNATURE`, exibir nesta mesma tela (ou em tela decorrente) o **Termo de Responsabilidade** para leitura e botão de assinatura/confirmação.
- Botão de cancelamento da reserva, quando aplicável ao status atual.

#### Tela 4 — Listagem de Reserva de Equipamento (`/reservas/equipamentos`)
- Lista as reservas de TV do próprio morador.
- Cada card exibe: data/horário, status (`CONFIRMED`, `IN_USE`, `RETURNED`).
- Botão de nova reserva.

#### Tela 5 — Nova Solicitação de Reserva de Equipamento
- Formulário simples: data/horário desejado.
- Validação de conflito (409) exibida de forma clara caso o horário já esteja ocupado.
- Confirmação imediata (self-service, sem aprovação), com feedback visual de "Reserva confirmada".

---

### 👷 Funcionário (`EMPLOYEE_ROLE`)

> O funcionário não deve ver as telas de solicitação de reserva. Seu acesso é focado nos pontos de ação do workflow.

#### Tela 6 — Listagem Geral de Reservas de Espaços (todas, não só as do funcionário)
- Lista **todas** as reservas de Salão, Churrasqueira e Campo de Futebol (não filtradas por morador).
- Filtros por data e por espaço.
- Indicação visual de quais reservas exigem ação do funcionário no momento (ex: destaque ou seção separada "Aguardando sua ação": vistorias pré-evento e pós-evento pendentes).
- Campo de Futebol aparece na listagem apenas para consulta (não gera ação de vistoria).

#### Tela 7 — Formulário de Vistoria (Pré-evento / Pós-evento)
- Acessado a partir de uma reserva com vistoria pendente.
- Lista os equipamentos do espaço (conforme catálogo da US-05), cada um com:
    - Toggle/checkbox de confirmação de estado.
    - Campo de upload de foto individual (obrigatório).
- Campo de observações gerais (opcional).
- Botão de envio, desabilitado até todos os itens estarem completos.
- Mesma tela reaproveitada para pré e pós-evento, diferenciando apenas o título/contexto exibido.

#### Tela 8 — Lista de Convidados (Consulta e Check-in)
- Acessada a partir de uma reserva confirmada de Salão ou Churrasqueira.
- Lista os convidados cadastrados pelo morador.
- No dia do evento: cada convidado tem um botão/gesto de "marcar entrada" (ex: swipe ou botão de check), que ao ser acionado risca o nome e desabilita a ação (irreversível).
- Fora do dia do evento: lista somente leitura, sem os controles de check-in visíveis/habilitados.

#### Tela 9 — Gestão da Reserva de Equipamento (Entrega/Devolução)
- Lista as reservas de TV do dia (ou próximas), permitindo:
    - Marcar retirada do controle (`IN_USE`) quando o morador comparecer.
    - Marcar devolução (`RETURNED`) quando o morador devolver.

---

### 🛡️ Administrador (`ADMIN_ROLE`)

- Acesso irrestrito a todas as telas acima (tanto as de morador quanto as de funcionário), sem necessidade de telas exclusivas adicionais — a especificação recomenda que o admin utilize os mesmos componentes, com todas as ações liberadas independentemente do "dono" da reserva ou da etapa.
- Adicionalmente, mantém a tela já existente de **aprovação/rejeição de reservas** (fluxo já implementado), que deve ser revisada apenas para se integrar visualmente ao novo padrão de cards/status/timeline definido aqui — sem necessidade de redesenho funcional dessa etapa específica.

---

## Regras de Acesso (Resumo)

| Tela | Morador | Funcionário | Admin |
|---|---|---|---|
| Nova solicitação (espaço/equipamento) | ✅ | ❌ | ✅ |
| Listagem das próprias reservas | ✅ | — | ✅ |
| Listagem geral de todas as reservas | ❌ | ✅ | ✅ |
| Detalhe da reserva | ✅ (próprias) | ✅ (consulta) | ✅ |
| Formulário de vistoria (pré/pós) | ❌ | ✅ | ✅ |
| Assinatura do termo de responsabilidade | ✅ (próprias) | ❌ | ✅ |
| Lista de convidados (consulta) | ✅ (próprias) | ✅ | ✅ |
| Check-in de convidados | ❌ | ✅ (no dia) | ✅ |
| Entrega/devolução de equipamento | ❌ | ✅ | ✅ |
| Aprovação/rejeição de reserva | ❌ | ❌ | ✅ |

---

## Componentes Visuais Reutilizáveis (Sugestão)

- `ReservationStatusChip` — badge colorido por status.
- `ReservationTimeline` — componente de etapas (usado apenas nas reservas de Salão/Churrasqueira, que têm fluxo multi-etapa).
- `ReservationCard` — card padrão de listagem, reaproveitado tanto em espaços quanto (com variação mínima) em equipamento.
- `InspectionItemForm` — item individual do formulário de vistoria (checkbox + upload de foto), reaproveitado entre pré e pós-evento.
- `GuestListItem` — item da lista de convidados, com variação visual para estado "riscado".

---

## Observação Importante para o Agente: Ordem de Execução

Esboçar e validar as telas descritas acima **antes** de qualquer ajuste no backend. O motivo: atualmente as consultas dos repositórios JPA/Hibernate que alimentam essas telas estão gerando erros no cliente Angular (a origem exata ainda não foi diagnosticada — pode ser mapeamento de relacionamento, paginação, ou dados retornados em formato incompatível com o esperado pelo frontend). Somente depois de definidas as telas finais — e, portanto, quais dados cada uma efetivamente precisa consumir (campos, filtros, agrupamentos) — o agente deve:

1. Mapear quais endpoints/queries alimentam cada tela definida nesta especificação.
2. Revisar os métodos dos repositórios JPA envolvidos (ex: métodos de busca por morador, por espaço, por status, por período) e verificar se as queries retornam exatamente o formato/estrutura que o Angular espera.
3. Corrigir as queries/DTOs conforme necessário, evitando quebrar contratos já usados por outras partes do sistema.

Esse diagnóstico e correção ficam para uma etapa posterior a esta especificação de tela — não devem ser resolvidos antes de as telas estarem esboçadas.