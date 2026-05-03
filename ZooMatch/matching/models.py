from django.db import models
from pets.models import Pet


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
        unique_together = ('pet_from', 'pet_to')


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
        unique_together = ('pet_from', 'pet_to')


class ActionCategory(models.Model):
    name = models.CharField(max_length=50, unique=True)


class Action(models.Model):
    pet = models.ForeignKey(
        Pet,
        on_delete=models.CASCADE,
        related_name='actions'
    )
    category = models.ForeignKey(
        ActionCategory,
        on_delete=models.CASCADE
    )

    created_at = models.DateTimeField(auto_now_add=True)
