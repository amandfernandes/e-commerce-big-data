# dialogs/order_dialog.py
# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

from botbuilder.dialogs import (
    ComponentDialog,
    WaterfallDialog,
    WaterfallStepContext,
    DialogTurnResult,
    TextPrompt,
    ChoicePrompt,
)
from botbuilder.dialogs.choices import Choice
from botbuilder.core import MessageFactory

from botbuilder.dialogs.prompts import PromptOptions
from api.compra_api import ComprasAPI  # Corrigido: CompraAPI -> ComprasAPI

class PedidoDialog(ComponentDialog):
    def __init__(self):
        super(PedidoDialog, self).__init__(PedidoDialog.__name__)

        self.add_dialog(TextPrompt(TextPrompt.__name__))
        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))

        self.add_dialog(
            WaterfallDialog(
                WaterfallDialog.__name__,
                [
                    self._list_orders
                ],
            )
        )

        self.initial_dialog_id = WaterfallDialog.__name__


    async def _list_orders(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        compra_api = ComprasAPI()  # Corrigido: CompraAPI -> ComprasAPI

        response = compra_api.get_user_orders()  # Corrigido: get_user_orders() em vez de get_orders()
        orders = response.get("data", [])

        if not orders:
            message = "📭 **Nenhum pedido encontrado!**\n\nVocê ainda não fez nenhum pedido conosco."
        else:
            message = "📦 **Seus Últimos Pedidos:**\n\n"
            
            for order in orders[-5:]:
                message += (
                    f"🏷️ **Pedido #{order['id']}**\n"
                    f"📅 Data: {order['data']}\n"  # era order['date'], ajuste se o campo for 'data'
                    f"💰 Total: R$ {order['total']:.2f}\n"
                    f"📦 Itens: {len(order['itens'])} produto(s)\n\n"  # era order['items']
                )

        await step_context.context.send_activity(MessageFactory.text(message))
        return await step_context.next(None)
