from botbuilder.dialogs import (
    ComponentDialog,
    WaterfallDialog,
    WaterfallStepContext,
    DialogTurnResult,
    ChoicePrompt,
    TextPrompt,
    PromptOptions
)
from botbuilder.dialogs.choices import Choice
from botbuilder.core import MessageFactory, UserState
from botbuilder.schema import ActionTypes, CardAction, SuggestedActions
from api.cartao_api import CartaoAPI

class ExtratoDialog(ComponentDialog):
    def __init__(self, user_state: UserState):
        super(ExtratoDialog, self).__init__(ExtratoDialog.__name__)
        
        self.cartao_api = CartaoAPI()
        self.user_state = user_state
        
        self.add_dialog(TextPrompt(TextPrompt.__name__))
        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))
        self.add_dialog(
            WaterfallDialog(
                "ExtratoWaterfall",
                [
                    self.validate_user_step,  # Novo passo
                    self.select_card_step,
                    self.show_extrato_step,
                    self.final_step
                ]
            )
        )
        
        self.initial_dialog_id = "ExtratoWaterfall"

    async def validate_user_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        """
        Solicita e valida o ID do usuário
        """
        return await step_context.prompt(
            TextPrompt.__name__,
            PromptOptions(
                prompt=MessageFactory.text("👤 Por favor, digite seu ID de usuário:")
            ),
        )

    async def select_card_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        # Validar ID do usuário
        try:
            user_id = int(step_context.result)
            # Verifica se o usuário existe
            if not self.cartao_api.user_exists(user_id):
                await step_context.context.send_activity("❌ Usuário não encontrado.")
                return await step_context.end_dialog()
            
            step_context.values["user_id"] = user_id
            
            # Busca cartões do usuário
            cards = self.cartao_api.get_user_cards(user_id)
            
            if not cards:
                await step_context.context.send_activity("Você não possui cartões cadastrados.")
                return await step_context.end_dialog()

            # Guarda dados para próximo step
            step_context.values["user_id"] = user_id
            step_context.values["cards"] = cards

            # Se só tem um cartão, vai direto pro próximo passo
            if len(cards) == 1:
                return await step_context.next(cards[0]["id"])

            # Se tem mais cartões, mostra opções para escolha
            card_choices = [
                Choice(value=str(card["id"]), 
                      text=f"Cartão final {card['numero'][-4:]}")
                for card in cards
            ]

            return await step_context.prompt(
                ChoicePrompt.__name__,
                PromptOptions(
                    prompt=MessageFactory.text("Qual cartão você quer ver o extrato?"),
                    choices=card_choices
                )
            )
        except ValueError:
            await step_context.context.send_activity("❌ ID de usuário inválido. Digite apenas números.")
            return await step_context.end_dialog()

    async def show_extrato_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        user_id = step_context.values["user_id"]
        
        # Pega o ID do cartão selecionado
        if hasattr(step_context.result, 'value'):
            card_id = step_context.result.value
        else:
            card_id = step_context.result

        # Busca extrato
        extrato = self.cartao_api.get_card_statement(user_id, card_id)
        
        if not extrato:
            await step_context.context.send_activity("Não há transações para este cartão.")
            return await step_context.end_dialog()

        # Monta mensagem do extrato
        mensagem = "📊 **EXTRATO DO CARTÃO**\n\n"
        
        for transacao in extrato:
            data = transacao.get("data", "")[:10]
            valor = transacao.get("valor", 0)
            descricao = transacao.get("descricao", "")
            status = transacao.get("status", "")
            
            mensagem += (
                f"📅 Data: {data}\n"
                f"💰 Valor: R$ {valor:.2f}\n"
                f"📝 Descrição: {descricao}\n"
                f"✔️ Status: {status}\n"
                f"{'─' * 30}\n"
            )

        await step_context.context.send_activity(MessageFactory.text(mensagem))

        return await step_context.next(None)

    async def final_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        reply = MessageFactory.text("O que você deseja fazer agora?")
        reply.suggested_actions = SuggestedActions(
            actions=[
                CardAction(
                    type=ActionTypes.im_back,
                    title="🔄 Voltar ao Menu",
                    value="menu"
                ),
                CardAction(
                    type=ActionTypes.im_back,
                    title="❌ Sair",
                    value="sair"
                )
            ]
        )
        
        await step_context.context.send_activity(reply)
        return await step_context.end_dialog()

