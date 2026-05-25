from celery import shared_task

from .pipeline import run_recommendation_pipeline
from .models import RecommendationCache

from pets.models import Pet


CHUNK_SIZE = 50


@shared_task
def recalculate_recommendations_for_pet(pet_id):
    try:
        pet = Pet.objects.select_related(
            'animal_type', 'breed', 'owner'
            ).get(id=pet_id, is_active=True)
    except Pet.DoesNotExist:
        return

    params = {
        'radius_km': None,
        'requires_pedigree': False,
        'min_age': None,
        'max_age': None,
        'max_months_since_mating': None,
    }

    result = run_recommendation_pipeline(pet, params)

    if result['results']:
        RecommendationCache.objects.update_or_create(
            pet=pet,
            defaults={
                'candidate_ids': [p.id for p in result['results']]
            },
        )


@shared_task
def recalculate_all_recomendations():
    pet_ids = list(
        Pet.objects
        .filter(is_active=True)
        .values_list('id', flat=True)
    )

    for i in range(0, len(pet_ids), CHUNK_SIZE):
        chunk = pet_ids[i:i + CHUNK_SIZE]
        for pet_id in chunk:
            recalculate_recommendations_for_pet.delay(pet_id)
