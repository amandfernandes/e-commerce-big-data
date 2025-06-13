# dialogs/pedido_dialog.py (corrigido e modernizado)

from botbuilder.dialogs import (
    ComponentDialog,
    WaterfallDialog,
    WaterfallStepContext,
    DialogTurnResult,
)
from botbuilder.core import MessageFactory, UserState

from api.compra_api import ComprasAPI

class PedidoDialog(ComponentDialog):
    # O __init__ agora recebe o user_state, assim como os outros diálogos
    def __init__(self, user_state: UserState):
        super(PedidoDialog, self).__init__(PedidoDialog.__name__)

        self.user_state = user_state
        self.user_id_accessor = self.user_state.create_property("UserId")

        self.add_dialog(
            WaterfallDialog(
                # Não precisamos mais do TextPrompt, pois não vamos pedir o ID
                "PedidoWaterfall",
                [
                    # Removemos o passo que pedia o ID
                    self._list_orders
                ],
            )
        )

        self.initial_dialog_id = "PedidoWaterfall"


    async def _list_orders(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        try:
            # Pegamos o ID do usuário diretamente do UserState, sem perguntar
            user_id = await self.user_id_accessor.get(step_context.context)

            if not user_id:
                await step_context.context.send_activity("Não consegui identificar seu usuário para buscar os pedidos. Por favor, inicie a conversa novamente.")
                return await step_context.end_dialog()

            compra_api = ComprasAPI()
            response = compra_api.get_user_orders(user_id)

            # --- CORREÇÃO PRINCIPAL APLICADA AQUI ---
            # Navegamos pela estrutura aninhada para encontrar a lista de "pedidos"
            orders = response.get("data", {}).get("pedidos", [])

            if not orders:
                message = "📭 **Nenhum pedido encontrado!**\n\nVocê ainda não fez nenhum pedido conosco."
            else:
                message = "📦 **Seus Últimos Pedidos:**\n\n"
                
                last_orders = orders if len(orders) <= 5 else orders[-5:]
                
                for order in last_orders:
                    if isinstance(order, dict):
                        order_id = order.get('id', 'N/A')
                        order_date = order.get('data', 'Data não disponível')
                        order_total = order.get('valorTotal', 0) # Corrigido de 'total' para 'valorTotal'
                        order_items = order.get('itens', [])
                        
                        message += (
                            f"🏷️ **Pedido #{order_id}**\n"
                            f"📅 Data: {order_date}\n"
                            f"💰 Total: R$ {order_total:.2f}\n"
                            f"📦 Itens: {len(order_items)} produto(s)\n\n"
                        )

            await step_context.context.send_activity(MessageFactory.text(message))
        
        except Exception as e:
            await step_context.context.send_activity(
                MessageFactory.text(f"❌ Ocorreu um erro ao buscar seus pedidos. Tente novamente mais tarde.")
            )
            print(f"Erro ao listar pedidos: {str(e)}")
            
        return await step_context.end_dialog()