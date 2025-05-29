from typing import List
from botbuilder.core import (
    ActivityHandler,
    TurnContext,
    ConversationState,
    UserState,
    MessageFactory,
)
from botbuilder.schema import (
    ChannelAccount,
    SuggestedActions,
    CardAction,
    ActionTypes,
)

from dialogs.main_dialog import MainDialog


class DialogBot(ActivityHandler):

    def __init__(
        self,
        conversation_state: ConversationState,
        user_state: UserState,
        dialog: MainDialog,
    ):
       
        if conversation_state is None:
            raise Exception(
                "[EcommerceBot]: Missing parameter. conversation_state is required"
            )
        if user_state is None:
            raise Exception("[EcommerceBot]: Missing parameter. user_state is required")
        if dialog is None:
            raise Exception("[EcommerceBot]: Missing parameter. dialog is required")

        self.conversation_state = conversation_state
        self.user_state = user_state
        self.dialog = dialog

    async def on_message_activity(self, turn_context: TurnContext):
       
        await self.dialog.run(
            turn_context, self.conversation_state.create_property("DialogState")
        )

        await self.conversation_state.save_changes(turn_context, False)
        await self.user_state.save_changes(turn_context, False)

    async def on_members_added_activity(
        self,
        members_added: List[ChannelAccount],
        turn_context: TurnContext
    ):
       
        for member in members_added:
            if member.id != turn_context.activity.recipient.id:
                await self._send_welcome_message(turn_context)

    async def _send_welcome_message(self, turn_context: TurnContext):
      
        welcome_text = (
            "🛍️ **Bem-vindo ao E-commerce Bot!**\n\n"
            "Eu posso ajudar você com:\n"
            "• **Consultar Pedidos** - Verificar status dos seus pedidos\n"
            "• **Buscar Produtos** - Encontrar produtos por nome ou categoria\n"
            "• **Ver Extrato** - Histórico de compras realizadas\n"
            "• **Fazer Compras** - Adicionar itens ao carrinho e finalizar pedido\n\n"
            "Para começar, escolha uma das opções abaixo ou digite sua solicitação:"
        )

        suggested_actions = SuggestedActions(
            actions=[
                CardAction(
                    title="📦 Meus Pedidos",
                    type=ActionTypes.im_back,
                    value="consultar pedidos"
                ),
                CardAction(
                    title="🔍 Buscar Produtos",
                    type=ActionTypes.im_back,
                    value="buscar produtos"
                ),
                CardAction(
                    title="📊 Meu Extrato",
                    type=ActionTypes.im_back,
                    value="ver extrato"
                ),
                CardAction(
                    title="🛒 Fazer Compra",
                    type=ActionTypes.im_back,
                    value="fazer compra"
                ),
            ]
        )

        welcome_message = MessageFactory.text(welcome_text)
        welcome_message.suggested_actions = suggested_actions

        await turn_context.send_activity(welcome_message)