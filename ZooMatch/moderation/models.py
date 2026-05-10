from django.db import models
from django.contrib.auth import get_user_model

from pets.models import Pet


User = get_user_model()


class ModerationRequest(models.Model):
    pet = models.OneToOneField(
        Pet,
        on_delete=models.CASCADE,
        related_name='moderation_request'
    )
    animal_type = models.CharField(max_length=50, null=True, blank=True)
    breed = models.CharField(max_length=100, null=True, blank=True)

    class Status(models.IntegerChoices):
        PENDING = 0
        APPROVED = 1
        REJECTED = 2
    
    status = models.PositiveSmallIntegerField(
        choices=Status,
        default=Status.PENDING
    )

    created_at = models.DateTimeField(auto_now_add=True)

    reviewed_by = models.ForeignKey(
        User,
        on_delete=models.SET_NULL,
        related_name='reviewed_request',
        null=True, blank=True
    )
    reviewed_at = models.DateTimeField(null=True, blank=True)
