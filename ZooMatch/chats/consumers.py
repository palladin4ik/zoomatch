import json
from channels.generic.websocket import AsyncWebsocketConsumer
from channels.db import database_sync_to_async
from django.contrib.auth import get_user_model

from .models import Message


User = get_user_model()


class ChatConsumer(AsyncWebsocketConsumer):

    async def connect(self):
        self.user = self.scope['user']

        if not self.user.is_authenticated:
            await self.close()
            return

        self.other_user_id = self.scope['url_route']['kwargs']['user_id']

        is_matched = await self.check_match()
        if not is_matched:
            await self.close()
            return

        ids = sorted([self.user.id, int(self.other_user_id)])
        self.room_name = f'chat_{ids[0]}_{ids[1]}'

        await self.channel_layer.group_add(
            self.room_name,
            self.channel_name
        )

        await self.accept()

    async def disconnect(self, code):
        if hasattr(self, 'room_name'):
            await self.channel_layer.group_discard(
                self.room_name,
                self.channel_name
            )

    async def receive(self, text_data=None, bytes_data=None):
        data = json.loads(text_data)
        message_type = data.get('type')

        if message_type == 'message':
            await self.handle_message(data)
        elif message_type == 'media_message':
            await self.handle_media_message(data)
        elif message_type == 'read':
            await self.handle_read(data)
        elif message_type == 'delivered':
            await self.handle_delivered(data)

    async def handle_message(self, data):
        text = data.get('text')

        message = await self.save_message(text)

        await self.channel_layer.group_send(
            self.room_name,
            {
                'type': 'chat_message',
                'message_id': message.id,
                'text': message.text,
                'sender_id': self.user.id,
                'created_at': str(message.created_at),
            }
        )

    async def handle_media_message(self, data):
        message_id = data.get('message_id')
        message = await self.get_message(message_id)

        await self.channel_layer.group_send(
            self.room_name,
            {
                'type': 'chat_message',
                'message_id': message.id,
                'text': message.text,
                'sender_id': self.user.id,
                'created_at': str(message.created_at),
                'has_media': True,
                'media_url': message.media.url if message.media else None, # ДОБАВИТЬ ЭТУ СТРОКУ
            }
        )

    async def handle_read(self, data):
        message_id = data.get('message_id')
        await self.mark_as_read(message_id)

        await self.channel_layer.group_send(
            self.room_name,
            {
                'type': 'message_read',
                'message_id': message_id,
            }
        )

    async def handle_delivered(self, data):
        message_id = data.get('message_id')
        await self.mark_as_delivered(message_id)

        await self.channel_layer.group_send(
            self.room_name,
            {
                'type': 'message_delivered',
                'message_id': message_id,
            }
        )

    async def chat_message(self, event):
        await self.send(text_data=json.dumps({
            'type': 'message',
            'message_id': event['message_id'],
            'text': event['text'],
            'sender_id': event['sender_id'],
            'created_at': event['created_at'],
            'has_media': event.get('has_media', False),
            'media_url': event.get('media_url', None), # ДОБАВИТЬ ЭТУ СТРОКУ
        }))

    async def message_read(self, event):
        await self.send(text_data=json.dumps({
            'type': 'read',
            'message_id': event['message_id'],
        }))

    async def message_delivered(self, event):
        await self.send(text_data=json.dumps({
            'type': 'delivered',
            'message_id': event['message_id'],
        }))

    @database_sync_to_async
    def get_message(self, message_id):
        return Message.objects.get(id=message_id, sender=self.user)

    @database_sync_to_async
    def save_message(self, text):
        return Message.objects.create(
            sender=self.user,
            receiver_id=int(self.other_user_id),
            text=text,
        )

    @database_sync_to_async
    def mark_as_read(self, message_id):
        Message.objects.filter(
            id=message_id,
            receiver=self.user
        ).update(is_read=True)

    @database_sync_to_async
    def mark_as_delivered(self, message_id):
        Message.objects.filter(
            id=message_id,
            receiver=self.user
        ).update(is_delivered=True)

    @database_sync_to_async
    def check_match(self):
        from matching.models import Match
        from django.db.models import Q
        import logging
        logger = logging.getLogger(__name__)
        
        other_id = int(self.other_user_id)
        logger.info(f"check_match: user={self.user.id}, other={other_id}")
        
        exists = Match.objects.filter(
            Q(pet_from__owner=self.user, pet_to__owner_id=other_id) |
            Q(pet_from__owner_id=other_id, pet_to__owner=self.user),
            status=Match.Status.ACCEPTED
        ).exists()
        
        logger.info(f"check_match result: {exists}")
        return exists
