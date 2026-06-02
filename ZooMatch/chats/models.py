from django.db import models
from django.contrib.auth import get_user_model

from core.uploads import message_media_path
from core.validators import validate_file_size, validate_media_type


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
    media = models.FileField(
        upload_to=message_media_path,
        blank=True, null=True,
        validators=[validate_media_type, validate_file_size]
    )

    is_delivered = models.BooleanField(default=False)
    is_read = models.BooleanField(default=False)

    deleted_by_sender = models.BooleanField(default=False)
    deleted_by_receiver = models.BooleanField(default=False)

    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['sender', 'receiver', '-created_at']),
            models.Index(fields=['receiver', 'sender', '-created_at']),
        ]
