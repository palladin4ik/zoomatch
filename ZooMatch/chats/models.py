from django.db import models
from django.contrib.auth import get_user_model


User = get_user_model()


class Message(models.Model):
    sender = models.ForeignKey(
        User,
        on_delete=models.SET_NULL,
        null=True,
        related_name='sent_messages'
    )
    receiver = models.ForeignKey(
        User,
        on_delete=models.SET_NULL,
        null=True,
        related_name='received_messages'
    )

    text = models.TextField(blank=True, null=True)
    media = models.FileField(upload_to='messages/', blank=True, null=True)

    is_delivered = models.BooleanField(default=False)
    is_read = models.BooleanField(default=False)

    deleted_by_sender = models.BooleanField(default=False)
    deleted_by_receiver = models.BooleanField(default=False)

    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['sender', 'receiver', 'created_at']),
            models.Index(fields=['receiver', 'sender', 'created_at']),
        ]
