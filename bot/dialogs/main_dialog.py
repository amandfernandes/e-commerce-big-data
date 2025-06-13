from botbuilder.dialogs import (
    ComponentDialog,
    WaterfallDialog,
    WaterfallStepContext,
    DialogTurnResult,
    TextPrompt,
    ChoicePrompt,
    DialogTurnStatus,
    DialogSet, 
    PromptOptions,
)
from botbuilder.dialogs.choices import Choice
from botbuilder.core import MessageFactory, UserState, TurnContext
from botbuilder.schema import CardAction, ActionTypes

from .pedido_dialog import PedidoDialog
from .produto_dialog import ProdutoDialog
from .extrato_dialog import ExtratoDialog
from .compra_dialog import CompraDialog

class MainDialog(ComponentDialog):

    def __init__(self, user_state: UserState):
        super(MainDialog, self).__init__(MainDialog.__name__)

        self.user_state = user_state
        # Criando o accessor para o User ID aqui
        self.user_id_accessor = self.user_state.create_property("UserId")

        self.add_dialog(TextPrompt(TextPrompt.__name__))
        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))

        # Adicionando os diálogos filhos
        self.add_dialog(PedidoDialog())
        self.add_dialog(ProdutoDialog(user_state))
        self.add_dialog(ExtratoDialog(user_state))
        self.add_dialog(CompraDialog(user_state))

        # Waterfall reestruturado com passo de identificação
        self.add_dialog(
            WaterfallDialog(
                "MainWaterfall", 
                [
                    self.identify_user_step,
                    self.intro_step,
                    self.act_step,
                    self.final_step,
                    self.process_continue_step,
                ],
            )
        )

        self.initial_dialog_id = "MainWaterfall"

    # PASSO 1 (NOVO): Verifica se o usuário já foi identificado. Se não, pede o ID.
    async def identify_user_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        # Tenta pegar o ID da memória de longo prazo (UserState)
        user_id = await self.user_id_accessor.get(step_context.context)

        if user_id is None:
            # Se não encontrou, pede para o usuário
            return await step_context.prompt(
                TextPrompt.__name__,
                PromptOptions(prompt=MessageFactory.text("Olá! Para começar, por favor, digite seu ID de usuário:"))
            )
        else:
            # Se já encontrou, pula para o próximo passo passando o ID
            return await step_context.next(user_id)


    # PASSO 2: Salva o ID (se foi pedido) e mostra o menu principal
    async def intro_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        # O resultado do passo anterior (o ID do usuário) chega aqui
        user_id = step_context.result

        # Verifica se o ID já está salvo, para não salvar de novo desnecessariamente
        current_id = await self.user_id_accessor.get(step_context.context)
        if current_id is None:
            try:
                # Salva o ID na memória de longo prazo (UserState)
                await self.user_id_accessor.set(step_context.context, int(user_id))
            except (ValueError, TypeError):
                 await step_context.context.send_activity("ID inválido. Por favor, reinicie a conversa e digite um número.")
                 return await step_context.end_dialog()


        # A lógica de detectar intenção continua aqui, mas a saudação é mais simples
        message_text = step_context.options or "Olá! Bem-vindo ao nosso chatbot."
        if isinstance(message_text, str):
            intent = self._get_intent(message_text.lower())
            if intent != "unknown":
                return await step_context.next({"intent": intent})

        return await self._show_main_menu(step_context)

    # PASSO 3: Lida com a escolha do menu principal
    async def act_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        # (Esta função permanece a mesma da versão anterior)
        if isinstance(step_context.result, dict) and "intent" in step_context.result:
            choice = step_context.result["intent"]
        else:
            choice = step_context.result.value if hasattr(step_context.result, 'value') else step_context.result

        if choice == "pedidos":
            return await step_context.begin_dialog(PedidoDialog.__name__)
        elif choice == "produtos":
            return await step_context.begin_dialog(ProdutoDialog.__name__)
        elif choice == "extrato":
            return await step_context.begin_dialog(ExtratoDialog.__name__)
        elif choice == "compra":
            return await step_context.begin_dialog(CompraDialog.__name__)
        
        await step_context.context.send_activity(
            MessageFactory.text("❌ Desculpe, não entendi sua solicitação.")
        )
        return await step_context.next()

    # PASSO 4: Pergunta se o usuário quer continuar
    async def final_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        # (Esta função permanece a mesma da versão anterior)
        prompt_message = "✅ Posso ajudar com mais alguma coisa?"
        choices = [
            Choice(value="menu", action=CardAction(type=ActionTypes.im_back, title="🔄 Voltar ao Menu", value="menu")),
            Choice(value="sair", action=CardAction(type=ActionTypes.im_back, title="❌ Encerrar", value="sair")),
        ]
        return await step_context.prompt(ChoicePrompt.__name__, PromptOptions(prompt=MessageFactory.text(prompt_message), choices=choices))

    # PASSO 5: Processa a resposta sobre continuar
    async def process_continue_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        # (Esta função permanece a mesma da versão anterior)
        choice = step_context.result.value
        if choice == "menu":
            return await step_context.replace_dialog(self.id, "Olá de novo! Como posso te ajudar?")
        elif choice == "sair":
            await self.user_id_accessor.delete(step_context.context) # Limpa o ID ao sair
            await step_context.context.send_activity(MessageFactory.text("👋 Obrigado por usar nosso chatbot! Até logo!"))
            return await step_context.end_dialog()
        return await step_context.end_dialog()

    # (As funções auxiliares _show_main_menu, _get_intent, e run permanecem as mesmas)
    async def _show_main_menu(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        prompt_message = "🛍️ **O que você gostaria de fazer?**\n\nEscolha uma das opções abaixo:"
        choices = [
            Choice(value="pedidos", action=CardAction(type=ActionTypes.im_back, title="📦 Consultar Pedidos", value="pedidos")),
            Choice(value="produtos", action=CardAction(type=ActionTypes.im_back, title="🔍 Buscar Produtos", value="produtos")),
            Choice(value="extrato", action=CardAction(type=ActionTypes.im_back, title="📊 Ver Extrato", value="extrato")),
            Choice(value="compra", action=CardAction(type=ActionTypes.im_back, title="🛒 Fazer Compra", value="compra")),
        ]
        return await step_context.prompt(ChoicePrompt.__name__, PromptOptions(prompt=MessageFactory.text(prompt_message), choices=choices))
    
    def _get_intent(self, text: str) -> str:
        # (Esta função não foi alterada)
        if any(word in text for word in ["pedido", "consultar pedidos", "pedidos", "status", "entrega", "rastreamento"]): return "pedidos"
        elif any(word in text for word in ["produto", "buscar produtos", "produtos", "buscar", "procurar", "categoria", "preço"]): return "produtos"
        elif any(word in text for word in ["extrato", "ver extrato", "histórico", "compras", "gastos", "transações"]): return "extrato"
        elif any(word in text for word in ["comprar", "fazer compra", "carrinho", "adicionar", "finalizar", "pagamento"]): return "compra"
        elif any(word in text for word in ["sair", "tchau", "bye", "encerrar", "parar"]): return "sair"
        return "unknown"
    
    async def run(self, turn_context: TurnContext, dialog_state_accessor):
        # (Esta função não foi alterada)
        dialog_set = DialogSet(dialog_state_accessor)
        dialog_set.add(self)
        dialog_context = await dialog_set.create_context(turn_context)
        results = await dialog_context.continue_dialog()
        if results.status == DialogTurnStatus.Empty:
            await dialog_context.begin_dialog(self.id)