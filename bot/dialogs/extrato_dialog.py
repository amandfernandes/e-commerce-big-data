# dialogs/extrato_dialog.py (corrigido e modernizado)
from datetime import datetime
from botbuilder.dialogs import (
    ComponentDialog,
    WaterfallDialog,
    WaterfallStepContext,
    DialogTurnResult,
    ChoicePrompt,
)
from botbuilder.dialogs.choices import Choice
from botbuilder.core import MessageFactory, UserState
from botbuilder.dialogs.prompts import PromptOptions
from botbuilder.schema import CardAction, ActionTypes
from dateutil.relativedelta import relativedelta
from api.cartao_api import CartaoAPI

class ExtratoDialog(ComponentDialog):
    # O __init__ agora recebe user_state
    def __init__(self, user_state: UserState):
        super(ExtratoDialog, self).__init__(ExtratoDialog.__name__)
        
        self.user_state = user_state
        self.user_id_accessor = self.user_state.create_property("UserId")
        self.cartao_api = CartaoAPI()

        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))
        self.add_dialog(
            WaterfallDialog(
                "ExtratoWaterfall",
                [
                    # Removemos o passo de validação de usuário
                    self.select_card_step,
                    self.select_month_step,
                    self.show_extrato_step,
                ]
            )
        )
        
        self.initial_dialog_id = "ExtratoWaterfall"

    async def select_card_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        try:
            # Pegamos o ID do usuário diretamente do UserState
            user_id = await self.user_id_accessor.get(step_context.context)
            if not user_id:
                await step_context.context.send_activity("Não consegui identificar seu usuário. Por favor, reinicie a conversa.")
                return await step_context.end_dialog()
            
            # Guardamos o user_id para usar no último passo
            step_context.values["user_id"] = user_id
            
            api_cards = self.cartao_api.get_user_cards(user_id)
            
            if not api_cards:
                await step_context.context.send_activity("❌ Você não possui cartões cadastrados.")
                return await step_context.end_dialog()

            sanitized_cards = [{"id": card.get("id"), "numero": card.get("numero")} for card in api_cards]
            step_context.values["cards"] = sanitized_cards

            if len(sanitized_cards) == 1:
                # Se só há um cartão, pulamos a pergunta
                return await step_context.next(str(sanitized_cards[0]["id"]))

            # Usando o padrão correto de Choice com CardAction
            card_choices = [
                Choice(
                    value=str(card["id"]),
                    action=CardAction(
                        type=ActionTypes.im_back,
                        title=f"Cartão final {card['numero'][-4:]}",
                        value=str(card["id"])
                    )
                ) for card in sanitized_cards
            ]

            return await step_context.prompt(
                ChoicePrompt.__name__,
                PromptOptions(
                    prompt=MessageFactory.text("Qual cartão você quer ver o extrato?"),
                    choices=card_choices
                )
            )
        except Exception as e:
            print(f"Erro em select_card_step: {e}")
            await step_context.context.send_activity("Ocorreu um erro ao buscar seus cartões.")
            return await step_context.end_dialog()


    async def select_month_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        # CORREÇÃO: Acessamos .value para pegar o ID do cartão do objeto FoundChoice
        # Se o passo anterior pulou a pergunta, step_context.result será uma string.
        card_id = step_context.result.value if hasattr(step_context.result, 'value') else step_context.result
        step_context.values["card_id"] = card_id

        current_date = datetime.now().replace(day=1)
        months = []
        
        for i in range(12):
            date = current_date - relativedelta(months=i)
            month_name = date.strftime("%B/%Y").capitalize()
            month_value = f"{date.month}/{date.year}"

            months.append(Choice(
                value=month_value,
                action=CardAction(
                    type=ActionTypes.im_back,
                    title=month_name,
                    value=month_value
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
        
        selected_month_result = step_context.result
        selected_month_value = selected_month_result.value

        month, year = map(int, selected_month_value.split('/'))
        
        start_date = f"{year}-{month:02d}-01"
        end_date = f"{year}-{month:02d}-31"

        extrato = self.cartao_api.get_card_statement(user_id, card_id, start_date, end_date)
        
        if not extrato:
            await step_context.context.send_activity(
                f"📭 Não há transações para o mês de {datetime(year, month, 1).strftime('%B/%Y').capitalize()}."
            )
            return await step_context.end_dialog()

        message = f"📊 **EXTRATO DO CARTÃO - {datetime(year, month, 1).strftime('%B/%Y').capitalize()}**\n\n"
        
        for transacao in extrato:
            # --- CORREÇÃO DA FORMATAÇÃO DA DATA ---
            
            # 1. Pega a data completa da API
            raw_date_str = transacao.get("data", "Data não disponível")
            formatted_date = raw_date_str # Define um valor padrão caso a formatação falhe

            # 2. Tenta converter para o formato DD/MM/AAAA
            if raw_date_str != "Data não disponível":
                try:
                    # Pega apenas a parte da data (YYYY-MM-DD), ignorando a hora e os microsegundos
                    date_part_str = raw_date_str.split(" ")[0]
                    # Converte a string em um objeto de data
                    date_obj = datetime.strptime(date_part_str, "%Y-%m-%d")
                    # Formata o objeto de data para o padrão brasileiro
                    formatted_date = date_obj.strftime("%d/%m/%Y")
                except (ValueError, IndexError):
                    # Se a conversão falhar, usa os 10 primeiros caracteres como um fallback seguro
                    formatted_date = raw_date_str[:10]

            # 3. Usa a data já formatada na mensagem
            data = formatted_date
            # --- FIM DA CORREÇÃO ---

            valor = transacao.get("valor", 0)
            status = transacao.get("status", "")
            codigo = transacao.get("codigoAutorizacao", "")
            
            message += (
                f"📅 Data: {data}\n"
                f"💰 Valor: R$ {valor:.2f}\n"
                f"✔️ Status: {status}\n"
                f"🔑 Código: {codigo}\n"
                f"{'─' * 30}\n"
            )

        await step_context.context.send_activity(MessageFactory.text(message))
        return await step_context.end_dialog()