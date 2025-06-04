from botbuilder.dialogs import (
    ComponentDialog,
    WaterfallDialog,
    WaterfallStepContext,
    DialogTurnResult,
    TextPrompt,
    ChoicePrompt,
)
from botbuilder.dialogs.choices import Choice
from botbuilder.dialogs.prompts import PromptOptions
from botbuilder.core import MessageFactory
from datetime import datetime, timedelta


class ExtratoDialog(ComponentDialog):
    """
    Diálogo para visualização de extrato de compras.
    Permite filtrar por período e valor das transações.
    """

    def __init__(self):
        super(ExtratoDialog, self).__init__(ExtratoDialog.__name__)

        # Adiciona os prompts
        self.add_dialog(TextPrompt(TextPrompt.__name__))
        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))

        # Define o fluxo do diálogo
        self.add_dialog(
            WaterfallDialog(
                WaterfallDialog.__name__,
                [
                    self.filter_step,
                    self.process_step,
                ],
            )
        )

        self.initial_dialog_id = WaterfallDialog.__name__

    async def filter_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        prompt_message = "📊 **Extrato de Compras**\n\nQue período você gostaria de consultar?"

        choices = [
            Choice("📅 Últimos 7 dias", "7_dias"),
            Choice("📅 Último mês", "1_mes"),
            Choice("📅 Últimos 3 meses", "3_meses"),
            Choice("📅 Todas as compras", "todas"),
            Choice("💰 Por valor mínimo", "valor"),
            Choice("🔙 Voltar", "voltar"),
        ]

        return await step_context.prompt(
            ChoicePrompt.__name__,
            PromptOptions(
                prompt=MessageFactory.text(prompt_message),
                choices=choices,
            ),
        )
    async def process_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        """
        Processa o filtro escolhido e mostra o extrato.
        """
        choice = step_context.result.value

        if choice == "voltar":
            return await step_context.end_dialog()
        elif choice == "valor":
            return await step_context.prompt(
                TextPrompt.__name__,
                MessageFactory.text("💰 Digite o valor mínimo das compras (ex: 50.00):")
            )
        else:
            await self._show_statement(step_context, choice)
            return await step_context.end_dialog()

    async def _show_statement(self, step_context: WaterfallStepContext, filter_type: str, min_value: float = None):
        """
        Mostra o extrato filtrado baseado na escolha do usuário.
        """
        purchases = self.ecommerce_data.get_user_purchases()
        
        if not purchases:
            message = "📭 **Nenhuma compra encontrada!**\n\nVocê ainda não realizou compras conosco."
            await step_context.context.send_activity(MessageFactory.text(message))
            return

        # Aplica os filtros
        filtered_purchases = self._apply_filters(purchases, filter_type, min_value)

        if not filtered_purchases:
            message = "❌ **Nenhuma compra encontrada com os filtros aplicados.**"
        else:
            message = self._format_statement(filtered_purchases, filter_type, min_value)

        await step_context.context.send_activity(MessageFactory.text(message))

    def _apply_filters(self, purchases: list, filter_type: str, min_value: float = None) -> list:
        """
        Aplica filtros nas compras baseado no tipo escolhido.
        """
        if filter_type == "todas":
            filtered = purchases
        elif filter_type == "7_dias":
            cutoff_date = datetime.now() - timedelta(days=7)
            filtered = [p for p in purchases if datetime.strptime(p['date'], '%d/%m/%Y') >= cutoff_date]
        elif filter_type == "1_mes":
            cutoff_date = datetime.now() - timedelta(days=30)
            filtered = [p for p in purchases if datetime.strptime(p['date'], '%d/%m/%Y') >= cutoff_date]
        elif filter_type == "3_meses":
            cutoff_date = datetime.now() - timedelta(days=90)
            filtered = [p for p in purchases if datetime.strptime(p['date'], '%d/%m/%Y') >= cutoff_date]
        else:
            filtered = purchases

        # Aplica filtro de valor se especificado
        if min_value is not None:
            filtered = [p for p in filtered if p['total'] >= min_value]

        return filtered

    def _format_statement(self, purchases: list, filter_type: str, min_value: float = None) -> str:
        """
        Formata o extrato para exibição.
        """
        total_spent = sum(purchase['total'] for purchase in purchases)
        
        # Título baseado no filtro
        if filter_type == "7_dias":
            title = "📊 **Extrato - Últimos 7 dias**"
        elif filter_type == "1_mes":
            title = "📊 **Extrato - Último mês**"
        elif filter_type == "3_meses":
            title = "📊 **Extrato - Últimos 3 meses**"
        elif min_value:
            title = f"📊 **Extrato - Compras acima de R$ {min_value:.2f}**"
        else:
            title = "📊 **Extrato Completo**"

        message = f"{title}\n\n"
        message += f"💰 **Total gasto:** R$ {total_spent:.2f}\n"
        message += f"🛍️ **Quantidade de compras:** {len(purchases)}\n\n"
        message += "📋 **Detalhes das compras:**\n\n"

        for purchase in purchases[-10:]:  # Mostra as 10 mais recentes
            message += (
                f"🏷️ **Pedido #{purchase['order_id']}**\n"
                f"📅 Data: {purchase['date']}\n"
                f"💰 Valor: R$ {purchase['total']:.2f}\n"
                f"💳 Forma de pagamento: {purchase['payment_method']}\n"
                f"📦 Produtos: {', '.join([item['name'] for item in purchase['items']])}\n\n"
            )

        if len(purchases) > 10:
            message += f"... e mais {len(purchases) - 10} compra(s).\n"

        return message