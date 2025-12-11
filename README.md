⚔️ RhaegarClans: Sistema de Clãs Avançado, Minecraft 1.21.10 ⚔️
Versão atual: 1.0.1

- O RhaegarClans é um plugin de clãs robusto e completo, desenvolvido para servidores com foco em competição e diplomacia, como Factions e Survival PvP.
- Ele oferece uma arquitetura de relações diplomáticas detalhada e integração perfeita com o plugin TAB/PlaceholderAPI para uma experiência visual dinâmica.

✨ Funcionalidades
1. Diplomacia de Três Vias: Gerenciamento de Alianças, Tratados de Paz (TNA) e Guerras.

2. Hierarquia de Cargos: Funções claras de Dono, Líder e Membro.

3. Chats Exclusivos: Chat de Clã (/.) e Chat de Aliança (/ally).

4. Tags Dinâmicas: Cores de tags que mudam no TAB (Tabela de Jogadores) de acordo com a relação diplomática.

5. Arquitetura Limpa: Código organizado em pacotes modulares (core, command, chat, integration) para fácil manutenção.

👑 Hierarquia e Permissões
A estrutura de cargos do clã determina quem pode executar comandos de gestão e diplomacia:

Cargo	Comandos de Gestão (Exemplos)
[~Dono]	Controle Total. Pode promover/rebaixar Líderes, transferir a posse e dissolver o clã.
[~Líder]	Gerenciamento de Membros (/clan invite, /clan kick, /clan promote/demote) e Diplomacia (/clan ally, /clan peace, /clan war).
[~Membro]	Comandos de Chat (/., /ally) e comandos pessoais (/clan leave, /clan profile).

📜 Comandos do Jogador (/clan [subcomando])
Básicos e Gestão Interna

/clan create <tag> <nome>	Cria um novo clã. (Ex: /clan create RGR Rhaegar)
/clan invite <jogador>	Convida um jogador para o clã.
/clan accept	Aceita um convite de clã pendente.
/clan kick <jogador>	Expulsa um membro do clã.
/clan leave confirm	Sai do seu clã.
/clan profile [tag]	Vê seu perfil ou o de outro clã.

Chats Exclusivos

/. <mensagem>	   ~ Envia uma mensagem apenas para os membros online do seu clã.
/ally <mensagem> ~ Envia uma mensagem para os membros online do seu clã e de todos os clãs aliados.

🤝 Sistema de Diplomacia Avançada
O sistema de diplomacia é o cerne do RhaegarClans e requer permissões de Líder ou Dono para ser gerenciado

1. Aliança	/clan ally [start/accept/end] <tag>	Relação mútua. Permite o Chat de Aliança. Quebra Guerra/TNA.

2. Tratado de Paz (TNA)	/clan peace [start/accept/end] <tag>	Relação mútua de Não-Agressão.

3. Guerra	/clan war [start/end] <tag>	Relação unilateral. Declaração de hostilidade imediata. Quebra Aliança/TNA.

🎨 Integração com PlaceholderAPI (TAB)
O RhaegarClans implementa a interface Relational do PlaceholderAPI, permitindo que as tags de clãs mudem de cor dinamicamente na Tabela de Jogadores (TAB) e em outros locais, com base na sua relação diplomática com o alvo.

%rhaegarclans_clan_tag%	Retorna a tag do seu clã (Ex: [RGR]).	Nickname e Chat.

%rhaegarclans_relation_color_<player>%	Retorna o código de cor da relação entre você e o jogador <player>.	Essencial para o TAB!

%rhaegarclans_relation_type_<player>%	Retorna o tipo de relação textual (Ex: Aliado, Guerra, Neutro).	Scoreboards ou Mensagens.


Plugins recomendados para se utilizar com o RhaegarClans: LuckPerms, TAB, PlaceHolderAPI, LPC, DecentHolograms
Para utilizar o Plugin, basta baixar o JAR



