from datetime import datetime
from botbuilder.dialogs import (
    ComponentDialog,
    WaterfallDialog,
    WaterfallStepContext,
    DialogTurnResult,
    ChoicePrompt,
    TextPrompt,
)
from botbuilder.dialogs.choices import Choice
from botbuilder.core import MessageFactory
from botbuilder.dialogs.prompts import PromptOptions
from botbuilder.schema import CardAction, ActionTypes
from dateutil.relativedelta import relativedelta
from api.cartao_api import CartaoAPI

class ExtratoDialog(ComponentDialog):
    def __init__(self, user_state):
        super(ExtratoDialog, self).__init__(ExtratoDialog.__name__)
        
        self.cartao_api = CartaoAPI()

        self.add_dialog(TextPrompt(TextPrompt.__name__))
        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))
        self.add_dialog(
            WaterfallDialog(
                "ExtratoWaterfall",
                [
                    self.validate_user_step,
                    self.select_card_step,
                    self.select_month_step,
                    self.show_extrato_step,
                ]
            )
        )
        
        self.initial_dialog_id = "ExtratoWaterfall"

    async def validate_user_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        return await step_context.prompt(
            TextPrompt.__name__,
            PromptOptions(
                prompt=MessageFactory.text("👤 Por favor, digite seu ID de usuário:")
            ),
        )

    async def select_card_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        try:
            user_id = int(step_context.result)
            step_context.values["user_id"] = user_id
            
            # 1. Recebemos a resposta da API, que pode conter objetos complexos.
            api_cards = self.cartao_api.get_user_cards(user_id)
            
            if not api_cards:
                await step_context.context.send_activity("❌ Você não possui cartões cadastrados.")
                return await step_context.end_dialog()

            # =================================================================================
            # ESTA É A CORREÇÃO DEFINITIVA PARA O RECURSIONERROR
            # =================================================================================
            # 2. "Sanitizamos" a resposta, criando uma lista de dicionários simples
            #    apenas com os dados que precisamos. Isso remove qualquer referência circular.
            sanitized_cards = [
                {"id": card["id"], "numero": card["numero"]} for card in api_cards
            ]
            # =================================================================================
            
            # 3. Salvamos a lista "limpa" no estado do diálogo.
            step_context.values["cards"] = sanitized_cards

            # Daqui para frente, usamos apenas a lista "limpa"
            if len(sanitized_cards) == 1:
                return await step_context.next(str(sanitized_cards[0]["id"]))

            card_choices = [
                Choice(
                    value=f"Cartão final {card['numero'][-4:]}",
                    action=CardAction(
                        type=ActionTypes.im_back,
                        title=f"Cartão final {card['numero'][-4:]}",
                        value=str(card["id"])
                    )
                )
                for card in sanitized_cards # Usando a lista limpa
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

    async def select_month_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        card_id = step_context.result
        step_context.values["card_id"] = card_id

        current_date = datetime.now().replace(day=1)
        months = []
        
        for i in range(12):
            date = current_date - relativedelta(months=i)
            
            month_name = date.strftime("%B/%Y").capitalize() # Ex: "Maio/2025"
            month_value = f"{date.month}/{date.year}"       # Ex: "5/2025"

            # --- A CORREÇÃO ESTÁ AQUI ---
            # O 'value' do Choice agora é o mesmo 'value' do CardAction.
            # O 'title' do CardAction é o que o usuário vê no botão.
            months.append(Choice(
                value=month_value,  # DEVE SER o valor que o bot recebe. Ex: "5/2025"
                action=CardAction(
                    type=ActionTypes.im_back,
                    title=month_name,   # O que o usuário lê. Ex: "Maio/2025"
                    value=month_value   # O que o bot recebe ao clicar. Ex: "5/2025"
                )
            ))

        return await step_context.prompt(
            ChoicePrompt.__name__,
            PromptOptions(
                prompt=MessageFactory.text("📅 Selecione o mês do extrato:"),
                choices=months
            )
        )

    async def show_extrato_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        user_id = step_context.values["user_id"]
        card_id = step_context.values["card_id"]
        selected_month = step_context.result
        
        month, year = map(int, selected_month.split('/'))
        
        start_date = f"{year}-{month:02d}-01"
        end_date = f"{year}-{month:02d}-31"

        extrato = self.cartao_api.get_card_statement(user_id, card_id, start_date, end_date)
        
        if not extrato:
            await step_context.context.send_activity(
                f"📭 Não há transações para o mês de {datetime(year, month, 1).strftime('%B/%Y').capitalize()}."
            )
            return await step_context.end_dialog()

        mensagem = f"📊 **EXTRATO DO CARTÃO - {datetime(year, month, 1).strftime('%B/%Y').capitalize()}**\n\n"
        
        for transacao in extrato:
            data = transacao.get("data", "")[:10]
            valor = transacao.get("valor", 0)
            status = transacao.get("status", "")
            codigo = transacao.get("codigoAutorizacao", "")
            
            mensagem += (
                f"📅 Data: {data}\n"
                f"💰 Valor: R$ {valor:.2f}\n"
                f"✔️ Status: {status}\n"
                f"🔑 Código: {codigo}\n"
                f"{'─' * 30}\n"
            )

        await step_context.context.send_activity(MessageFactory.text(mensagem))
        return await step_context.end_dialog()