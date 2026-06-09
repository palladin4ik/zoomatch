from django.contrib.auth import get_user_model
from django.db import models
from pets.models import Pet


User = get_user_model()


class Match(models.Model):
    pet_from = models.ForeignKey(
        Pet,
        on_delete=models.CASCADE,
        related_name='sent_matches'
    )
    pet_to = models.ForeignKey(
        Pet,
        on_delete=models.CASCADE,
        related_name='received_matches'
    )

    class Status(models.IntegerChoices):
        PENDING = 0
        ACCEPTED = 1
        REJECTED = 2
        CLOSED = 3

    status = models.PositiveSmallIntegerField(choices=Status.choices,
                                              default=Status.PENDING)

    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        constraints = [
            models.UniqueConstraint(
                fields=['pet_from', 'pet_to'],
                name='unique_match'
            )
        ]

        indexes = [
            models.Index(fields=['status']),
            models.Index(fields=['created_at']),
        ]


class Rejection(models.Model):
    pet_from = models.ForeignKey(
        Pet,
        on_delete=models.CASCADE,
        related_name='rejections_sent'
    )
    pet_to = models.ForeignKey(
        Pet,
        on_delete=models.CASCADE,
        related_name='rejections_received'
    )

    count = models.PositiveIntegerField(default=1)
    last_rejected_at = models.DateTimeField(auto_now=True)

    class Meta:
        indexes = [
            models.Index(fields=['count']),
            models.Index(fields=['last_rejected_at']),
        ]


class ActionCategory(models.Model):
    name = models.CharField(max_length=50, unique=True)


class Action(models.Model):
    user = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        related_name='actions'
    )
    pet = models.ForeignKey(
        Pet,
        on_delete=models.CASCADE,
        related_name='actions'
    )
    category = models.ForeignKey(
        ActionCategory,
        on_delete=models.CASCADE
    )
    value = models.PositiveSmallIntegerField(blank=True, null=True)

    created_at = models.DateTimeField(auto_now_add=True)
