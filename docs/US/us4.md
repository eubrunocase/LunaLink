# [US-04] Reserva e Gestão de Empréstimo da Televisão Comunitária

## Descrição (Contexto de Negócio)

O condomínio oferece uma televisão para uso comunitário de forma totalmente gratuita. A reserva deste equipamento deve ser ágil e self-service (sem moderação do administrador). O principal desafio operacional não é a cobrança, mas sim a custódia do acessório físico (controle remoto). O sistema precisa garantir que o morador consiga reservar a TV e que a portaria/funcionário consiga rastrear quem está com o controle no momento, confirmando sua retirada e devolução com precisão de horários para fins de auditoria.

## Histórias de Usuário

**Parte 1 (Morador)**
**Como** residente do condomínio (`RESIDENT_ROLE`),
**Eu quero** agendar o uso da televisão comunitária de forma autônoma,
**Para que** minha reserva seja confirmada instantaneamente e eu possa retirar o controle remoto no horário estipulado.

**Parte 2 (Funcionário)**
**Como** funcionário/employee,
**Eu quero** registrar a entrega e a devolução física do controle remoto atrelado à reserva ativa,
**Para que** eu possa controlar o inventário, isentar o condomínio de perdas ou danos e ter o histórico exato de uso.

## Critérios de Aceite

- **CA01: Auto-aprovação e Gratuidade**
  **Dado** que um morador solicita a reserva da televisão para um horário disponível,
  **Quando** o sistema processar o `POST` da requisição,
  **Então** a reserva deve ser salva imediatamente com um status válido (ex: `CONFIRMED`), pulando a etapa de moderação do administrador, e não deve gerar nenhum registro para faturamento futuro.

- **CA02: Registro de Retirada (Check-out do Controle)**
  **Dado** que um morador possui uma reserva aprovada para o momento atual,
  **Quando** ele for à portaria retirar o controle remoto,
  **Então** o funcionário deve conseguir alterar o status desta locação para "Em Uso" (`IN_USE`).

- **CA03: Registro de Devolução (Check-in do Controle)**
  **Dado** que a televisão está com o status "Em Uso",
  **Quando** o morador devolver o controle remoto ao funcionário,
  **Então** o sistema deve permitir que o funcionário marque a reserva como "Concluída/Devolvida" (`RETURNED`), liberando o equipamento para as próximas reservas.

- **CA04: Prevenção de Conflitos**
  **Dado** que já existe uma locação da televisão aprovada ou em uso para um determinado bloco de horário,
  **Quando** outro morador tentar reservar a TV concorrendo com o mesmo período,
  **Então** o sistema deve retornar um erro `409 Conflict` avisando que o equipamento já está alocado.

- **CA05: Auditoria e Rastreabilidade de Horários**
  **Dado** o ciclo de vida da reserva do equipamento,
  **Quando** as ações de criação, retirada e devolução ocorrerem,
  **Então** o sistema deve registrar a data e hora exata de forma automática, garantindo que o histórico exiba quando a reserva foi solicitada, a hora real em que o morador pegou o controle na portaria e a hora exata da devolução.

## Notas Técnicas e de Arquitetura (Para a equipe de Desenvolvimento)

- **Separação de Domínio (Obrigatório):** Para não acoplar a lógica de faturamento do `Reservation` atual, criar um novo agregado/entidade. Sugestões: `Equipment` e `EquipmentReservation`.

- **Máquina de Estados Própria:** A nova entidade deve possuir um Enum de status customizado, como `EquipmentReservationStatus` contendo: `CONFIRMED`, `IN_USE` e `RETURNED`.

- **Novos Atributos de Tempo (Timestamps):** A entidade `EquipmentReservation` deve obrigatoriamente possuir os seguintes campos mapeados como `LocalDateTime`:
    - `createdAt`: Preenchido automaticamente no momento do `POST` pelo morador.
    - `pickedUpAt`: Preenchido automaticamente com `LocalDateTime.now()` quando o funcionário acionar o endpoint de entrega (mudança para `IN_USE`).
    - `returnedAt`: Preenchido automaticamente com `LocalDateTime.now()` quando o funcionário acionar o endpoint de devolução (mudança para `RETURNED`).

- **Camada Web (Endpoints Sugeridos):**
    - `POST /lunaLink/equipment-reservation` (Acesso: Resident)
    - `PATCH /lunaLink/equipment-reservation/{id}/handover` (Acesso: Admin/Funcionário)
    - `PATCH /lunaLink/equipment-reservation/{id}/return` (Acesso: Admin/Funcionário)
- **Separação de telas no frontend**
    -  As telas do frontend devem ser incluidas na interface do usuário de role (EMPLOYEE)
    -  Usuário com role ADMIN_ROLE também pode ter acesso a funcionalidade para fins de gestão
  
  