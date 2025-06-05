# dialogs/purchase_dialog.py
# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

from botbuilder.dialogs import (
    ComponentDialog,
    WaterfallDialog,
    WaterfallStepContext,
    DialogTurnResult,
    TextPrompt,
    ChoicePrompt,
    NumberPrompt,
)
from botbuilder.dialogs.choices import Choice
from botbuilder.core import MessageFactory, UserState

from api.product_api import ProductAPI
from api.compra_api import ComprasAPI

class CompraDialog(ComponentDialog):
    def __init__(self, user_state: UserState):
        super(CompraDialog, self).__init__(CompraDialog.__name__)

        self.user_state = user_state
        self.user_id_accessor = self.user_state.create_property("UserId")
        self.cart_accessor = self.user_state.create_property("Cart")
        
        self.product_api = ProductAPI()
        self.compras_api = ComprasAPI()
        self.user_state = user_state

        self.add_dialog(TextPrompt(TextPrompt.__name__))
        self.add_dialog(ChoicePrompt(ChoicePrompt.__name__))
        self.add_dialog(NumberPrompt(NumberPrompt.__name__))

        self.add_dialog(
            WaterfallDialog(
                WaterfallDialog.__name__,
                [
                    self.menu_step,
                    self.action_step,
                    self.process_step,
                    self.confirm_step,
                    self.final_step,
                ],
            )
        )

        self.initial_dialog_id = WaterfallDialog.__name__

    async def menu_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        cart = await self.cart_accessor.get(step_context.context, lambda: [])
        
        cart_count = len(cart)
        cart_info = f" ({cart_count} item{'s' if cart_count != 1 else ''})" if cart_count > 0 else ""

        prompt_message = f"🛒 **Área de Compras**{cart_info}\n\nO que você gostaria de fazer?"

        choices = [
            Choice("➕ Adicionar Produto", "adicionar"),
            Choice(f"🛒 Ver Carrinho{cart_info}", "carrinho"),
            Choice("✅ Finalizar Compra", "finalizar"),
            Choice("🗑️ Limpar Carrinho", "limpar"),
            Choice("🔙 Voltar", "voltar"),
        ]

        return await step_context.prompt(
            ChoicePrompt.__name__,
            {
                "prompt": MessageFactory.text(prompt_message),
                "choices": choices,
            },
        )

    async def action_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        choice = step_context.result.value
        step_context.values["action"] = choice

        if choice == "adicionar":
            return await step_context.prompt(
                TextPrompt.__name__,
                MessageFactory.text("🔍 Digite o nome ou ID do produto que deseja adicionar:")
            )
        elif choice == "carrinho":
            await self._show_cart(step_context)
            return await step_context.replace_dialog(self.id)
        elif choice == "finalizar":
            return await self._start_checkout(step_context)
        elif choice == "limpar":
            await self._clear_cart(step_context)
            return await step_context.replace_dialog(self.id)
        elif choice == "voltar":
            return await step_context.end_dialog()

    async def process_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        action = step_context.values.get("action")
        
        if action == "adicionar":
            search_term = step_context.result
            return await self._add_product_to_cart(step_context, search_term)
        elif action == "finalizar":
            # Continuação do checkout
            payment_method = step_context.result.value
            step_context.values["payment_method"] = payment_method
            return await self._show_order_summary(step_context)

    async def confirm_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        if step_context.values.get("action") == "finalizar":
            choices = [
                Choice("✅ Confirmar Pedido", "confirmar"),
                Choice("❌ Cancelar", "cancelar"),
            ]

            return await step_context.prompt(
                ChoicePrompt.__name__,
                {
                    "prompt": MessageFactory.text("🤔 **Confirmar o pedido?**"),
                    "choices": choices,
                },
            )
        else:
            return await step_context.next(None)

    async def final_step(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        if step_context.values.get("action") == "finalizar":
            choice = step_context.result.value if step_context.result else "cancelar"
            
            if choice == "confirmar":
                await self._process_order(step_context)
            else:
                await step_context.context.send_activity(
                    MessageFactory.text("❌ **Pedido cancelado!**\n\nSeus itens permanecem no carrinho.")
                )

        return await step_context.end_dialog()

    async def _add_product_to_cart(self, step_context: WaterfallStepContext, search_term: str) -> DialogTurnResult:
        products = self.product_api.search_product(search_term)
        
        if not products:
            try:
                product = self.product_api.get_product_by_id(search_term)
                if product:
                    products = [product]
            except:
                pass

        if not products:
            await step_context.context.send_activity(
                MessageFactory.text(f"❌ **Produto não encontrado!**\n\nNão consegui encontrar '{search_term}'.")
            )
            return await step_context.replace_dialog(self.id)

        product = products[0]
        step_context.values["selected_product"] = product

        return await step_context.prompt(
            NumberPrompt.__name__,
            {
                "prompt": MessageFactory.text(
                    f"📦 **{product['nome']}** - R$ {product['preco']:.2f}\n\n"
                    f"Quantas unidades você deseja adicionar?"
                ),
                "retry_prompt": MessageFactory.text("❌ Por favor, digite um número válido.")
            }
        )

    async def _show_cart(self, step_context: WaterfallStepContext):
        cart = await self.cart_accessor.get(step_context.context, lambda: [])

        if not cart:
            message = "🛒 **Seu carrinho está vazio!**\n\nAdicione alguns produtos para começar suas compras."
        else:
            total = sum(item["price"] * item["quantity"] for item in cart)
            
            message = "🛒 **Seu Carrinho:**\n\n"
            
            for item in cart:
                subtotal = item["price"] * item["quantity"]
                message += (
                    f"📦 **{item['name']}**\n"
                    f"💰 R$ {item['price']:.2f} x {item['quantity']} = R$ {subtotal:.2f}\n\n"
                )
            
            message += f"💰 **Total: R$ {total:.2f}**"

        await step_context.context.send_activity(MessageFactory.text(message))

    async def _clear_cart(self, step_context: WaterfallStepContext):
        await self.cart_accessor.set(step_context.context, [])
        await step_context.context.send_activity(
            MessageFactory.text("🗑️ **Carrinho limpo!**\n\nTodos os itens foram removidos.")
        )

    async def _start_checkout(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        cart = await self.cart_accessor.get(step_context.context, lambda: [])

        if not cart:
            await step_context.context.send_activity(
                MessageFactory.text("🛒 **Carrinho vazio!**\n\nAdicione produtos antes de finalizar a compra.")
            )
            return await step_context.replace_dialog(self.id)

        choices = [
            Choice("💳 Cartão de Crédito", "credito"),
            Choice("💰 Cartão de Débito", "debito"),
            Choice("🏦 PIX", "pix"),
            Choice("💵 Boleto Bancário", "boleto"),
        ]

        return await step_context.prompt(
            ChoicePrompt.__name__,
            {
                "prompt": MessageFactory.text("💳 **Escolha a forma de pagamento:**"),
                "choices": choices,
            },
        )

    async def _show_order_summary(self, step_context: WaterfallStepContext) -> DialogTurnResult:
        
        cart = await self.cart_accessor.get(step_context.context, lambda: [])
        payment_method = step_context.values["payment_method"]

        total = sum(item["price"] * item["quantity"] for item in cart)
        
        fee = 0
        if payment_method == "credito":
            fee = total * 0.03
        elif payment_method == "boleto":
            fee = 2.50

        final_total = total + fee

        payment_names = {
            "credito": "Cartão de Crédito",
            "debito": "Cartão de Débito", 
            "pix": "PIX",
            "boleto": "Boleto Bancário"
        }

        message = (
            "📋 **Resumo do Pedido:**\n\n"
            "🛒 **Itens:**\n"
        )

        for item in cart:
            subtotal = item["price"] * item["quantity"]
            message += f"• {item['name']} x{item['quantity']} - R$ {subtotal:.2f}\n"

        message += (
            f"\n💰 **Subtotal:** R$ {total:.2f}\n"
            f"💳 **Forma de pagamento:** {payment_names[payment_method]}\n"
        )

        if fee > 0:
            message += f"📄 **Taxa:** R$ {fee:.2f}\n"

        message += f"💰 **Total Final:** R$ {final_total:.2f}\n"

        await step_context.context.send_activity(MessageFactory.text(message))
        return await step_context.next(None)

    async def _process_order(self, step_context: WaterfallStepContext):
        """Processa o pedido final."""
        user_id = await self.user_id_accessor.get(step_context.context, lambda: 1)
        cart = await self.cart_accessor.get(step_context.context, lambda: [])
        payment_method = step_context.values["payment_method"]

        payment_info = {
            "method": payment_method,
            "details": {}
        }
        order = self.compras_api.create_order(user_id, cart, payment_info)

        if order:
            await self.cart_accessor.set(step_context.context, [])
            
            message = (
                f"✅ **Pedido realizado com sucesso!**\n\n"
                f"🏷️ **Número do pedido:** #{order['id']}\n"
                f"💳 **Pagamento:** {payment_method}\n"
                f"📧 **Confirmação enviada por email**\n\n"
                f"🚚 **Prazo de entrega:** 3-5 dias úteis"
            )
        else:
            message = "❌ **Erro ao processar pedido!**\n\nTente novamente mais tarde."

        await step_context.context.send_activity(MessageFactory.text(message))