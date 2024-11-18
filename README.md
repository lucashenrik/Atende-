# Atende Mais

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-00599C?style=for-the-badge&logo=websocket&logoColor=white)

## **Automatizando a comunicação entre pontos de vendas(PDV) e produção**

O **Atende Mais** é uma aplicação back-end desenvolvida com **Java** e **Spring Boot**, projetada para agilizar a comunicação entre pontos de venda e a área de produção. Ele se integra a sistemas de pagamento e utiliza **WebSocket** para comunicação em tempo real, garantindo que vendas sejam notificadas diretamente à produção, sem intermediários.

---

## 🏆 **Destaques do Projeto**

- **Integração por Webhooks**: Notifica automaticamente a área de produção após pagamentos confirmados.
- **Comunicação via WebSocket**: Atualizações em tempo real para os operadores.
- **Gerenciamento inteligente de prefixos**: Define quais itens devem ser monitorados, garantindo precisão e personalização.
- **Controle de pedidos**:
  - Acompanhamento do status (`produção`, `pronto`, `entregue`, `cancelado`).
  - Atualizações dinâmicas através da interface web conectada ao back-end.
- **Solução escalável**: Projetada para atender diferentes tipos de negócios.

---

## 🌐 **Arquitetura e Tecnologias**

- **Back-end**: Java com Spring Boot
- **Comunicação em Tempo Real**: WebSocket
- **Integração**: APIs do PagBank (notificações de pagamento)
- **Persistência**: Dados armazenados em JSON simplificado para agilidade

⚠️ *O front-end, desenvolvido em React, não está incluído neste repositório.*

---

## 📋 **Fluxo de Funcionamento**

1. **Venda realizada**:
   - A aplicação recebe notificações via webhook ou via máquina de pagamento conectada ao computador.
2. **Processamento do pedido**:
   - Os dados são verificados e registrados no sistema, conforme prefixos definidos.
3. **Notificação em tempo real**:
   - O back-end notifica o front-end usando WebSocket para atualização imediata.
4. **Acompanhamento do pedido**:
   - O usuário acompanha e gerencia os pedidos diretamente na interface web.

---

## 📖 **Documentação Completa**

Para detalhes sobre os endpoints e funcionamento da API, acesse a [Documentação do Postman](https://atende-mais.postman.co/workspace/Atende-Mais~10ec1013-9b41-46d5-9409-227f0443be22/collection/34479954-e3a8e11d-df56-4cdc-834c-153f3f05d246).

---

## 🧑‍💻 **Sobre o Desenvolvedor**

Este projeto foi idealizado e desenvolvido por mim, com foco em resolver problemas reais enfrentados por operações comerciais. Ele reflete minha experiência em **desenvolvimento back-end**, integrando APIs, criando sistemas escaláveis e implementando comunicação em tempo real.

> Caso tenha interesse em discutir mais sobre o projeto ou conhecer meu trabalho, sinta-se à vontade para entrar em contato.

---

## 📫 **Contato**

- Email: [@lucashenrik.com](lucashenrikjj@gmail.com)
---
