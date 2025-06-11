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

class PedidoDialog(ComponentDialog):

    def __init__(self):
        super(PedidoDialog, self).__init__(PedidoDialog.__name__)

        self.add_dialog(TextPrompt(TextPrompt.__name__))
        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))

        self.add_dialog(
            WaterfallDialog(
                WaterfallDialog.__name__,
                [
                    self.choice_step,
                    self.action_step,
                    self.final_step,
                ],
            )
        )

        self.initial_dialog_id = WaterfallDialog.__name__

    async def choice_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        prompt_message = "📦 **Consulta de Pedidos**\n\nO que você gostaria de fazer?"

        choices = [
            Choice("📋 Listar Últimos Pedidos", "listar"),
            Choice("🔍 Buscar Pedido Específico", "buscar"),
            Choice("🔙 Voltar", "voltar"),
        ]

        return await step_context.prompt(
            ChoicePrompt.__name__,
            PromptOptions(
                prompt=MessageFactory.text(prompt_message),
                choices=choices,
            ),
        )

    async def action_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        """
        Executa a ação escolhida pelo usuário.
        """
        choice = step_context.result.value

        if choice == "listar":
            return await self._list_orders(step_context)
        elif choice == "buscar":
            return await step_context.prompt(
                TextPrompt.__name__,
                {"prompt": MessageFactory.text("🔍 Digite o ID do pedido que você quer consultar:")}
            )
        elif choice == "voltar":
            return await step_context.end_dialog()

    async def final_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        """
        Processa a busca por ID específico ou finaliza o diálogo.
        """
        if step_context.result and isinstance(step_context.result, str):
            # Usuário digitou um ID para buscar
            order_id = step_context.result.strip()
            await self._search_order_by_id(step_context, order_id)
        
        return await step_context.end_dialog()

    async def _list_orders(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        """
        Lista os últimos pedidos do usuário.
        """
        orders = self.ecommerce_data.get_user_orders()

        if not orders:
            message = "📭 **Nenhum pedido encontrado!**\n\nVocê ainda não fez nenhum pedido conosco."
        else:
            message = "📦 **Seus Últimos Pedidos:**\n\n"
            
            for order in orders[-5:]:
                status_emoji = self._get_status_emoji(order["status"])
                message += (
                    f"🏷️ **Pedido #{order['id']}**\n"
                    f"📅 Data: {order['data']}\n"  # era order['date'], ajuste se o campo for 'data'
                    f"{status_emoji} Status: {order['status']}\n"
                    f"💰 Total: R$ {order['total']:.2f}\n"
                    f"📦 Itens: {len(order['itens'])} produto(s)\n\n"  # era order['items']
                )

        await step_context.context.send_activity(MessageFactory.text(message))
        return await step_context.next(None)

    async def _search_order_by_id(self, step_context: WaterfallStepContext, order_id: str):
        """
        Busca um pedido específico pelo ID.
        """
        try:
            order_id_int = int(order_id)
            order = self.ecommerce_data.get_order_by_id(order_id_int)

            if order:
                message = await self._format_order_details(order)
            else:
                message = f"❌ **Pedido não encontrado!**\n\nNão foi possível encontrar o pedido #{order_id}."

        except ValueError:
            message = f"❌ **ID inválido!**\n\nPor favor, digite apenas números para o ID do pedido."

        await step_context.context.send_activity(MessageFactory.text(message))

    async def _format_order_details(self, order: dict) -> str:
        """
        Formata os detalhes completos de um pedido.
        """
        status_emoji = self._get_status_emoji(order["status"])
        
        message = (
            f"📦 **Detalhes do Pedido #{order['id']}**\n\n"
            f"📅 **Data:** {order['date']}\n"
            f"{status_emoji} **Status:** {order['status']}\n"
            f"💰 **Total:** R$ {order['total']:.2f}\n\n"
            f"📋 **Itens do Pedido:**\n"
        )

        for item in order["items"]:
            message += (
                f"• {item['name']} - Qtd: {item['quantity']} - "
                f"R$ {item['price']:.2f} cada\n"
            )

        # Adiciona informações de entrega se disponível
        if "delivery_info" in order:
            delivery = order["delivery_info"]
            message += (
                f"\n🚚 **Informações de Entrega:**\n"
                f"📍 Endereço: {delivery['address']}\n"
                f"📅 Previsão: {delivery['estimated_date']}\n"
            )

            if "tracking_code" in delivery:
                message += f"📋 Código de Rastreamento: {delivery['tracking_code']}\n"

        return message

    def _get_status_emoji(self, status: str) -> str:
        """
        Retorna emoji apropriado para cada status de pedido.
        """
        status_emojis = {
            "Processando": "⏳",
            "Confirmado": "✅",
            "Enviado": "🚚",
            "Entregue": "📦",
            "Cancelado": "❌",
        }
        return status_emojis.get(status, "❓")