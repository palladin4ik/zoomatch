from django.db import models

from pets.models import Pet


class RecommendationCache(models.Model):
    pet = models.ForeignKey(
        Pet,
        on_delete=models.CASCADE,
        related_name='recommendation_cache'
    )
    candidate_ids = models.JSONField(default=list)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        indexes = [
            models.Index(fields=['updated_at'])
        ]
