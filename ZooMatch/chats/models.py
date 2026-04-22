from django.db import models
from django.contrib.auth import get_user_model

from pets.models import Pet


User = get_user_model()


class Chat(models.Model):
    recipient_user = models.ForeignKey(
        User,
        on_delete=models.SET_NULL,
        null=True,
        related_name='received_chats'
    )
    recipient_pet = models.ForeignKey(
        Pet,
        on_delete=models.SET_NULL,
        null=True, blank=True,
        related_name='received_pet_chats'
    )
    sender_user = models.ForeignKey(
        User,
        on_delete=models.SET_NULL,
        null=True,
        related_name='sent_chats'
    )
    sender_pet = models.ForeignKey(
        Pet,
        on_delete=models.SET_NULL,
        null=True, blank=True,
        related_name='sent_pet_chats'
    )


class Message(models.Model):
    sender = models.ForeignKey(
        User,
        on_delete=models.SET_NULL,
        null=True,
        related_name='sent_messages'
    )
    recipient = models.ForeignKey(
        User,
        on_delete=models.SET_NULL,
        null=True,
        related_name='receive_messages'
    )
    content_type = models.CharField(
        max_length=10,
        choices=[
            ('text', 'Текст'),
            ('image', 'Изображение'),
            ('video', 'Видео'),
        ],
        default='text'
    )
    text = models.TextField(blank=True, null=True)
    media = models.FilePathField(blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)


class MessageStatus(models.Model):
    message = models.OneToOneField(
        Message,
        on_delete=models.CASCADE,
        related_name='message_status'
    )
    is_read = models.BooleanField(default=False)
