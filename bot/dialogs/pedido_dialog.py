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
from api.compra_api import ComprasAPI

class PedidoDialog(ComponentDialog):
    def __init__(self):
        super(PedidoDialog, self).__init__(PedidoDialog.__name__)

        self.add_dialog(TextPrompt(TextPrompt.__name__))
        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))

        self.add_dialog(
            WaterfallDialog(
                WaterfallDialog.__name__,
                [
                    self.request_user_id_step,
                    self._list_orders
                ],
            )
        )

        self.initial_dialog_id = WaterfallDialog.__name__

    async def request_user_id_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        """
        Primeiro passo: solicita o ID do usuário
        """
        return await step_context.prompt(
            TextPrompt.__name__,
            PromptOptions(
                prompt=MessageFactory.text("👤 Por favor, digite seu ID de usuário:")
            ),
        )

    async def _list_orders(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        try:
            user_id = int(step_context.result)
            compra_api = ComprasAPI()

            response = compra_api.get_user_orders(user_id)
            orders = response.get("data", []) if isinstance(response, dict) else []

            if not orders:
                message = "📭 **Nenhum pedido encontrado!**\n\nVocê ainda não fez nenhum pedido conosco."
            else:
                message = "📦 **Seus Últimos Pedidos:**\n\n"
                
                # Pega os últimos 5 pedidos de forma segura
                last_orders = orders if len(orders) <= 5 else orders[-5:]
                
                for order in last_orders:
                    if isinstance(order, dict):  # Verifica se order é um dicionário
                        order_id = order.get('id', 'N/A')
                        order_date = order.get('data', 'Data não disponível')
                        order_total = order.get('total', 0)
                        order_items = order.get('itens', [])
                        
                        message += (
                            f"🏷️ **Pedido #{order_id}**\n"
                            f"📅 Data: {order_date}\n"
                            f"💰 Total: R$ {order_total:.2f}\n"
                            f"📦 Itens: {len(order_items)} produto(s)\n\n"
                        )

            await step_context.context.send_activity(MessageFactory.text(message))
        except ValueError:
            await step_context.context.send_activity(
                MessageFactory.text("❌ ID de usuário inválido. Por favor, digite apenas números.")
            )
        except Exception as e:
            await step_context.context.send_activity(
                MessageFactory.text(f"❌ Ocorreu um erro ao buscar seus pedidos. Tente novamente mais tarde.")
            )
            print(f"Erro ao listar pedidos: {str(e)}")
            
        return await step_context.end_dialog()
