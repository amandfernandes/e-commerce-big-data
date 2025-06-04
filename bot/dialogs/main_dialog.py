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
from botbuilder.schema import SuggestedActions, CardAction, ActionTypes

from .pedido_dialog import PedidoDialog
from .produto_dialog import ProdutoDialog
from .extrato_dialog import ExtratoDialog
from .compra_dialog import CompraDialog

class MainDialog(ComponentDialog):

    def __init__(self, user_state: UserState):
        super(MainDialog, self).__init__(MainDialog.__name__)

        self.user_state = user_state

        self.add_dialog(TextPrompt(TextPrompt.__name__))
        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))

        self.add_dialog(PedidoDialog())
        self.add_dialog(ProdutoDialog(user_state))
        self.add_dialog(ExtratoDialog())
        self.add_dialog(CompraDialog(user_state))

        self.add_dialog(
            WaterfallDialog(
                WaterfallDialog.__name__,
                [
                    self.intro_step,
                    self.act_step,
                    self.final_step,
                ],
            )
        )

        self.initial_dialog_id = WaterfallDialog.__name__

    async def intro_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:

        message_text = step_context.options or step_context.context.activity.text

        if message_text:
            intent = self._get_intent(message_text.lower())
            
            if intent != "unknown":
                return await step_context.next(intent)

        return await self._show_main_menu(step_context)

    async def act_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        
        choice = step_context.result

        if choice == "pedidos":
            return await step_context.begin_dialog(PedidoDialog.__name__)
        elif choice == "produtos":
            return await step_context.begin_dialog(ProdutoDialog.__name__)
        elif choice == "extrato":
            return await step_context.begin_dialog(ExtratoDialog.__name__)
        elif choice == "compra":
            return await step_context.begin_dialog(CompraDialog.__name__)
        elif choice == "sair":
            await step_context.context.send_activity(
                MessageFactory.text("👋 Obrigado por usar nosso chatbot! Até logo!")
            )
            return await step_context.end_dialog()
        else:
            await step_context.context.send_activity(
                MessageFactory.text("❌ Desculpe, não entendi sua solicitação.")
            )
            return await step_context.replace_dialog(self.id)

    async def final_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        return await self._show_continue_menu(step_context)

    async def _show_main_menu(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        prompt_message = (
            "🛍️ **O que você gostaria de fazer?**\n\n"
            "Escolha uma das opções abaixo:"
        )

        choices = [
            Choice("📦 Consultar Pedidos", "pedidos"),
            Choice("🔍 Buscar Produtos", "produtos"),
            Choice("📊 Ver Extrato", "extrato"),
            Choice("🛒 Fazer Compra", "compra"),
            Choice("❌ Sair", "sair"),
        ]

        return await step_context.prompt(
            ChoicePrompt.__name__,
            PromptOptions(
                prompt=MessageFactory.text(prompt_message),
                choices=choices,
            ),
        )


    async def _show_continue_menu(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        prompt_message = "✅ Posso ajudar com mais alguma coisa?"

        choices = [
            Choice("🔄 Voltar ao Menu", "menu"),
            Choice("❌ Encerrar", "sair"),
        ]

        return await step_context.prompt(
            ChoicePrompt.__name__,
            PromptOptions(
                prompt=MessageFactory.text(prompt_message),
                choices=choices,
            ),
        )

    def _get_intent(self, text: str) -> str:
       
        if any(word in text for word in ["pedido", "consultar pedidos", "pedidos", "status", "entrega", "rastreamento"]):
            return "pedidos"
        
        elif any(word in text for word in ["produto", "buscar produtos", "produtos", "buscar", "procurar", "categoria", "preço"]):
            return "produtos"
        
        elif any(word in text for word in ["extrato", "ver extrato", "histórico", "compras", "gastos", "transações"]):
            return "extrato"

        elif any(word in text for word in ["comprar", "fazer compra", "carrinho", "adicionar", "finalizar", "pagamento"]):
            return "compra"
        
        elif any(word in text for word in ["sair", "tchau", "bye", "encerrar", "parar"]):
            return "sair"
        
        return "unknown"
    
    async def run(self, turn_context: TurnContext, dialog_state_accessor):
        dialog_set = DialogSet(dialog_state_accessor)
        dialog_set.add(self)

        dialog_context = await dialog_set.create_context(turn_context)
        results = await dialog_context.continue_dialog()

        if results.status == DialogTurnStatus.Empty:
            return await dialog_context.begin_dialog(self.id)
        
        return results