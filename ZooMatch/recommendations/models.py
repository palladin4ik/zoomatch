from django.db import models

from pets.models import Pet


class RecommendationCache(models.Model):
    pet = models.OneToOneField(
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


class RecommendationParams(models.Model):
    pet = models.OneToOneField(
        Pet,
        on_delete=models.CASCADE,
        related_name='recommendation_params'
    )
    radius_km = models.PositiveSmallIntegerField(null=True, blank=True)
    requires_pedigree = models.BooleanField(default=False)
    min_age = models.PositiveSmallIntegerField(null=True, blank=True)
    max_age = models.PositiveSmallIntegerField(null=True, blank=True)
    max_months_since_mating = models.PositiveSmallIntegerField(
        null=True, blank=True
    )

    updated_at = models.DateTimeField(auto_now=True)
